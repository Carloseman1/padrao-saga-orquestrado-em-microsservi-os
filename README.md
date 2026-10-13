# Arquitetura de Microsserviços: Padrão Saga Orquestrado

Pedido de uma livraria passando por três etapas, cada uma num microsserviço com o próprio banco: validar se os livros existem no catálogo, cobrar e baixar o estoque. Como não existe transação que atravesse bancos diferentes, a consistência é garantida por uma saga: um orquestrador decide a próxima etapa e, quando alguma falha, manda desfazer as anteriores na ordem inversa.

Projeto de estudo de arquitetura de microsserviços, comunicação assíncrona com Kafka e consistência eventual.

**Atalhos:** [Como executar](#como-executar) · [Arquitetura](#arquitetura) · [Como a saga funciona](#como-a-saga-funciona) · [Por que MongoDB no pedido](#por-que-mongodb-no-pedido-service) · [Modelo de dados](#modelo-de-dados) · [Decisões técnicas](#decisões-técnicas) · [Endpoints](#endpoints) · [O que ainda daria pra melhorar](#o-que-ainda-daria-pra-melhorar)

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem |
| Spring Boot 3.5.14 | Framework principal |
| Apache Kafka | Mensageria entre os serviços |
| MongoDB 7 | Banco do pedido-service |
| PostgreSQL 16 | Banco dos serviços de validação, pagamento e estoque |
| Spring Data JPA + Flyway | Persistência e migrations no PostgreSQL |
| Spring Data MongoDB | Persistência no pedido-service |
| springdoc-openapi | Swagger do pedido-service |
| JUnit 5 + Mockito + AssertJ | Testes |
| Docker + docker compose | Ambiente local |
| Redpanda Console | Visualizar tópicos e mensagens do Kafka |

44 testes, nenhum deles sobe contexto do Spring nem encosta em banco ou Kafka.

---

## Como executar

### Pré-requisitos

- Java 21+
- Docker

Não precisa instalar Maven. Cada serviço tem o wrapper (`mvnw` / `mvnw.cmd`), que baixa a versão certa na primeira execução.

### Opção 1: tudo no Docker pelo script

Na raiz do repositório, com Python 3 instalado (no Windows o comando é `py`):

```powershell
py build.py
```

O script derruba só os containers deste projeto, avisa se alguma porta já está ocupada por outro processo, sobe tudo com `docker compose up --build -d` e espera cada serviço iniciar, mostrando quem subiu e quem falhou.

| Comando | O que faz |
|---|---|
| `py build.py` | Sobe tudo |
| `py build.py --testes` | Roda os testes dos cinco serviços antes e não sobe nada se algum falhar |
| `py build.py --parar` | Derruba os containers |

Com o projeto no Docker, não rode os serviços também pelo `mvnw`: as portas já estão ocupadas pelos containers.

### Opção 1b: tudo no Docker sem o script

Na raiz do repositório:

```powershell
docker compose up --build -d
```

O build das aplicações acontece dentro das imagens (Dockerfile multi-stage), então a primeira subida demora alguns minutos baixando dependências. As seguintes reaproveitam o cache.

Os serviços só sobem depois que o banco deles responde ao healthcheck, e as migrations do Flyway rodam sozinhas na inicialização. Para acompanhar:

```powershell
docker compose ps
docker compose logs -f orquestrador-service
```

Para derrubar tudo:

```powershell
docker compose down
```

### Opção 2: infraestrutura no Docker, serviços pela IDE ou terminal

É o jeito mais prático pra desenvolver, porque dá pra debugar um serviço sem rebuildar imagem.

**Terminal 1, bancos e Kafka:**

```powershell
docker compose up -d pedido-db validacao-livro-db pagamento-db estoque-db kafka redpanda-console
```

**Um terminal por serviço:**

```powershell
cd orquestrador-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd pedido-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd validacao-livro-service
.\mvnw.cmd spring-boot:run
```

```powershell
cd pagamento-service
$env:DB_PORT="5433"; .\mvnw.cmd spring-boot:run
```

```powershell
cd estoque-service
$env:DB_PORT="5434"; .\mvnw.cmd spring-boot:run
```

O `DB_PORT` é necessário no pagamento e no estoque porque cada Postgres do compose está numa porta diferente do host (5432, 5433 e 5434), e o padrão das três aplicações é 5432. No Linux ou no Git Bash troque por `DB_PORT=5433 ./mvnw spring-boot:run`.

Pela IDE, rode a classe `*Application` de cada serviço e defina `DB_PORT` nas variáveis de ambiente da configuração de execução do pagamento e do estoque.

### Testando o fluxo

O jeito mais rápido é o Swagger do pedido-service: http://localhost:3000/swagger-ui.html

Pelo curl (Git Bash):

```bash
curl -X POST http://localhost:3000/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"itens":[{"livro":{"codigo":"DOM_CASMURRO","valorUnitario":39.90},"quantidade":2}]}'
```

A resposta traz o `id` e o `transacaoId` do pedido. Com qualquer um dos dois dá pra acompanhar o resultado:

```bash
curl "http://localhost:3000/api/eventos?pedidoId=<id do pedido>"
```

Para ver uma saga falhar e fazer rollback, peça mais do que tem em estoque, por exemplo 3 unidades de `O_CORTICO`, que tem 2. Ou um código que não está no catálogo.

Os livros que já vêm cadastrados pelas migrations:

| Código | Título | Estoque |
|---|---|---|
| `DOM_CASMURRO` | Dom Casmurro | 4 |
| `O_CORTICO` | O Cortiço | 2 |
| `IRACEMA` | Iracema | 5 |
| `MEMORIAS_POSTUMAS` | Memórias Póstumas de Brás Cubas | 9 |

### Rodando os testes

Dentro de cada serviço:

```powershell
.\mvnw.cmd test
```

### Endereços

| O quê | Endereço |
|---|---|
| pedido-service (API e Swagger) | http://localhost:3000 |
| orquestrador-service | 8080 |
| validacao-livro-service | 8090 |
| pagamento-service | 8091 |
| estoque-service | 8092 |
| Redpanda Console | http://localhost:8081 |
| Kafka | localhost:9092 |
| MongoDB (pedido-db) | localhost:27017 |
| PostgreSQL validacao-livro-db / pagamento-db / estoque-db | localhost:5432 / 5433 / 5434 |

Usuário e senha dos bancos de desenvolvimento estão no `docker-compose.yml`.

---

## Arquitetura

```mermaid
flowchart LR
    cliente([Cliente / Swagger])

    subgraph pedido [pedido-service :3000]
        api[API REST]
    end
    mongo[(MongoDB<br/>pedido-db)]

    kafka{{Apache Kafka}}

    orq[orquestrador-service<br/>sem banco]

    val[validacao-livro-service]
    pg1[(PostgreSQL<br/>validacao-livro-db)]

    pag[pagamento-service]
    pg2[(PostgreSQL<br/>pagamento-db)]

    est[estoque-service]
    pg3[(PostgreSQL<br/>estoque-db)]

    cliente -- HTTP --> api
    api --- mongo
    api -- inicio-saga --> kafka
    kafka -- notificar-fim --> api

    kafka <--> orq
    kafka <--> val
    kafka <--> pag
    kafka <--> est

    val --- pg1
    pag --- pg2
    est --- pg3
```

O único serviço com API para fora é o **pedido-service**. Ele grava o pedido, publica o evento em `inicio-saga` e, no fim, recebe o resultado em `notificar-fim`. Daí pra frente ninguém chama ninguém por HTTP: tudo passa pelo Kafka.

O **orquestrador** é o único que conhece a ordem das etapas. Ele não tem banco, porque não guarda estado: a decisão sai inteira da origem e do status do evento que acabou de chegar, consultando a tabela de transições (`TransicaoSaga`).

Os três **participantes** não sabem que os outros existem. Cada um escuta dois tópicos, um para executar a etapa (`*-sucesso`) e outro para desfazê-la (`*-falha`), e sempre responde no mesmo tópico, `orquestrador`. Isso deixa a ordem da saga num lugar só: trocar pagamento e estoque de posição muda uma tabela no orquestrador, e nenhum participante.

Cada serviço tem o próprio banco e nenhum lê o banco do outro. O que um serviço precisa saber do pedido chega dentro do evento.

| Serviço | Responsabilidade | Banco |
|---|---|---|
| pedido-service | Recebe o pedido, inicia a saga e guarda o histórico final | MongoDB |
| orquestrador-service | Decide o próximo tópico a partir da origem e do status | nenhum |
| validacao-livro-service | Confere se todos os livros existem no catálogo | PostgreSQL |
| pagamento-service | Calcula os totais, aprova e estorna o pagamento | PostgreSQL |
| estoque-service | Baixa e restaura o estoque de cada livro | PostgreSQL |

---

## Como a saga funciona

### Caminho feliz

```mermaid
sequenceDiagram
    participant C as Cliente
    participant P as pedido-service
    participant O as orquestrador
    participant V as validacao-livro
    participant G as pagamento
    participant E as estoque

    C->>P: POST /api/pedidos
    P->>P: grava pedido e evento no MongoDB
    P-->>C: 201 com id e transacaoId
    P->>O: inicio-saga
    O->>V: validacao-livro-sucesso
    V->>O: orquestrador (SUCESSO)
    O->>G: pagamento-sucesso
    G->>O: orquestrador (SUCESSO)
    O->>E: estoque-sucesso
    E->>O: orquestrador (SUCESSO)
    O->>O: fim-sucesso
    O->>P: notificar-fim
    P->>P: grava o evento final com o histórico completo
```

A resposta do `POST` sai antes da saga terminar. O cliente recebe o pedido criado e acompanha o resultado pelo `GET /api/eventos`. É a consistência eventual na prática: por alguns instantes o pedido existe sem ter sido pago.

### Quando uma etapa falha

O status do evento diz ao orquestrador o que fazer:

| Status | Significado | O orquestrador manda para |
|---|---|---|
| `SUCESSO` | A etapa terminou | a próxima etapa |
| `ROLLBACK_PENDENTE` | A etapa falhou e pode ter deixado algo pela metade | o tópico de falha do **mesmo** serviço |
| `FALHA` | A etapa foi desfeita | o tópico de falha do serviço **anterior** |

Exemplo com 3 unidades de `O_CORTICO`, que só tem 2 em estoque:

```mermaid
sequenceDiagram
    participant O as orquestrador
    participant V as validacao-livro
    participant G as pagamento
    participant E as estoque
    participant P as pedido-service

    Note over V,G: validação e pagamento já aprovados
    O->>E: estoque-sucesso
    E->>O: ROLLBACK_PENDENTE (estoque insuficiente)
    O->>E: estoque-falha
    E->>O: FALHA (estoque restaurado)
    O->>G: pagamento-falha
    G->>O: FALHA (pagamento estornado)
    O->>V: validacao-livro-falha
    V->>O: FALHA (validação marcada como falha)
    O->>O: fim-falha
    O->>P: notificar-fim
```

Cada passo entra no `historico` do evento com a origem, o status e a mensagem, então o `GET /api/eventos` mostra exatamente onde a saga parou e por quê.

### Tabela de transições

É o conteúdo do enum `TransicaoSaga` no orquestrador:

| Origem | Status | Próximo tópico |
|---|---|---|
| `ORQUESTRADOR` | `SUCESSO` | `validacao-livro-sucesso` |
| `ORQUESTRADOR` | `FALHA` | `fim-falha` |
| `VALIDACAO_LIVRO_SERVICE` | `SUCESSO` | `pagamento-sucesso` |
| `VALIDACAO_LIVRO_SERVICE` | `ROLLBACK_PENDENTE` | `validacao-livro-falha` |
| `VALIDACAO_LIVRO_SERVICE` | `FALHA` | `fim-falha` |
| `PAGAMENTO_SERVICE` | `SUCESSO` | `estoque-sucesso` |
| `PAGAMENTO_SERVICE` | `ROLLBACK_PENDENTE` | `pagamento-falha` |
| `PAGAMENTO_SERVICE` | `FALHA` | `validacao-livro-falha` |
| `ESTOQUE_SERVICE` | `SUCESSO` | `fim-sucesso` |
| `ESTOQUE_SERVICE` | `ROLLBACK_PENDENTE` | `estoque-falha` |
| `ESTOQUE_SERVICE` | `FALHA` | `pagamento-falha` |

Antes isso era uma matriz `Object[][]` percorrida por índice e com cast. Virou enum porque o compilador passa a conferir os tipos, e a tabela fica legível do jeito que está acima.

### A mesma transação não é processada duas vezes

O Kafka entrega pelo menos uma vez, então uma mensagem pode chegar repetida. Antes de executar, cada participante confere se já existe registro para o par `pedidoId` + `transacaoId` (`VALIDACAO_DUPLICADA`, `PAGAMENTO_DUPLICADO`, `MOVIMENTACAO_DUPLICADA`). Se existir, a etapa não roda de novo e o participante responde com `ROLLBACK_PENDENTE`. Isso impede cobrar ou baixar o estoque duas vezes, mas tem um custo, descrito em [O que ainda daria pra melhorar](#o-que-ainda-daria-pra-melhorar).

---

## Por que MongoDB no pedido-service

O que o pedido-service guarda é o **evento da saga**: o pedido com os itens, o status atual e o histórico, que cresce a cada etapa. É um documento único, que nasce inteiro, é lido inteiro e é substituído inteiro no fim. Ninguém consulta só um item ou só uma linha do histórico.

```
evento
├── pedido
│   └── itens[]
│       └── livro
└── historico[]
```

Num banco relacional isso viraria quatro tabelas (`evento`, `pedido`, `item_pedido`, `historico`) e um join toda vez que alguém pede o evento, só pra remontar o JSON que já chegou pronto do Kafka. No MongoDB o JSON do evento é gravado como chegou.

Pesou também:

- **O formato do evento muda junto com a saga.** Quando uma etapa nova acrescenta um campo ao evento, o documento aceita sem migration.
- **A gravação final é uma troca do documento inteiro.** O evento volta do orquestrador com o mesmo `id` com que saiu, e o `save` substitui o documento inicial pelo final, com o histórico completo. Uma saga, um documento.
- **As consultas são por chave.** O `GET /api/eventos` busca pelo `pedidoId` ou pelo `transacaoId`, pega o mais recente, e só.

### E por que PostgreSQL nos outros três

Validação, pagamento e estoque guardam dado com regra. O estoque precisa de um número disponível que não pode ficar negativo, e de movimentações ligadas ao estoque por chave estrangeira. O catálogo precisa de código único. O pagamento tem status que muda de `PENDENTE` para `APROVADO` ou `ESTORNADO`. São estruturas fixas, com restrição de integridade, que o banco relacional garante sozinho.

Como cada serviço é dono do próprio banco, essa escolha é por serviço, e não do sistema inteiro.

---

## Estrutura do projeto

Cada serviço é organizado por domínio, no mesmo padrão do `card-service`. Exemplo do pagamento:

```
pagamento-service/src/main/java/com/livraria/pagamento/
├── pagamento/        Pagamento, StatusPagamento, service, consumer,
│                     repository e exceptions/
├── saga/             contrato do evento (Evento, Pedido, ItemPedido, Livro,
│                     Historico, StatusSaga), ConversorJson e o producer
└── shared/
    ├── config/       ClockConfig e criação dos tópicos
    └── exception/    IErroPadrao, ErrorDTO e DefaultExceptionHandler
```

| Serviço | Pacotes de domínio |
|---|---|
| pedido-service | `pedido/`, `evento/` |
| orquestrador-service | `orquestracao/` |
| validacao-livro-service | `catalogo/`, `validacao/` |
| pagamento-service | `pagamento/` |
| estoque-service | `estoque/` |

## Migrations

```
validacao-livro-service
  V1  livro, validacao             →  catálogo com código único e o registro de cada validação
  V2  carga inicial dos livros

pagamento-service
  V1  pagamento

estoque-service
  V1  estoque, movimentacao_estoque  →  movimentação com FK para o estoque
  V2  carga inicial do estoque
```

`ddl-auto=validate` está ligado nos três. O Hibernate não altera estrutura nenhuma, quem faz isso é o Flyway, e campo novo na entidade sem a migration correspondente derruba o serviço na inicialização.

---

## Modelo de dados

**pedido-db (MongoDB)**

- `pedido`: itens, `transacaoId`, data de criação.
- `evento`: o pedido inteiro, origem, status e o histórico de cada etapa.

**validacao-livro-db**

- `livro`: código único e título. É o catálogo.
- `validacao`: `pedido_id`, `transacao_id` e se a validação passou.

**pagamento-db**

- `pagamento`: `pedido_id`, `transacao_id`, quantidade e valor total, status (`PENDENTE`, `APROVADO`, `ESTORNADO`).

**estoque-db**

```
estoque (1) ──── (N) movimentacao_estoque
```

- `estoque`: código do livro e quantidade disponível.
- `movimentacao_estoque`: quantidade pedida, a quantidade anterior e a nova. O rollback usa a anterior para devolver o estoque ao que era.

O `codigo` do livro é o identificador entre os serviços. O catálogo e o estoque não compartilham tabela, e quem liga um ao outro é o código que vem no evento. A busca por ele usa o índice que o PostgreSQL cria para a restrição `UNIQUE`.

---

## Decisões técnicas

### O contrato do evento é copiado em cada serviço

`Evento`, `Pedido`, `ItemPedido`, `Livro` e `Historico` existem no pacote `saga/` de cada serviço. Não virou biblioteca compartilhada de propósito: uma lib em comum acoplaria a versão de deploy de todos os serviços.

O custo é que um campo novo no evento precisa ir para **todos** os serviços que repassam o evento, e não só para quem o usa. O orquestrador e os participantes desserializam e serializam o evento de novo, e o campo que a classe não conhece se perde no caminho.

### As regras ficam no modelo

`Pagamento.aprovar()` recusa valor abaixo do mínimo, `Estoque.baixar()` recusa quantidade maior que a disponível e `Pedido` não nasce sem itens. O service orquestra a etapa e o modelo diz o que pode ou não acontecer, então a regra é testada sem mock nenhum.

### Uma exceção por regra, e a mensagem vai pro histórico

Cada regra tem a sua exceção implementando `IErroPadrao`, que devolve um `ErrorDTO` com código e mensagem. No pedido-service isso vira resposta HTTP pelo `DefaultExceptionHandler`. Nos participantes, que não têm cliente HTTP, a mesma mensagem vai para o histórico do evento: `Falha ao atualizar o estoque: Estoque insuficiente para o livro.`

### O relógio é injetado

Toda data vem de um `Clock` injetado (`ClockConfig`), e os testes usam `Clock.fixed`. As datas do histórico e o `transacaoId` saem previsíveis no teste.

---

## Endpoints

Todos no pedido-service (`localhost:3000`):

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/pedidos` | Cria o pedido e inicia a saga. `201` com id e `transacaoId` |
| GET | `/api/eventos?pedidoId=` | Evento mais recente do pedido |
| GET | `/api/eventos?transacaoId=` | Evento mais recente da transação |
| GET | `/api/eventos/todos` | Todos os eventos, do mais recente para o mais antigo |

Corpo do `POST /api/pedidos`:

```json
{
  "itens": [
    { "livro": { "codigo": "DOM_CASMURRO", "valorUnitario": 39.90 }, "quantidade": 2 },
    { "livro": { "codigo": "IRACEMA", "valorUnitario": 25.00 }, "quantidade": 1 }
  ]
}
```

A resposta do `POST` vem com `valorTotal` e `quantidadeTotal` zerados. Quem calcula os totais é o pagamento, e eles aparecem no evento final:

```json
{
  "id": "64429e9a7a8b646915b37360",
  "transacaoId": "1760263976536_99d2ca6c-f074-41a6-92e0-21700148b519",
  "pedidoId": "64429e987a8b646915b3735f",
  "pedido": {
    "id": "64429e987a8b646915b3735f",
    "itens": [
      { "livro": { "codigo": "DOM_CASMURRO", "valorUnitario": 39.9 }, "quantidade": 2 },
      { "livro": { "codigo": "IRACEMA", "valorUnitario": 25.0 }, "quantidade": 1 }
    ],
    "valorTotal": 104.8,
    "quantidadeTotal": 3
  },
  "origem": "ORQUESTRADOR",
  "status": "SUCESSO",
  "historico": [
    { "origem": "ORQUESTRADOR", "status": "SUCESSO", "mensagem": "Saga iniciada." },
    { "origem": "VALIDACAO_LIVRO_SERVICE", "status": "SUCESSO", "mensagem": "Livros validados com sucesso." },
    { "origem": "PAGAMENTO_SERVICE", "status": "SUCESSO", "mensagem": "Pagamento realizado com sucesso." },
    { "origem": "ESTOQUE_SERVICE", "status": "SUCESSO", "mensagem": "Estoque atualizado com sucesso." },
    { "origem": "ORQUESTRADOR", "status": "SUCESSO", "mensagem": "Saga finalizada com sucesso." }
  ]
}
```

Datas omitidas no exemplo.

### Erros da API

Todo erro sai no mesmo formato:

```json
{ "code": "FILTRO_NAO_INFORMADO", "message": "Informe o pedidoId ou o transacaoId." }
```

| Código | HTTP | Quando |
|---|---|---|
| `PEDIDO_SEM_ITENS` | 400 | Pedido sem nenhum item |
| `FILTRO_NAO_INFORMADO` | 400 | Consulta de evento sem `pedidoId` e sem `transacaoId` |
| `EVENTO_NAO_ENCONTRADO` | 404 | Nenhum evento para o filtro informado |
| `CORPO_INVALIDO` | 400 | JSON malformado |
| `ROTA_NAO_ENCONTRADA` / `METODO_NAO_PERMITIDO` | 404 / 405 | URL ou verbo errado |
| `ERRO_NAO_MAPEADO` | 500 | O que não foi previsto |

### Falhas registradas no histórico da saga

| Código | Serviço | Quando |
|---|---|---|
| `LIVRO_NAO_CADASTRADO` | validação | Código fora do catálogo |
| `LIVRO_NAO_INFORMADO` | validação | Item sem livro ou sem código |
| `PEDIDO_NAO_IDENTIFICADO` | validação | Evento sem id do pedido ou sem transação |
| `VALOR_MINIMO_NAO_ATINGIDO` | pagamento | Total abaixo de R$ 0,10 |
| `PAGAMENTO_NAO_ENCONTRADO` | pagamento | Estorno sem pagamento registrado |
| `ESTOQUE_INSUFICIENTE` | estoque | Quantidade maior que a disponível |
| `ESTOQUE_NAO_ENCONTRADO` | estoque | Livro sem estoque cadastrado |
| `*_DUPLICADA` / `*_DUPLICADO` | todos | Transação já processada |

---

## Configuração

Cada serviço lê as conexões de variável de ambiente, com padrão para desenvolvimento local:

| Variável | Serviços | Padrão |
|---|---|---|
| `KAFKA_BROKER` | todos | `localhost:9092` |
| `MONGO_DB_URI` | pedido | `mongodb://admin:123456@localhost:27017` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | validação, pagamento, estoque | `localhost` / `5432` / nome do banco do serviço |
| `DB_USER` / `DB_PASSWORD` | validação, pagamento, estoque | `postgres` / `postgres` |

No `docker-compose.yml` essas variáveis já apontam para os containers. As senhas versionadas são de desenvolvimento e não valem nada como segredo.

---

## O que ainda daria pra melhorar

**Gravar e publicar não é atômico.** O pedido-service salva no MongoDB e depois publica no Kafka. Se o processo cair entre os dois, fica um pedido sem saga. O mesmo vale para os participantes, que gravam no banco e depois respondem ao orquestrador. O padrão Outbox resolve: gravar o evento numa tabela na mesma transação e publicar a partir dela.

**O estado intermediário não aparece no pedido.** O pedido-service grava o evento no início e no fim. Enquanto a saga roda, o `GET /api/eventos` mostra o evento inicial, sem o histórico das etapas já concluídas.

Além disso:

- **Mensagem repetida derruba a saga.** A checagem de duplicidade protege o dado, mas responde com `ROLLBACK_PENDENTE`, e o rollback desfaz uma etapa que tinha dado certo. O ideal é a etapa repetida devolver o mesmo resultado da primeira vez.
- **Baixa de estoque sem transação.** Num pedido com vários livros, cada item é baixado e salvo separadamente. Se o segundo falhar, o primeiro já foi baixado e depende do rollback da saga para voltar. `@Transactional` na etapa deixaria a baixa toda ou nada.
- **Teste de integração.** Os testes cobrem service e modelo. Testcontainers com Kafka e PostgreSQL provaria a saga de ponta a ponta e as migrations contra um banco de verdade.
- **Mensagem que não converte.** Um JSON inválido no tópico cai no tratamento padrão do Spring Kafka, que tenta de novo e descarta. Um tópico de mensagens mortas (DLT) guardaria essas mensagens para análise.

## Autor

### Carlos Emanuel
### Desenvolvedor de Software Back-End

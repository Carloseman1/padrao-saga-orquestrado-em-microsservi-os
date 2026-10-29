import argparse
import http.cookiejar
import json
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import date, datetime

API = "http://localhost:3000/api"
USUARIOS = "http://localhost:8093/api/usuarios"
CARTOES = "http://localhost:8091/api/cartoes"
CLIENTE = ("cliente@livraria.com", "Cliente@123")
ADMIN = ("admin@livraria.com", "Admin@123")
CARTAO_DE_TESTE = "4111111111111111"
MONGO_EXPRESS = "http://localhost:8082/db/pedido-db"
REDPANDA = "http://localhost:8081/topics"
TEMPO_LIMITE_SEGUNDOS = 30
INTERVALO_SEGUNDOS = 0.5
KAFKA_BIN = "/opt/kafka/bin"
KAFKA_BROKER = "kafka:29092"

NOMES_DOS_SERVICOS = {
    "ORQUESTRADOR": "Orquestrador",
    "VALIDACAO_LIVRO_SERVICE": "Validação de livros",
    "PAGAMENTO_SERVICE": "Pagamento",
    "ESTOQUE_SERVICE": "Estoque",
}

SIGNIFICADO_DOS_STATUS = {
    "SUCESSO": "ok",
    "ROLLBACK_PENDENTE": "falhou",
    "FALHA": "desfeito",
}


def item(codigo, valor, quantidade):
    return {"livro": {"codigo": codigo, "valorUnitario": valor}, "quantidade": quantidade}


CENARIOS = {
    "sucesso": {
        "titulo": "Compra aprovada",
        "explicacao": "1 x Memórias Póstumas de Brás Cubas. O livro existe, o valor é válido e tem estoque.",
        "itens": [item("MEMORIAS_POSTUMAS", 39.90, 1)],
    },
    "estoque": {
        "titulo": "Falha no estoque",
        "explicacao": "3 x O Cortiço, que só tem 2. Validação e pagamento passam, o estoque recusa e tudo é desfeito.",
        "itens": [item("O_CORTICO", 29.90, 3)],
    },
    "pagamento": {
        "titulo": "Falha no pagamento",
        "explicacao": "O cliente tenta pagar com um cartão cadastrado pelo admin. A validação passa, o pagamento recusa e a validação é desfeita.",
        "itens": [item("DOM_CASMURRO", 39.90, 1)],
        "cartao_do_admin": True,
    },
    "livro": {
        "titulo": "Livro fora do catálogo",
        "explicacao": "Código que não existe. A saga para logo na primeira etapa.",
        "itens": [item("LIVRO_INEXISTENTE", 49.90, 1)],
    },
}


def nova_sessao():
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))


def chamar(sessao, metodo, url, corpo=None):
    dados = json.dumps(corpo).encode("utf-8") if corpo else None
    requisicao = urllib.request.Request(
        url, data=dados, method=metodo, headers={"Content-Type": "application/json"})
    with sessao.open(requisicao, timeout=10) as resposta:
        conteudo = resposta.read().decode("utf-8")
        return json.loads(conteudo) if conteudo else None


def entrar(email, senha):
    sessao = nova_sessao()
    chamar(sessao, "POST", f"{USUARIOS}/login", {"email": email, "senha": senha})
    return sessao


def cartao_de_teste(sessao, titular):
    cartoes = chamar(sessao, "GET", CARTOES)
    if cartoes:
        return cartoes[0]["id"]

    cartao = chamar(sessao, "POST", CARTOES, {
        "numero": CARTAO_DE_TESTE,
        "nomeTitular": titular,
        "validadeMes": 12,
        "validadeAno": date.today().year + 3,
        "cvv": "123",
    })
    return cartao["id"]


def criar_pedido(cliente, itens, cartao_id):
    return chamar(cliente, "POST", f"{API}/pedidos", {"itens": itens, "cartaoId": cartao_id})


def saga_terminou(evento):
    return evento.get("origem") == "ORQUESTRADOR" and evento.get("status") in ("SUCESSO", "FALHA") \
        and len(evento.get("historico") or []) > 1


def esperar_fim_da_saga(cliente, pedido_id):
    limite = time.monotonic() + TEMPO_LIMITE_SEGUNDOS

    while time.monotonic() < limite:
        evento = chamar(cliente, "GET", f"{API}/eventos?pedidoId={pedido_id}")
        if saga_terminou(evento):
            return evento
        print(".", end="", flush=True)
        time.sleep(INTERVALO_SEGUNDOS)

    return None


def ler_data(texto):
    data, _, fracao = texto.partition(".")
    return datetime.fromisoformat(f"{data}.{(fracao + '000000')[:6]}")


def mostrar_etapas(evento):
    historico = evento["historico"]
    inicio = ler_data(historico[0]["criadoEm"])

    print()
    for etapa in historico:
        segundos = (ler_data(etapa["criadoEm"]) - inicio).total_seconds()
        servico = NOMES_DOS_SERVICOS.get(etapa["origem"], etapa["origem"])
        status = SIGNIFICADO_DOS_STATUS.get(etapa["status"], etapa["status"])
        tempo = f"+{segundos:.2f}s"
        print(f"  {tempo:>8}  {servico:<20} {status:<9} {etapa['mensagem']}")

    total = (ler_data(historico[-1]["criadoEm"]) - inicio).total_seconds()
    print()
    if evento["status"] == "SUCESSO":
        valor = f"{evento['pedido']['valorTotal']:.2f}".replace(".", ",")
        print(f"  Resultado: compra concluída. Total cobrado: R$ {valor}. Tempo da saga: {total:.2f}s")
    else:
        print(f"  Resultado: compra desfeita, nada ficou cobrado nem baixado. Tempo da saga: {total:.2f}s")


def mostrar_links_do_mongo(evento):
    print("\n  Ver no MongoDB:")
    print(f"    evento: {MONGO_EXPRESS}/evento/%22{evento['id']}%22")
    print(f"    pedido: {MONGO_EXPRESS}/pedido/%22{evento['pedidoId']}%22")


def rodar_cenario(nome, cliente, cartoes):
    cenario = CENARIOS[nome]
    print(f"\n=== {cenario['titulo']} ===")
    print(f"  {cenario['explicacao']}")

    cartao_id = cartoes["admin"] if cenario.get("cartao_do_admin") else cartoes["cliente"]
    pedido = criar_pedido(cliente, cenario["itens"], cartao_id)
    print(f"  Pedido criado: {pedido['id']}  (transação {pedido['transacaoId']})")
    print("  Aguardando a saga terminar", end="", flush=True)

    evento = esperar_fim_da_saga(cliente, pedido["id"])
    if evento is None:
        print(f"\n  A saga não terminou em {TEMPO_LIMITE_SEGUNDOS}s. Veja os logs com: docker compose logs -f")
        return False

    mostrar_etapas(evento)
    mostrar_links_do_mongo(evento)
    return True


def comando_kafka(script, *argumentos, entrada=None):
    return subprocess.run(
        ["docker", "exec", "-i", "kafka", f"{KAFKA_BIN}/{script}", *argumentos],
        input=entrada, capture_output=True, text=True, encoding="utf-8", errors="replace")


def mensagens_no_topico(topico):
    resultado = comando_kafka("kafka-run-class.sh", "kafka.tools.GetOffsetShell",
                              "--broker-list", KAFKA_BROKER, "--topic", topico, "--time", "-1")
    linhas = [linha for linha in resultado.stdout.splitlines() if linha.startswith(topico + ":")]
    return sum(int(linha.rsplit(":", 1)[1]) for linha in linhas)


def ultima_mensagem(topico, posicao):
    resultado = comando_kafka("kafka-console-consumer.sh", "--bootstrap-server", KAFKA_BROKER,
                              "--topic", topico, "--partition", "0", "--offset", str(posicao),
                              "--max-messages", "1", "--timeout-ms", "10000")
    return resultado.stdout.strip()


def rodar_cenario_dlt():
    topico = "estoque-sucesso"
    topico_dlt = topico + ".DLT"
    mensagem_quebrada = "{ isto não é um evento válido"

    print("\n=== Falha técnica indo para a DLT ===")
    print(f"  Uma mensagem com JSON quebrado é enviada direto para o tópico {topico}.")
    print("  O estoque não consegue ler, não tenta de novo (não adiantaria) e manda para a DLT.")

    antes = mensagens_no_topico(topico_dlt)
    print(f"  Mensagens em {topico_dlt} antes: {antes}")

    comando_kafka("kafka-console-producer.sh", "--broker-list", KAFKA_BROKER, "--topic", topico,
                  entrada=mensagem_quebrada + "\n")
    print(f"  Mensagem enviada: {mensagem_quebrada}")
    print("  Aguardando o estoque processar", end="", flush=True)

    limite = time.monotonic() + TEMPO_LIMITE_SEGUNDOS
    depois = antes
    while time.monotonic() < limite and depois == antes:
        print(".", end="", flush=True)
        time.sleep(1)
        depois = mensagens_no_topico(topico_dlt)

    print()
    if depois == antes:
        print(f"  A mensagem não chegou na DLT em {TEMPO_LIMITE_SEGUNDOS}s. Veja: docker logs estoque-service")
        return False

    print(f"  Mensagens em {topico_dlt} depois: {depois}")
    print(f"  Conteúdo guardado na DLT: {ultima_mensagem(topico_dlt, depois - 1)}")
    print("  A fila do estoque continuou andando: a mensagem quebrada não travou as próximas.")
    print(f"\n  Ver no Kafka: {REDPANDA}/{topico_dlt}")
    return True


def main():
    sys.stdout.reconfigure(encoding="utf-8")

    opcoes = list(CENARIOS) + ["dlt"]
    parser = argparse.ArgumentParser(description="Mostra a saga da livraria passo a passo.")
    parser.add_argument("cenarios", nargs="*", choices=opcoes, metavar="cenario",
                        help=f"um ou mais de: {', '.join(opcoes)}. Sem nenhum, roda todos.")
    escolhidos = parser.parse_args().cenarios or opcoes

    try:
        cliente = entrar(*CLIENTE)
        admin = entrar(*ADMIN)
        cartoes = {
            "cliente": cartao_de_teste(cliente, "CLIENTE DEMONSTRACAO"),
            "admin": cartao_de_teste(admin, "ADMIN LIVRARIA"),
        }
    except urllib.error.HTTPError as erro:
        print(f"Falha ao preparar o login e os cartões: {erro.code} em {erro.url}")
        return 1
    except OSError:
        print("O usuario-service ou o pagamento-service não respondeu. Suba o projeto com: py build.py")
        return 1

    print("Cada compra aprovada baixa o estoque de verdade. Para voltar ao estoque inicial, rode py build.py --recriar.")

    resultados = [rodar_cenario_dlt() if nome == "dlt" else rodar_cenario(nome, cliente, cartoes)
                  for nome in escolhidos]
    return 0 if all(resultados) else 1


if __name__ == "__main__":
    sys.exit(main())

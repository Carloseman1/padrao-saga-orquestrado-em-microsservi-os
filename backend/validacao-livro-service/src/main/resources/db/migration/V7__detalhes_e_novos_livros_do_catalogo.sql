ALTER TABLE livro
    ADD COLUMN autor VARCHAR(255),
    ADD COLUMN editora VARCHAR(120),
    ADD COLUMN categoria VARCHAR(40) NOT NULL DEFAULT 'OUTROS',
    ADD COLUMN descricao VARCHAR(1000),
    ADD COLUMN capa_url VARCHAR(500);

UPDATE livro SET autor = 'Machado de Assis', categoria = 'LITERATURA_BRASILEIRA',
    descricao = 'Bentinho revisita a juventude e o casamento com Capitu, e deixa ao leitor a dúvida que atravessa gerações: houve ou não traição?',
    capa_url = 'https://covers.openlibrary.org/b/id/647501-L.jpg'
WHERE codigo = 'DOM_CASMURRO';

UPDATE livro SET autor = 'Aluísio Azevedo', categoria = 'LITERATURA_BRASILEIRA',
    descricao = 'Um cortiço no Rio de Janeiro do século XIX vira personagem principal neste retrato naturalista da ambição e da desigualdade.',
    capa_url = 'https://covers.openlibrary.org/b/id/12370912-L.jpg'
WHERE codigo = 'O_CORTICO';

UPDATE livro SET autor = 'José de Alencar', categoria = 'LITERATURA_BRASILEIRA',
    descricao = 'A lenda do Ceará contada pelo amor entre a indígena Iracema e o colonizador português Martim.',
    capa_url = 'https://covers.openlibrary.org/b/id/2664651-L.jpg'
WHERE codigo = 'IRACEMA';

UPDATE livro SET autor = 'Machado de Assis', categoria = 'LITERATURA_BRASILEIRA',
    descricao = 'Um defunto autor narra a própria vida com ironia e sem pressa, num dos romances que inauguram o realismo no Brasil.',
    capa_url = 'https://covers.openlibrary.org/b/id/123152-L.jpg'
WHERE codigo = 'MEMORIAS_POSTUMAS';

UPDATE livro SET autor = 'José de Alencar', categoria = 'LITERATURA_BRASILEIRA',
    descricao = 'Aventura e romance no interior do Rio de Janeiro colonial, com o indígena Peri e a jovem Ceci.',
    capa_url = 'https://covers.openlibrary.org/b/id/5271115-L.jpg'
WHERE codigo = 'O_GUARANI';

INSERT INTO livro (codigo, titulo, preco, autor, editora, categoria, descricao, capa_url) VALUES
    ('DESIGNING_DATA_INTENSIVE_APPLICATIONS', 'Designing Data-Intensive Applications', 389.90,
     'Martin Kleppmann', 'O''Reilly Media', 'TECNOLOGIA',
     'Os fundamentos por trás de bancos de dados, filas e sistemas distribuídos: replicação, particionamento, consistência e processamento de streams.',
     'https://covers.openlibrary.org/b/id/8434671-L.jpg'),
    ('BUILDING_MICROSERVICES', 'Building Microservices', 329.90,
     'Sam Newman', 'O''Reilly Media', 'TECNOLOGIA',
     'Como desenhar, integrar, testar e operar microsserviços, incluindo sagas, comunicação assíncrona e a divisão de um monólito.',
     'https://covers.openlibrary.org/b/id/8513315-L.jpg'),
    ('FUNDAMENTALS_OF_SOFTWARE_ARCHITECTURE', 'Fundamentals of Software Architecture', 299.90,
     'Mark Richards e Neal Ford', 'O''Reilly Media', 'TECNOLOGIA',
     'Estilos de arquitetura, características de qualidade e as escolhas que um arquiteto precisa justificar no dia a dia.',
     'https://covers.openlibrary.org/b/id/10193363-L.jpg'),
    ('KAFKA_THE_DEFINITIVE_GUIDE', 'Kafka: The Definitive Guide', 289.90,
     'Gwen Shapira, Todd Palino, Rajini Sivaram e Krit Petty', 'O''Reilly Media', 'TECNOLOGIA',
     'Produtores, consumidores, partições e garantias de entrega do Apache Kafka, do primeiro tópico até a operação em produção.',
     'https://covers.openlibrary.org/b/id/8509893-L.jpg'),
    ('LEARNING_REACT', 'Learning React', 249.90,
     'Alex Banks e Eve Porcello', 'O''Reilly Media', 'TECNOLOGIA',
     'React moderno com componentes funcionais, hooks e boas práticas para construir interfaces do zero.',
     'https://covers.openlibrary.org/b/id/10282783-L.jpg'),
    ('HEAD_FIRST_DESIGN_PATTERNS', 'Head First Design Patterns', 279.90,
     'Eric Freeman e Elisabeth Robson', 'O''Reilly Media', 'TECNOLOGIA',
     'Os padrões de projeto mais usados explicados com exemplos visuais e exercícios, do Strategy ao Observer.',
     'https://covers.openlibrary.org/b/id/388950-L.jpg'),
    ('VIDAS_SECAS', 'Vidas Secas', 44.90,
     'Graciliano Ramos', NULL, 'LITERATURA_BRASILEIRA',
     'Fabiano, Sinhá Vitória, os filhos e a cachorra Baleia atravessam a seca do sertão nordestino em busca de sobrevivência.',
     'https://covers.openlibrary.org/b/id/14158825-L.jpg'),
    ('A_HORA_DA_ESTRELA', 'A Hora da Estrela', 39.90,
     'Clarice Lispector', NULL, 'LITERATURA_BRASILEIRA',
     'A história de Macabéa, nordestina no Rio de Janeiro, contada por um narrador que questiona o próprio ato de escrever.',
     'https://covers.openlibrary.org/b/id/650866-L.jpg'),
    ('GRANDE_SERTAO_VEREDAS', 'Grande Sertão: Veredas', 89.90,
     'João Guimarães Rosa', NULL, 'LITERATURA_BRASILEIRA',
     'O jagunço Riobaldo relembra batalhas, pactos e o amor por Diadorim numa linguagem que reinventa o português.',
     'https://covers.openlibrary.org/b/id/13909068-L.jpg'),
    ('SAPIENS', 'Sapiens: Uma Breve História da Humanidade', 69.90,
     'Yuval Noah Harari', NULL, 'HISTORIA',
     'Da revolução cognitiva à científica, como o Homo sapiens passou a dominar o planeta.',
     'https://covers.openlibrary.org/b/id/8634250-L.jpg'),
    ('ARMAS_GERMES_E_ACO', 'Armas, Germes e Aço', 89.90,
     'Jared Diamond', NULL, 'HISTORIA',
     'Por que algumas sociedades conquistaram outras: geografia, agricultura e doenças na formação do mundo moderno.',
     'https://covers.openlibrary.org/b/id/7884018-L.jpg'),
    ('LIVRO_1808', '1808', 59.90,
     'Laurentino Gomes', NULL, 'HISTORIA',
     'A fuga da corte portuguesa para o Brasil e como ela mudou para sempre a história do país.',
     'https://covers.openlibrary.org/b/id/9632319-L.jpg'),
    ('BRASIL_UMA_BIOGRAFIA', 'Brasil: Uma Biografia', 99.90,
     'Lilia M. Schwarcz e Heloisa M. Starling', NULL, 'HISTORIA',
     'Cinco séculos de história do Brasil contados a partir das pessoas, das ideias e dos conflitos que formaram o país.',
     'https://covers.openlibrary.org/b/id/14550488-L.jpg'),
    ('A_ERA_DOS_EXTREMOS', 'A Era dos Extremos', 94.90,
     'Eric Hobsbawm', NULL, 'HISTORIA',
     'O breve século XX, das guerras mundiais ao fim da União Soviética, na visão de um dos maiores historiadores do período.',
     'https://covers.openlibrary.org/b/id/456406-L.jpg');

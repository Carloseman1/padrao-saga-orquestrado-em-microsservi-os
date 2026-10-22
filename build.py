import argparse
import socket
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

RAIZ = Path(__file__).resolve().parent

SERVICOS = [
    "pedido-service",
    "orquestrador-service",
    "validacao-livro-service",
    "pagamento-service",
    "estoque-service",
]

PORTAS = {
    3000: "pedido-service",
    8080: "orquestrador-service",
    8081: "redpanda-console",
    8082: "mongo-express",
    8090: "validacao-livro-service",
    8091: "pagamento-service",
    8092: "estoque-service",
    9092: "kafka",
    5432: "validacao-livro-db",
    5433: "pagamento-db",
    5434: "estoque-db",
    5435: "usuario-db",
    27017: "pedido-db",
}

TEMPO_LIMITE_SEGUNDOS = 300
INTERVALO_SEGUNDOS = 5


def executar(comando, pasta=RAIZ, capturar=False):
    return subprocess.run(comando, cwd=pasta, capture_output=capturar, text=True, encoding="utf-8", errors="replace")


def docker_disponivel():
    return executar(["docker", "info"], capturar=True).returncode == 0


def porta_ocupada(porta):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as conexao:
        conexao.settimeout(0.5)
        return conexao.connect_ex(("127.0.0.1", porta)) == 0


def testar(servico):
    pasta = RAIZ / servico
    wrapper = pasta / ("mvnw.cmd" if sys.platform == "win32" else "mvnw")
    resultado = executar([str(wrapper), "-q", "-B", "test"], pasta=pasta, capturar=True)
    return servico, resultado.returncode == 0, resultado.stdout + resultado.stderr


def rodar_testes():
    print("Rodando os testes dos serviços...")
    with ThreadPoolExecutor(max_workers=len(SERVICOS)) as executor:
        resultados = list(executor.map(testar, SERVICOS))

    falhas = [(servico, saida) for servico, passou, saida in resultados if not passou]

    for servico, passou, _ in resultados:
        print(f"  {'OK   ' if passou else 'FALHA'} {servico}")

    for servico, saida in falhas:
        print(f"\n--- {servico} ---\n{saida[-3000:]}")

    return not falhas


def avisar_portas_ocupadas():
    ocupadas = [porta for porta in PORTAS if porta_ocupada(porta)]

    for porta in ocupadas:
        print(f"  Atenção: a porta {porta} ({PORTAS[porta]}) já está em uso por outro processo.")

    return ocupadas


def status_do_servico(servico):
    logs = executar(["docker", "logs", servico], capturar=True)
    saida = logs.stdout + logs.stderr

    if "APPLICATION FAILED TO START" in saida:
        return "falhou"
    if "Started " in saida:
        return "no ar"
    return "subindo"


def aguardar_servicos():
    print("Aguardando os serviços iniciarem...")
    limite = time.monotonic() + TEMPO_LIMITE_SEGUNDOS
    pendentes = set(SERVICOS)
    falharam = []

    while pendentes and time.monotonic() < limite:
        for servico in sorted(pendentes):
            status = status_do_servico(servico)
            if status == "subindo":
                continue
            pendentes.discard(servico)
            print(f"  {status.upper():6} {servico}")
            if status == "falhou":
                falharam.append(servico)
        if pendentes:
            time.sleep(INTERVALO_SEGUNDOS)

    for servico in sorted(pendentes):
        print(f"  TEMPO ESGOTADO {servico}")

    return not falharam and not pendentes, falharam + sorted(pendentes)


def subir(com_testes):
    if com_testes and not rodar_testes():
        print("\nTestes falharam. Nada foi iniciado.")
        return 1

    print("Derrubando os containers deste projeto...")
    executar(["docker", "compose", "down"])

    if avisar_portas_ocupadas():
        print("  Feche esses processos se algum serviço não subir.\n")

    print("Construindo as imagens e subindo os containers...")
    if executar(["docker", "compose", "up", "--build", "-d"]).returncode != 0:
        print("\nFalha no docker compose.")
        return 1

    tudo_no_ar, com_problema = aguardar_servicos()

    if not tudo_no_ar:
        print("\nVeja o log de quem não subiu:")
        for servico in com_problema:
            print(f"  docker logs {servico}")
        return 1

    print("\nTudo no ar.")
    print("  Swagger do pedido-service: http://localhost:3000/swagger-ui.html")
    print("  Redpanda Console:          http://localhost:8081")
    print("  MongoDB (mongo-express):   http://localhost:8082")
    print("  Para ver a saga rodando:   py demo.py")
    return 0


def parar():
    print("Derrubando os containers deste projeto...")
    return executar(["docker", "compose", "down"]).returncode


def main():
    parser = argparse.ArgumentParser(description="Sobe ou derruba a saga da livraria no Docker.")
    parser.add_argument("--testes", action="store_true", help="roda os testes antes de subir")
    parser.add_argument("--parar", action="store_true", help="derruba os containers e sai")
    argumentos = parser.parse_args()

    if not docker_disponivel():
        print("O Docker não está respondendo. Abra o Docker Desktop e tente de novo.")
        return 1

    if argumentos.parar:
        return parar()

    return subir(argumentos.testes)


if __name__ == "__main__":
    sys.exit(main())

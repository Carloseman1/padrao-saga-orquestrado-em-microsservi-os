export class ErroApi extends Error {
    readonly status: number;
    readonly codigo: string;

    constructor(status: number, codigo: string, mensagem: string) {
        super(mensagem)
        this.status = status;
        this.codigo = codigo;
    }
}

type Metodo = 'GET' | 'POST' | 'DELETE';

export async function chamarApi<T>(metodo: Metodo, caminho: string, corpo?: unknown): Promise<T> {

    const resposta = await fetch(caminho, {
        method: metodo,
        credentials: 'same-origin',
        headers: corpo === undefined ? undefined : { 'Content-Type': 'application/json' },
        body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })

    if (!resposta.ok) {
        throw await lerErro(resposta);
    }

    if (resposta.status == 204) {
        return undefined as T;
    }

    return (await resposta.json()) as T;

}

async function lerErro(resposta: Response): Promise<ErroApi> {

    try {
        const erro = (await resposta.json()) as { code?: string, message?: string }
        return new ErroApi(resposta.status, erro.code ?? 'ERRO', erro.message ?? 'Algo deu errado. Tente de novo.');

    } catch {
        return new ErroApi(resposta.status, 'ERRO', 'Não foi possível falar com o servidor. Tente de novo.');
    }

}
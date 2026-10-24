package com.livraria.usuario.endereco;

import org.springframework.stereotype.Service;

import com.livraria.usuario.endereco.exceptions.CepNaoEncontradoException;
import com.livraria.usuario.integracao.viacep.ViaCepClient;
import com.livraria.usuario.integracao.viacep.ViaCepResponse;
import com.livraria.usuario.integracao.viacep.exceptions.ViaCepRespostaInesperadaException;

@Service
public class EnderecoService {

    private final ViaCepClient viaCepClient;

    public EnderecoService(ViaCepClient viaCepClient){
        this.viaCepClient = viaCepClient;
    }

    public Endereco buscar(String cepInformado){
        Cep cep = new Cep(cepInformado);

        ViaCepResponse resposta = viaCepClient.consultar(cep.valor());

        if(resposta == null || resposta.naoEncontrado()){
            throw new CepNaoEncontradoException();
        }

        if(!resposta.temCidadeEEstado()){
            throw new ViaCepRespostaInesperadaException();
        }

        return Endereco.doCep(
                cep.valor(),
                resposta.logradouro(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf());
    }
}

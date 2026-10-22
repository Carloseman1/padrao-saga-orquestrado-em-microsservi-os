package com.livraria.usuario.endereco;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/endereco")
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService){
        this.enderecoService = enderecoService;
    }

    @GetMapping("/{cep}")
    public Endereco buscar(@PathVariable String cep){
        return  enderecoService.buscar(cep);
    }
}

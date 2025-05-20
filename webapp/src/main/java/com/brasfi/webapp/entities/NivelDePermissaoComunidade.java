package com.brasfi.webapp.entities;

public enum NivelDePermissaoComunidade {
    PUBLICA(1, "Publica"),
    APENAS_LIDERES(2, "Apenas para lideres"),
    PERSONALIZADA(3, "Personlizada");

    private int codigo;
    private String descricaoDeAcesso;


    private NivelDePermissaoComunidade(int i, String s) {
    }
}

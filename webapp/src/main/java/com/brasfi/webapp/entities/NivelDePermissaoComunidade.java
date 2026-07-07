package com.brasfi.webapp.entities;

public enum NivelDePermissaoComunidade {
    PUBLICA(1, "Pública"),
    APENAS_LIDERES(2, "Apenas para líderes"),
    PERSONALIZADA(3, "Personalizada");

    private int codigo;
    private String descricaoDeAcesso;

    public String getDescricaoDeAcesso() {
        return descricaoDeAcesso;
    }

    public int getCodigo() {
        return codigo;
    }

    private NivelDePermissaoComunidade(int i, String s) {
        this.codigo = i;
        this.descricaoDeAcesso = s;
    }
}

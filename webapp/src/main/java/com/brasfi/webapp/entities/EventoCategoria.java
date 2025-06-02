package com.brasfi.webapp.entities;

public enum EventoCategoria {
    PALESTRA("Palestra"),
    WORKSHOP("Workshop"),
    AULA("Aula");

    private final String displayName;

    EventoCategoria(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
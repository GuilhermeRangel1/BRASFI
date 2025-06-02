package com.brasfi.webapp.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Evento implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private LocalDate dataEvento;

    @Column(nullable = false, length = 1000)
    private String convidados;

    @Column(nullable = false, length = 1500)
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventoCategoria categoria;

    @Column(length = 500, nullable =false) 
    private String urlVideo;

    public Evento() {
    }

    public Evento(Long id, String titulo, LocalDate dataEvento, String convidados, String conteudo, EventoCategoria categoria, String urlVideo) {
        this.id = id;
        this.titulo = titulo;
        this.dataEvento = dataEvento;
        this.convidados = convidados;
        this.conteudo = conteudo;
        this.categoria = categoria;
        this.urlVideo = urlVideo; 
    }

    public Long getId() {
        return id;
    }
	public void setId(Long id) {
		this.id = id;
	}

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDate dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getConvidados() {
        return convidados;
    }

    public void setConvidados(String convidados) {
        this.convidados = convidados;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public EventoCategoria getCategoria() {
        return categoria;
    }

    public void setCategoria(EventoCategoria categoria) {
        this.categoria = categoria;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evento)) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", dataEvento=" + dataEvento +
                ", convidados=" + convidados +
                ", conteudo='" + conteudo + '\'' +
                ", categoria=" + categoria +
                ", urlVideo='" + urlVideo + '\'' + 
                '}';
    }

}
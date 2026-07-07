package com.brasfi.webapp.dto;

import com.brasfi.webapp.entities.Administrador;
import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.Gerente;
import com.brasfi.webapp.entities.Post;
import com.brasfi.webapp.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record ErrorResponse(String message) {
    }

    public record UserResponse(Long id, String name, String email, int idade, String role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getIdade(), roleOf(user));
        }
    }

    public record AuthResponse(boolean authenticated, UserResponse user) {
    }

    public record RegisterRequest(String nome, String email, String cpf, String senha, Integer idade) {
    }

    public record EventRequest(
            String titulo,
            LocalDate dataEvento,
            String convidados,
            String conteudo,
            String categoria,
            String urlVideo
    ) {
    }

    public record EventResponse(
            Long id,
            String titulo,
            LocalDate dataEvento,
            String convidados,
            String conteudo,
            String categoria,
            String categoriaLabel,
            String urlVideo,
            boolean futuro
    ) {
        public static EventResponse from(Evento evento) {
            return new EventResponse(
                    evento.getId(),
                    evento.getTitulo(),
                    evento.getDataEvento(),
                    evento.getConvidados(),
                    evento.getConteudo(),
                    evento.getCategoria().name(),
                    evento.getCategoria().getDisplayName(),
                    evento.getUrlVideo(),
                    !evento.getDataEvento().isBefore(LocalDate.now())
            );
        }
    }

    public record CategoryResponse(String value, String label) {
    }

    public record CommunityRequest(String nome, String descricao, String nivelDePermissao) {
    }

    public record CommunityResponse(
            Long id,
            String nome,
            String descricao,
            String nivelDePermissao,
            String nivelLabel,
            int totalPosts,
            int totalMembros
    ) {
        public static CommunityResponse from(Comunidade comunidade) {
            return new CommunityResponse(
                    comunidade.getId(),
                    comunidade.getNome(),
                    comunidade.getDescricao(),
                    comunidade.getNivelDePermissao().name(),
                    comunidade.getNivelDePermissao().getDescricaoDeAcesso(),
                    comunidade.getPosts() == null ? 0 : comunidade.getPosts().size(),
                    comunidade.getUsuarios() == null ? 0 : comunidade.getUsuarios().size()
            );
        }
    }

    public record PostRequest(String mensagem) {
    }

    public record PostResponse(
            Long id,
            String titulo,
            String mensagem,
            LocalDateTime dataCriacao,
            Long autorId,
            String autorNome
    ) {
        public static PostResponse from(Post post) {
            User autor = post.getAutor();
            return new PostResponse(
                    post.getId(),
                    post.getTitulo(),
                    post.getDescricao(),
                    post.getDataCriacao(),
                    autor == null ? null : autor.getId(),
                    autor == null ? "BRASFI" : autor.getName()
            );
        }
    }

    public record DashboardResponse(
            long totalEventos,
            long eventosFuturos,
            long eventosGravados,
            long totalComunidades,
            List<EventResponse> proximosEventos,
            List<CommunityResponse> comunidades
    ) {
    }

    public record LearningStepResponse(String title, String description, String action) {
    }

    public record LearningTrackResponse(
            String id,
            String title,
            String level,
            String duration,
            String description,
            List<String> outcomes,
            List<LearningStepResponse> steps,
            List<String> resources,
            List<EventResponse> recommendedEvents,
            List<CommunityResponse> recommendedCommunities
    ) {
    }

    public static String roleOf(User user) {
        if (user instanceof Administrador) {
            return "ADMIN";
        }
        if (user instanceof Gerente) {
            return "MANAGER";
        }
        return "USER";
    }
}

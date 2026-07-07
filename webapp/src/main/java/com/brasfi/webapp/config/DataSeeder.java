package com.brasfi.webapp.config;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.EventoCategoria;
import com.brasfi.webapp.entities.NivelDePermissaoComunidade;
import com.brasfi.webapp.entities.Post;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.repositories.EventoRepository;
import com.brasfi.webapp.repositories.UserRepository;
import com.brasfi.webapp.service.PostService;
import com.brasfi.webapp.service.UserService;
import com.brasfi.webapp.service.LearningTrackService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final EventoRepository eventoRepository;
    private final ComunidadeRepository comunidadeRepository;
    private final UserService userService;
    private final PostService postService;
    private final LearningTrackService learningTrackService;

    public DataSeeder(
            UserRepository userRepository,
            EventoRepository eventoRepository,
            ComunidadeRepository comunidadeRepository,
            UserService userService,
            PostService postService,
            LearningTrackService learningTrackService
    ) {
        this.userRepository = userRepository;
        this.eventoRepository = eventoRepository;
        this.comunidadeRepository = comunidadeRepository;
        this.userService = userService;
        this.postService = postService;
        this.learningTrackService = learningTrackService;
    }

    @Override
    public void run(String... args) {
        boolean seedEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("SEED_DEMO_DATA", "true"));
        if (!seedEnabled) {
            return;
        }

        String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@brasfi.com");
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");
        String adminCpf = System.getenv().getOrDefault("ADMIN_CPF", "00000000000");
        User admin = userRepository.findByEmail(adminEmail);
        if (admin == null) {
            admin = userService.registerAdmin("Administrador BRASFI", adminEmail, adminCpf, adminPassword, 30);
        }

        if (eventoRepository.count() == 0) {
            eventoRepository.save(new Evento(
                    null,
                    "Finanças sustentáveis na prática",
                    LocalDate.now().plusDays(10),
                    "Especialistas BRASFI e convidados do mercado",
                    "Uma conversa aplicada sobre investimentos responsáveis, indicadores ESG e tomada de decisão.",
                    EventoCategoria.PALESTRA,
                    "https://www.youtube.com/"
            ));
            eventoRepository.save(new Evento(
                    null,
                    "Workshop de indicadores ESG",
                    LocalDate.now().plusDays(24),
                    "Comunidade acadêmica e profissionais de dados",
                    "Atividade para estruturar métricas, comparar critérios e transformar pesquisa em solução.",
                    EventoCategoria.WORKSHOP,
                    "https://www.youtube.com/"
            ));
            eventoRepository.save(new Evento(
                    null,
                    "Aula aberta: mercado de carbono",
                    LocalDate.now().minusDays(20),
                    "Rede BRASFI",
                    "Gravação introdutória sobre precificação, regulação e oportunidades ligadas ao mercado de carbono.",
                    EventoCategoria.AULA,
                    "https://www.youtube.com/"
            ));
        }

        if (comunidadeRepository.count() == 0) {
            Comunidade pesquisa = comunidadeRepository.save(new Comunidade(
                    "Pesquisa e Inovação",
                    "Espaço para conectar pesquisadores, projetos e referências em finanças sustentáveis.",
                    NivelDePermissaoComunidade.PUBLICA,
                    null
            ));
            Comunidade mercado = comunidadeRepository.save(new Comunidade(
                    "Mercado e Carreiras",
                    "Comunidade para oportunidades, debates de carreira e pontes com organizações parceiras.",
                    NivelDePermissaoComunidade.PUBLICA,
                    null
            ));

            postService.incluirPost(new Post(null, "Boas-vindas", "Compartilhe aqui pesquisas, eventos e perguntas para a rede.", 0, null, pesquisa, admin));
            postService.incluirPost(new Post(null, "Conexões", "Use este espaço para divulgar oportunidades e colaborar com outros membros.", 0, null, mercado, admin));
        }

        learningTrackService.criarTrilhaInicialSeNecessario();
    }
}

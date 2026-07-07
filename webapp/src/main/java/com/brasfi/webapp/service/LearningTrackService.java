package com.brasfi.webapp.service;

import com.brasfi.webapp.dto.ApiDtos.CommunityResponse;
import com.brasfi.webapp.dto.ApiDtos.EventResponse;
import com.brasfi.webapp.dto.ApiDtos.LearningStepResponse;
import com.brasfi.webapp.dto.ApiDtos.LearningTrackResponse;
import com.brasfi.webapp.entities.Evento;
import com.brasfi.webapp.entities.LearningTrack;
import com.brasfi.webapp.entities.LearningTrackStep;
import com.brasfi.webapp.repositories.LearningTrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LearningTrackService {
    private final LearningTrackRepository learningTrackRepository;
    private final EventoService eventoService;
    private final ComunidadeService comunidadeService;

    public LearningTrackService(
            LearningTrackRepository learningTrackRepository,
            EventoService eventoService,
            ComunidadeService comunidadeService
    ) {
        this.learningTrackRepository = learningTrackRepository;
        this.eventoService = eventoService;
        this.comunidadeService = comunidadeService;
    }

    @Transactional(readOnly = true)
    public List<LearningTrackResponse> listarTrilhas() {
        List<EventResponse> recommendedEvents = eventoService.listarEventosAtuaisOuFuturos()
                .stream()
                .sorted(Comparator.comparing(Evento::getDataEvento))
                .limit(2)
                .map(EventResponse::from)
                .toList();

        List<CommunityResponse> recommendedCommunities = comunidadeService.listarTodasComunidades()
                .stream()
                .limit(2)
                .map(CommunityResponse::from)
                .toList();

        return learningTrackRepository.findAllByOrderByIdAsc()
                .stream()
                .map(track -> toResponse(track, recommendedEvents, recommendedCommunities))
                .toList();
    }

    @Transactional
    public void criarTrilhaInicialSeNecessario() {
        if (learningTrackRepository.findBySlug("financas-sustentaveis").isPresent()) {
            return;
        }

        LearningTrack track = new LearningTrack(
                "financas-sustentaveis",
                "Fundamentos de finanças sustentáveis",
                "Iniciante",
                "4 semanas",
                "Uma trilha guiada para entender conceitos ESG, mercado de carbono, investimento responsável e caminhos de atuação na rede BRASFI."
        );

        track.setOutcomes(List.of(
                "Entender os conceitos centrais",
                "Conectar teoria com casos reais",
                "Entrar nas comunidades certas"
        ));
        track.setResources(List.of(
                "Glossário ESG e finanças sustentáveis",
                "Checklist de análise de oportunidade",
                "Roteiro para estudo de caso"
        ));
        track.addStep(new LearningTrackStep(
                "Base conceitual",
                "Comece pelos termos essenciais: ESG, impacto, materialidade e risco climático.",
                "Assistir aula introdutória"
        ));
        track.addStep(new LearningTrackStep(
                "Aplicação no mercado",
                "Veja como indicadores e métricas apoiam decisões de investimento e gestão.",
                "Participar de workshop"
        ));
        track.addStep(new LearningTrackStep(
                "Discussão em comunidade",
                "Leve dúvidas e referências para uma comunidade ativa da plataforma.",
                "Publicar uma pergunta"
        ));
        track.addStep(new LearningTrackStep(
                "Projeto prático",
                "Organize um pequeno estudo de caso para consolidar aprendizado e colaboração.",
                "Montar plano de ação"
        ));

        learningTrackRepository.save(track);
    }

    private LearningTrackResponse toResponse(
            LearningTrack track,
            List<EventResponse> recommendedEvents,
            List<CommunityResponse> recommendedCommunities
    ) {
        return new LearningTrackResponse(
                track.getSlug(),
                track.getTitle(),
                track.getLevel(),
                track.getDuration(),
                track.getDescription(),
                track.getOutcomes(),
                track.getSteps()
                        .stream()
                        .map(step -> new LearningStepResponse(step.getTitle(), step.getDescription(), step.getAction()))
                        .toList(),
                track.getResources(),
                recommendedEvents,
                recommendedCommunities
        );
    }
}

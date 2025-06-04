package com.brasfi.webapp.controller;

import com.brasfi.webapp.entities.Comunidade;
import com.brasfi.webapp.entities.Solicitacao;
import com.brasfi.webapp.entities.User;
import com.brasfi.webapp.repositories.ComunidadeRepository;
import com.brasfi.webapp.repositories.SolicitacaoRepository;
import com.brasfi.webapp.security.CustomUserDetails;
import org.springframework.boot.Banner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.Transient;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class SolicitacaoController {

    SolicitacaoRepository solicitacaoRepository;
    ComunidadeRepository comunidadeRepository;

    public SolicitacaoController(SolicitacaoRepository solicitacaoRepository, ComunidadeRepository comunidadeRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.comunidadeRepository = comunidadeRepository;
    }

    @PostMapping("/criar-solicitacao")
    public ModelAndView criarSolicitacao(
            @RequestParam("comunidadeId-solicitada") Long comunidadeId,
            @RequestParam("solicitacao-usuario") String conteudoSolicitacao,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes // Used for flash attributes
    ) {
        // Ensure a user is logged in to create a solicitation
        if (currentUser == null || currentUser.getUserEntity() == null) {
            redirectAttributes.addFlashAttribute("erroMensagem", "Você precisa estar logado para enviar uma solicitação.");
            return new ModelAndView("redirect:/login"); // Redirect to login page
        }

        User solicitante = currentUser.getUserEntity();
        Optional<Comunidade> comunidadeOptional = comunidadeRepository.findById(comunidadeId);

        if (comunidadeOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("erroMensagem", "Comunidade solicitada não encontrada.");
            return new ModelAndView("redirect:/"); // Redirect to home or an error page
        }

        Comunidade comunidadeSolicitada = comunidadeOptional.get();

        // Create the new Solicitacao object
        Solicitacao novaSolicitacao = new Solicitacao();
        novaSolicitacao.setUsuarioSolicitante(solicitante);
        novaSolicitacao.setComunidadeSolicitada(comunidadeSolicitada);
        novaSolicitacao.setDataDeSolicitacao(LocalDate.now()); // Set current date
        novaSolicitacao.setConteudo(conteudoSolicitacao);
        // You might want to set an initial status, e.g., SolicitacaoStatus.PENDENTE
        // novaSolicitacao.setStatus(SolicitacaoStatus.PENDENTE);

        solicitacaoRepository.save(novaSolicitacao);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Sua solicitação foi enviada com sucesso!");
        return new ModelAndView("redirect:/"); // Redirect to the home page after submission
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/listar-solicitacoes")
    public ModelAndView listarSolicitacoes() {
        ModelAndView mv = new ModelAndView("lista_de_solicitacoes");

        List<Solicitacao> todasAsSolicitacoes = solicitacaoRepository.findAll();

        mv.addObject("solicitacoes", todasAsSolicitacoes);
        return mv;
    }

    @PostMapping("/processar-solicitacao-acao")
    public ModelAndView processarSolicitacaoAcao(
            @RequestParam("solicitacaoId") Long solicitacaoId
    ) {
        ModelAndView mv = new ModelAndView("lista_de_solicitacoes"); // Stay on the same page

        // Find the solicitation by ID
        solicitacaoRepository.findById(solicitacaoId).ifPresentOrElse(
                solicitacao -> {
                    // Add the found solicitation to the model
                    mv.addObject("solicitacaoSelecionada", solicitacao);
                    // Set a flag to indicate that the pop-up should be shown
                    mv.addObject("mostrarPopup", true);
                },
                () -> {
                    // Handle case where solicitation is not found (e.g., add an error message)
                    mv.addObject("erroMensagem", "Solicitação não encontrada.");
                    mv.addObject("mostrarPopup", false); // Ensure pop-up doesn't show
                }
        );

        // Re-add all solicitations to the model so the list remains visible behind the popup
        List<Solicitacao> todasAsSolicitacoes = solicitacaoRepository.findAll();
        mv.addObject("solicitacoes", todasAsSolicitacoes);

        return mv;
    }

    @PostMapping("/adicionar-usuario-na-comunidade")
    @Transactional
    public ModelAndView adicionarUsuarioNaComunidade(
            @RequestParam("solicitacaoId") Long solicitacaoId
    )
    {
        return solicitacaoRepository.findById(solicitacaoId).map(solicitacao -> {
            User user = solicitacao.getUsuarioSolicitante();
            Comunidade comunidade = solicitacao.getComunidadeSolicitada();
            comunidade.getUsuarios().add(user);
            comunidadeRepository.save(comunidade);

            return new ModelAndView("redirect:/");
        }).orElseGet(() -> {
            return new ModelAndView("redirect:/listar-solicitacoes?error=solicitacaoNaoEncontradaParaAdicionar");
        });

    }
}
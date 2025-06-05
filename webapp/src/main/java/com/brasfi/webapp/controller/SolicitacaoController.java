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
import org.springframework.ui.Model;
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
            RedirectAttributes redirectAttributes 
    ) {
        ModelAndView mv = new ModelAndView("comunidades_hub");

        if (currentUser == null || currentUser.getUserEntity() == null) {
            redirectAttributes.addFlashAttribute("erroMensagem", "Você precisa estar logado para enviar uma solicitação.");
            return new ModelAndView("redirect:/login");
        }

        User solicitante = currentUser.getUserEntity();
        Optional<Comunidade> comunidadeOptional = comunidadeRepository.findById(comunidadeId);

        if (comunidadeOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("erroMensagem", "Comunidade solicitada não encontrada.");
            return new ModelAndView("redirect:/comunidades"); 
        }

        Comunidade comunidadeSolicitada = comunidadeOptional.get();

        Solicitacao novaSolicitacao = new Solicitacao();
        novaSolicitacao.setUsuarioSolicitante(solicitante);
        novaSolicitacao.setComunidadeSolicitada(comunidadeSolicitada);
        novaSolicitacao.setDataDeSolicitacao(LocalDate.now()); 
        novaSolicitacao.setConteudo(conteudoSolicitacao);

        solicitacaoRepository.save(novaSolicitacao);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Sua solicitação foi enviada com sucesso!");
        return new ModelAndView("redirect:/comunidades/1");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
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
        ModelAndView mv = new ModelAndView("lista_de_solicitacoes"); 

        solicitacaoRepository.findById(solicitacaoId).ifPresentOrElse(
                solicitacao -> {
                    mv.addObject("solicitacaoSelecionada", solicitacao);
                    mv.addObject("mostrarPopup", true);
                },
                () -> {
                    mv.addObject("erroMensagem", "Solicitação não encontrada.");
                    mv.addObject("mostrarPopup", false); 
                }
        );

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
        ModelAndView mv = new ModelAndView("lista_de_solicitacoes");
        return solicitacaoRepository.findById(solicitacaoId).map(solicitacao -> {
            User user = solicitacao.getUsuarioSolicitante();
            Comunidade comunidade = solicitacao.getComunidadeSolicitada();
            comunidade.getUsuarios().add(user);
            comunidadeRepository.save(comunidade);
            solicitacaoRepository.delete(solicitacao);

            mv.addObject("solicitacoes", solicitacaoRepository.findAll());
            return mv;
        }).orElseGet(() -> {
            return new ModelAndView("redirect:/listar-solicitacoes?error=solicitacaoNaoEncontradaParaAdicionar");
        });

    }

    @PostMapping("/excluir-solicitacao")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ModelAndView excluirSolicitacao(
            @RequestParam("solicitacaoId") Long solicitacaoId,
            RedirectAttributes redirectAttributes
    ) {
        ModelAndView mv = new ModelAndView("lista_de_solicitacoes");
        mv.addObject("solicitacoes", solicitacaoRepository.findAll());

        solicitacaoRepository.findById(solicitacaoId).ifPresentOrElse(
                solicitacao -> {
                    solicitacaoRepository.delete(solicitacao);
                    mv.addObject("solicitacoes", solicitacaoRepository.findAll());
                    mv.addObject("mensagemSucesso", "Solicitação recusada e excluída com sucesso.");
                },
                () -> {
                    mv.addObject("erroMensagem", "Solicitação não encontrada para exclusão.");
                }
        );
        return mv;
    }
}
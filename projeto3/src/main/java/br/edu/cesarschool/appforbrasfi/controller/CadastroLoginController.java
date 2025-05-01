package br.edu.cesarschool.appforbrasfi.controller;

import br.edu.cesarschool.appforbrasfi.model.Usuario;
import br.edu.cesarschool.appforbrasfi.repository.UsuarioRepository;
import br.edu.cesarschool.appforbrasfi.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CadastroLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public ModelAndView login(Usuario usuario)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("login");
        mv.addObject("usuario", new Usuario());
        return mv;
    }

    @PostMapping("EntrarNaPlataforma")
    public ModelAndView entrarNaPlataforma(Usuario usuario)
    {
        ModelAndView mv = new ModelAndView();
        boolean podeEntrar = usuarioService.validateLogin(usuario);
        if (podeEntrar)
        {
            mv.setViewName("redirect:home");
        }
        else
        {
            mv.setViewName("redirect:");
        }
        return mv;
    }

    @GetMapping("/cadastro")
    public ModelAndView cadastro(Usuario usuario)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("cadastro");
        mv.addObject("usuario", new Usuario());
        return mv;
    }

    @PostMapping("CadastrarUsuario")
    public ModelAndView cadastrarUsuario(Usuario usuario)
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("redirect:");
        usuarioRepository.save(usuario);
        return mv;
    }
}

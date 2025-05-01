package br.edu.cesarschool.appforbrasfi.service;

import br.edu.cesarschool.appforbrasfi.model.Usuario;
import br.edu.cesarschool.appforbrasfi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    //usuarioSave
    @Autowired
    private UsuarioRepository usuarioRepository;
    //validateLogin
    public boolean validateLogin(Usuario usuario)
    {
        List<Usuario> usuarios = findAllUsers();
        for (Usuario u: usuarios)
        {
            if (u.getSenha().equals(usuario.getSenha()) && u.getNome().equals(usuario.getNome()))
            {
                return true;
            }
        }

        return false;
    }

    public List<Usuario> findAllUsers()
    {
        return usuarioRepository.findAll();
    }

}

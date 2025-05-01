package br.edu.cesarschool.appforbrasfi.repository;

import br.edu.cesarschool.appforbrasfi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

}

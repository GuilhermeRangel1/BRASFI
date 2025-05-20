package com.brasfi.webapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.brasfi.webapp.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); 
    User findByCpf(String cpf);
}

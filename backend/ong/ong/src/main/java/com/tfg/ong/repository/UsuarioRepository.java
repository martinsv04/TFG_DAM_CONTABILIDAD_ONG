package com.tfg.ong.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRol(Rol rol);

}

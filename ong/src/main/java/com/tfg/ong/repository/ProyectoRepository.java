package com.tfg.ong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByOngId(Long idOng);
}

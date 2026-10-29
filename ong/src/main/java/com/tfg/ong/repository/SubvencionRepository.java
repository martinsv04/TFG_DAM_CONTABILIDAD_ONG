package com.tfg.ong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Subvencion;

public interface SubvencionRepository extends JpaRepository<Subvencion, Long> {

    List<Subvencion> findByOngId(Long idOng);
}

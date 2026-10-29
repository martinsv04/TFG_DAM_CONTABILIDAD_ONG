package com.tfg.ong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.MiembroGobierno;

public interface MiembroGobiernoRepository extends JpaRepository<MiembroGobierno, Long> {

    List<MiembroGobierno> findByOngIdOrderByOrganoAscOrdenAscIdAsc(Long idOng);
}

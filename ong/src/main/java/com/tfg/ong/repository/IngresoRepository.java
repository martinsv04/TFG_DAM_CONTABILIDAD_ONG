package com.tfg.ong.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.tfg.ong.model.Ingreso;

public interface IngresoRepository extends JpaRepository<Ingreso, Long>{
    
    List<Ingreso> findByOngId(Long idOng);

    List<Ingreso> findByOngIdAndFechaBetween(Long idOng, LocalDate desde, LocalDate hasta);

}

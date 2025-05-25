package com.tfg.ong.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Gasto;

public interface GastoRepository extends JpaRepository<Gasto, Long>{
    
    List<Gasto> findByOngId(Long idOng);

    List<Gasto> findByOngIdAndFechaBetween(Long idOng, LocalDate desde, LocalDate hasta);
    
}

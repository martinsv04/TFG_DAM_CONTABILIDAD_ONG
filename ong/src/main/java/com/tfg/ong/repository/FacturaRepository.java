package com.tfg.ong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tfg.ong.model.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long>{

    List<Factura> findByOngId(Long idOng);

    boolean existsByIngresoId(Long idIngreso);

    boolean existsByGastoId(Long idGasto);

    /** Números de factura de una ONG que empiezan por un prefijo de serie (por ejemplo «F-2026-»). */
    @Query("select f.numero from Factura f where f.ong.id = :idOng and f.numero like concat(:prefijo, '%')")
    List<String> findNumerosByOngIdAndPrefijo(@Param("idOng") Long idOng, @Param("prefijo") String prefijo);

}

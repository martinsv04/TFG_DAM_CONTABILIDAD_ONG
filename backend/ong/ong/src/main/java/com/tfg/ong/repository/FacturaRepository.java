package com.tfg.ong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long>{

    List<Factura> findByOngId(Long idOng);

}

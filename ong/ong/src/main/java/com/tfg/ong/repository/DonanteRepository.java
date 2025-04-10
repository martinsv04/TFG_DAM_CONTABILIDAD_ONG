package com.tfg.ong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Donante;

public interface DonanteRepository extends JpaRepository<Donante, Long> {
}
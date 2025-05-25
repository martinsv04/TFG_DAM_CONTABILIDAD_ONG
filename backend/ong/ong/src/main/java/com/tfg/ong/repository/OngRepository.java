package com.tfg.ong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfg.ong.model.Ong;

public interface OngRepository extends JpaRepository<Ong, Long>{

    List<Ong> findByAdminId(Long adminId);
    
}

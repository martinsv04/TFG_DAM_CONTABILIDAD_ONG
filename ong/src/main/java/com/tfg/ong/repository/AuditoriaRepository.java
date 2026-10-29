package com.tfg.ong.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tfg.ong.model.RegistroAuditoria;

public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    @Query("select r from RegistroAuditoria r where r.ongId = :ong"
            + " and (:entidad is null or r.entidad = :entidad)"
            + " and (:accion is null or r.accion = :accion)"
            + " and (:desde is null or r.fecha >= :desde)"
            + " and (:hasta is null or r.fecha < :hasta)"
            + " order by r.fecha desc, r.id desc")
    Page<RegistroAuditoria> buscar(@Param("ong") Long ong, @Param("entidad") String entidad,
                                   @Param("accion") String accion, @Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta, Pageable pageable);
}

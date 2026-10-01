package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Reporte;
import com.tfg.ong.repository.ReporteRepository;

@Service
public class ReporteService {
    
    private ReporteRepository reporteRepository;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

      public List<Reporte> getAllReportes() {
        return reporteRepository.findAll();
    }

    public Reporte getReporteById(Long id) {
        Optional<Reporte> reporte = reporteRepository.findById(id);
        return reporte.orElse(null);
    }

    public Reporte createReporte(Reporte reporte) {
        return reporteRepository.save(reporte);
    }

    public Reporte updateReportes(Long id, Reporte reporte) {
        reporte.setId(id);
        return reporteRepository.save(reporte);
    }

    public void deleteReporte(Long id) {
        reporteRepository.deleteById(id);
    }

}

package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Ingreso;
import com.tfg.ong.repository.IngresoRepository;

@Service
public class IngresoService {

    private final IngresoRepository ingresoRepository;

    public IngresoService(IngresoRepository ingresoRepository) {
        this.ingresoRepository = ingresoRepository;
    }

    public List<Ingreso> getAllIngresos() {
        return ingresoRepository.findAll();
    }

    public Ingreso getIngresoById(Long id) {
        Optional<Ingreso> ingreso = ingresoRepository.findById(id);
        return ingreso.orElse(null);
    }

    public Ingreso createIngreso(Ingreso ingreso) {
        return ingresoRepository.save(ingreso);
    }

    public Ingreso updateIngresos(Long id, Ingreso ingreso) {
        ingreso.setId(id);
        return ingresoRepository.save(ingreso);
    }

    public void deleteIngreso(Long id) {
        ingresoRepository.deleteById(id);
    }

}

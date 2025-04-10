package com.tfg.ong.service;

import com.tfg.ong.model.Donante;
import com.tfg.ong.repository.DonanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonanteService {

    @Autowired
    private DonanteRepository donanteRepository;

    public List<Donante> getAllDonantes() {
        return donanteRepository.findAll();
    }

    public Donante getDonanteById(Long id) {
        Optional<Donante> donante = donanteRepository.findById(id);
        return donante.orElse(null); // Devolver null si no existe
    }

    public Donante createDonante(Donante donante) {
        return donanteRepository.save(donante);
    }

    public Donante updateDonante(Long id, Donante donante) {
        donante.setId(id);
        return donanteRepository.save(donante);
    }

    public void deleteDonante(Long id) {
        donanteRepository.deleteById(id);
    }
}

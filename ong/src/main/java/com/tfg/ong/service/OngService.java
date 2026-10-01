package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Ong;
import com.tfg.ong.repository.OngRepository;

@Service
public class OngService {

    private final OngRepository ongRepository;

    public OngService(OngRepository ongRepository) {
        this.ongRepository = ongRepository;
    }

      public List<Ong> getAllOngs() {
        return ongRepository.findAll();
    }

    public Ong getOngById(Long id) {
        Optional<Ong> ong = ongRepository.findById(id);
        return ong.orElse(null);
    }

    public Ong createOng(Ong ong) {
        return ongRepository.save(ong);
    }

    public Ong updateOngs(Long id, Ong ong) {
        ong.setId(id);
        return ongRepository.save(ong);
    }

    public void deleteOng(Long id) {
        ongRepository.deleteById(id);
    }

}

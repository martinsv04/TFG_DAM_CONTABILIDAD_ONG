package com.tfg.ong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tfg.ong.model.Gasto;
import com.tfg.ong.repository.GastoRepository;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;

    public GastoService(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    public List<Gasto> getAllGastos() {
        return gastoRepository.findAll();
    }

    public Gasto getGastoById(Long id) {
        Optional<Gasto> gasto = gastoRepository.findById(id);
        return gasto.orElse(null);
    }

    public Gasto createGasto(Gasto gasto) {
        return gastoRepository.save(gasto);
    }

    public Gasto updateGastos(Long id, Gasto gasto) {
        gasto.setId(id);
        return gastoRepository.save(gasto);
    }

    public void deleteGasto(Long id) {
        gastoRepository.deleteById(id);
    }

}

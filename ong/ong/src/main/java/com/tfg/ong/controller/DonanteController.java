package com.tfg.ong.controller;

import com.tfg.ong.model.Donante;
import com.tfg.ong.service.DonanteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donantes")
public class DonanteController {

    @Autowired
    private DonanteService donanteService;

    @GetMapping
    public List<Donante> getAllDonantes() {
        return donanteService.getAllDonantes();
    }

    @GetMapping("/{id}")
    public Donante getDonanteById(@PathVariable Long id) {
        return donanteService.getDonanteById(id);
    }

    @PostMapping
    public Donante createDonante(@RequestBody Donante donante) {
        return donanteService.createDonante(donante);
    }

    @PutMapping("/{id}")
    public Donante updateDonante(@PathVariable Long id, @RequestBody Donante donante) {
        return donanteService.updateDonante(id, donante);
    }

    @DeleteMapping("/{id}")
    public void deleteDonante(@PathVariable Long id) {
        donanteService.deleteDonante(id);
    }
}

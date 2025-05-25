package com.tfg.ong.controller;

import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Rol;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.OngRepository;
import com.tfg.ong.repository.UsuarioRepository;
import com.tfg.ong.service.OngService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ongs")
public class OngController {

    private final OngService ongService;

    private final UsuarioRepository usuarioRepository;

    private final OngRepository ongRepository;

    public OngController(OngService ongService, UsuarioRepository usuarioRepository, OngRepository ongRepository) {
        this.ongService = ongService;
        this.usuarioRepository = usuarioRepository;
        this.ongRepository = ongRepository;
    }
    @GetMapping
    public List<Ong> getAllOngs() {

        return ongService.getAllOngs();

    }

    @GetMapping("/{id}")
    public Ong getOngById(@PathVariable Long id) {

        return ongService.getOngById(id);

    }

    @PostMapping
    public Ong createOng(@RequestBody Ong ong) {

        ong.setFechaCreacion(LocalDate.now());
        return ongService.createOng(ong);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Ong> updateOng(@PathVariable Long id, @RequestBody Ong updatedOng) {

    Optional<Ong> ongOpt = ongRepository.findById(id);

        if (ongOpt.isPresent()) {
            Ong ong = ongOpt.get();
            ong.setNombre(updatedOng.getNombre());
            ong.setDescripcion(updatedOng.getDescripcion());
            ong.setDireccion(updatedOng.getDireccion());
            ong.setTelefono(updatedOng.getTelefono());
            ong.setEmail(updatedOng.getEmail());

            ongRepository.save(ong);

            return ResponseEntity.ok(ong);
        } else {
            return ResponseEntity.notFound().build();
        }

    }


    @DeleteMapping("/{id}")
    public void deleteOng(@PathVariable Long id) {

        ongService.deleteOng(id);

    }

    @GetMapping("/admin/{adminId}")
    public List<Ong> getOngsByAdminId(@PathVariable Long adminId) {

    return ongRepository.findByAdminId(adminId);

    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<Ong>> getOngsForUser(@PathVariable Long userId) {
        
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (usuario.getRol().equals(Rol.ADMIN)) {
                List<Ong> ongs = ongRepository.findByAdminId(userId);
                return ResponseEntity.ok(ongs);
            } else {
                Ong ong = usuario.getOng();
                if (ong != null) {
                    List<Ong> resultado = new ArrayList<>();
                    resultado.add(ong);
                    return ResponseEntity.ok(resultado);
                } else {
                    return ResponseEntity.ok(Collections.emptyList());
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }

    }

}

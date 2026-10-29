package com.tfg.ong.service;

import com.tfg.ong.dto.Modelo182Dto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Usuario;
import com.tfg.ong.repository.IngresoRepository;
import com.tfg.ong.repository.OngRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Modelo182ServiceTest {

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private OngRepository ongRepository;

    @InjectMocks
    private Modelo182Service service;

    private Ong ong;
    private Usuario carlos;
    private Usuario ana;

    @BeforeEach
    void preparar() {
        ong = new Ong();
        ong.setId(1L);
        ong.setNombre("Manos Solidarias");
        ong.setNif("G12345678");
        ong.setRepresentante("Laura Admin");
        ong.setTelefono("954 123 456");
        lenient().when(ongRepository.findById(1L)).thenReturn(Optional.of(ong));

        carlos = new Usuario();
        carlos.setId(5L);
        carlos.setNombre("Ruiz Pérez Carlos");
        carlos.setNifCif("12345678Z");
        carlos.setCodigoPostal("41001");

        ana = new Usuario();
        ana.setId(6L);
        ana.setNombre("López Ana");
        ana.setNifCif("87654321X");
        ana.setCodigoPostal("28001");
    }

    private Ingreso ingreso(long id, String monto, String fecha, Usuario u) {
        Ingreso i = new Ingreso();
        i.setId(id);
        i.setOng(ong);
        i.setTipo("DONACION");
        i.setMonto(new BigDecimal(monto));
        i.setFecha(LocalDate.parse(fecha));
        i.setUsuario(u);
        return i;
    }

    private void donaciones(Ingreso... lista) {
        when(ingresoRepository.findByOngIdAndFechaBetween(eq(1L), any(), any())).thenReturn(List.of(lista));
    }

    @Test
    void donanteRecurrenteConImporteAltoLlevaPorcentajeDel45() {
        donaciones(ingreso(1, "300", "2026-02-01", carlos), ingreso(2, "1200", "2026-06-01", carlos),
                ingreso(3, "1000", "2025-05-01", carlos), ingreso(4, "500", "2024-05-01", carlos));

        Modelo182Dto d = service.previsualizar(1L, 2026);

        assertEquals(1, d.registros().size());
        Modelo182Dto.RegistroDTO r = d.registros().get(0);
        assertEquals(new BigDecimal("1500.00"), r.importe());
        assertTrue(r.recurrente());
        assertEquals(0, new BigDecimal("45").compareTo(r.porcentaje()));
        assertEquals("F", r.naturaleza());
        assertEquals("41", r.provincia());
        assertTrue(d.listo());
    }

    @Test
    void donanteNuevoConImportePequenoLleva80() {
        donaciones(ingreso(1, "100", "2026-02-01", ana));

        Modelo182Dto.RegistroDTO r = service.previsualizar(1L, 2026).registros().get(0);

        assertFalse(r.recurrente());
        assertEquals(0, new BigDecimal("80").compareTo(r.porcentaje()));
    }

    @Test
    void ficheroTieneRegistrosDe250CaracteresYCamposEnSuSitio() {
        donaciones(ingreso(1, "300", "2026-02-01", carlos), ingreso(2, "1200", "2026-06-01", carlos),
                ingreso(3, "1000", "2025-05-01", carlos), ingreso(4, "500", "2024-05-01", carlos),
                ingreso(5, "100", "2026-03-01", ana));

        String contenido = new String(service.fichero(1L, 2026), StandardCharsets.ISO_8859_1);
        String[] lineas = contenido.split("\r\n");

        assertEquals(3, lineas.length);
        for (String l : lineas) {
            assertEquals(250, l.length());
        }
        // Declarante
        assertTrue(lineas[0].startsWith("1182" + "2026" + "G12345678"));
        assertEquals("MANOS SOLIDARIAS", lineas[0].substring(17, 57).trim());
        assertEquals("182" + "2026" + "000001", lineas[0].substring(107, 120));
        // Donantes (orden alfabético: Lopez Ana, Ruiz Perez Carlos)
        assertTrue(lineas[1].startsWith("2182" + "2026" + "G12345678" + "87654321X"));
        assertTrue(lineas[2].startsWith("2182" + "2026" + "G12345678" + "12345678Z"));
        // Carlos: provincia 41, clave A, 45,00 %, 1.500,00 €, naturaleza F, recurrente '1'
        String c = lineas[2];
        assertEquals("RUIZ PEREZ CARLOS", c.substring(35, 75).trim());
        assertEquals("41A" + "04500" + "00000001500" + "00", c.substring(75, 75 + 3 + 5 + 11 + 2));
        assertEquals('1', c.charAt(c.length() - 9 - 40 - 69 - 1));
        // Ana: 80 %, no recurrente '2'
        String a = lineas[1];
        assertEquals("28A" + "08000", a.substring(75, 83));
        assertEquals('2', a.charAt(a.length() - 9 - 40 - 69 - 1));
    }

    @Test
    void ficheroFallaSiFaltanDatosDeLaOng() {
        ong.setNif(null);
        donaciones(ingreso(1, "100", "2026-02-01", ana));

        Modelo182Dto d = service.previsualizar(1L, 2026);
        assertFalse(d.listo());
        assertFalse(d.problemas().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.fichero(1L, 2026));
    }

    @Test
    void donanteSinCodigoPostalImpideGenerar() {
        ana.setCodigoPostal(null);
        donaciones(ingreso(1, "100", "2026-02-01", ana));

        Modelo182Dto d = service.previsualizar(1L, 2026);

        assertFalse(d.listo());
        assertFalse(d.registros().get(0).problemas().isEmpty());
    }

    @Test
    void sinDonacionesNoEstaListo() {
        donaciones();
        assertFalse(service.previsualizar(1L, 2026).listo());
    }

    @Test
    void formatoDeCampos() {
        assertEquals("000000012345", Modelo182Service.numero(12345, 12));
        assertEquals("0000000150000", Modelo182Service.importe(new BigDecimal("1500"), 11));
        assertEquals("ANO NUEVO     ", Modelo182Service.texto("Año nuevo", 14).replace('Ñ', 'N'));
        assertEquals("04500", Modelo182Service.porcentajeCampo(new BigDecimal("45")));
    }
}

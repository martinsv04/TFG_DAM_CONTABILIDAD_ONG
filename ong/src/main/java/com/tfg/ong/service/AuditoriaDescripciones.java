package com.tfg.ong.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.tfg.ong.model.Factura;
import com.tfg.ong.model.Gasto;
import com.tfg.ong.model.Ingreso;
import com.tfg.ong.model.Ong;
import com.tfg.ong.model.Proyecto;
import com.tfg.ong.model.Subvencion;

/** Texto, id y ONG de cada tipo de elemento para el registro de actividad. */
public final class AuditoriaDescripciones {

    public static final String INGRESO = "INGRESO";
    public static final String GASTO = "GASTO";
    public static final String FACTURA = "FACTURA";
    public static final String PROYECTO = "PROYECTO";
    public static final String SUBVENCION = "SUBVENCION";

    private AuditoriaDescripciones() {
    }

    private static String euros(BigDecimal valor) {
        return valor == null ? "sin importe" : valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? "sin descripción" : "«" + valor.trim() + "»";
    }

    private static String cuenta(String valor) {
        return valor == null || valor.isBlank() ? "" : " · cuenta " + valor;
    }

    /** Resumen legible del elemento (null si el objeto no es de un tipo auditado). */
    public static String describir(Object e) {
        if (e instanceof Ingreso i) {
            return texto(i.getDescripcion()) + " · " + (i.getTipo() == null ? "sin tipo" : i.getTipo()) + " · " + euros(i.getMonto()) + cuenta(i.getCuenta());
        }
        if (e instanceof Gasto g) {
            return texto(g.getDescripcion()) + " · " + (g.getCategoria() == null ? "sin categoría" : g.getCategoria().name()) + " · " + euros(g.getMonto()) + cuenta(g.getCuenta());
        }
        if (e instanceof Factura f) {
            return "Factura " + (f.getNumero() == null ? "sin número" : f.getNumero()) + " · " + euros(f.getTotal());
        }
        if (e instanceof Proyecto p) {
            return texto(p.getNombre()) + " · " + p.getEstado() + (p.getPresupuesto() == null ? "" : " · presupuesto " + euros(p.getPresupuesto()));
        }
        if (e instanceof Subvencion s) {
            return texto(s.getNombre()) + " · " + s.getEstado() + (s.getFinanciador() == null ? "" : " · " + s.getFinanciador());
        }
        return null;
    }

    public static Long id(Object e) {
        if (e instanceof Ingreso i) return i.getId();
        if (e instanceof Gasto g) return g.getId();
        if (e instanceof Factura f) return f.getId();
        if (e instanceof Proyecto p) return p.getId();
        if (e instanceof Subvencion s) return s.getId();
        return null;
    }

    public static Long ongId(Object e) {
        Ong ong = null;
        if (e instanceof Ingreso i) ong = i.getOng();
        else if (e instanceof Gasto g) ong = g.getOng();
        else if (e instanceof Factura f) ong = f.getOng();
        else if (e instanceof Proyecto p) ong = p.getOng();
        else if (e instanceof Subvencion s) ong = s.getOng();
        return ong == null ? null : ong.getId();
    }
}

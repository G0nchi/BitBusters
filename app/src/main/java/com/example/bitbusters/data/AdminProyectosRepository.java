package com.example.bitbusters.data;

import com.example.bitbusters.models.AdminProyecto;
import com.example.bitbusters.models.Tipologia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repositorio estático único para los proyectos del Administrador.
 * Diferente a ProjectSessionData (datos mock del cliente/asesor) — no mezclarlos.
 *
 * Patrón: singleton de lista estática con datos demo iniciales.
 * Más reciente primero (index 0).
 */
public class AdminProyectosRepository {

    /** Lista maestra — se inicializa con 3 proyectos demo */
    private static final List<AdminProyecto> proyectos = new ArrayList<>(crearDemoInicial());

    /** Evita instanciación accidental */
    private AdminProyectosRepository() {}

    // ── API pública ──────────────────────────────────────────────────────────

    /**
     * Agrega un proyecto al inicio de la lista (más reciente primero).
     *
     * @param p Proyecto a agregar.
     */
    public static void agregar(AdminProyecto p) {
        proyectos.add(0, p);
    }

    /**
     * Devuelve la lista completa (más reciente primero).
     */
    public static List<AdminProyecto> getTodos() {
        return proyectos;
    }

    /**
     * Busca un proyecto por su ID único.
     *
     * @param id ID del proyecto (UUID o "demo-xxx").
     * @return El proyecto si existe, o null si no se encontró.
     */
    public static AdminProyecto getById(String id) {
        if (id == null || id.isEmpty()) return null;
        for (AdminProyecto p : proyectos) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    /** Elimina todos los proyectos (útil para tests). */
    public static void limpiar() {
        proyectos.clear();
    }

    // ── Datos demo iniciales ─────────────────────────────────────────────────

    /**
     * Crea 3 proyectos ficticios para que la lista no aparezca vacía en primera apertura.
     */
    private static List<AdminProyecto> crearDemoInicial() {
        List<AdminProyecto> demo = new ArrayList<>();

        // ── Proyecto 1: Edificio Los Álamos ──────────────────────────────────
        List<Tipologia> tipologias1 = new ArrayList<>();
        tipologias1.add(new Tipologia("Tipo A", 2, 1, 65.0,  280000.0, "Vista exterior",  ""));
        tipologias1.add(new Tipologia("Tipo B", 3, 2, 90.0,  380000.0, "Vista al jardín", ""));
        tipologias1.add(new Tipologia("Penthouse", 3, 3, 130.0, 680000.0, "Azotea privada", ""));
        demo.add(new AdminProyecto(
                "demo-001",
                "Edificio Los Álamos",
                "Proyecto residencial de 15 pisos con unidades de 2 y 3 dormitorios " +
                        "en la mejor zona de Miraflores.",
                "Av. Benavides 1520",
                "Miraflores",
                "5000",
                "280000",
                "Álamos Residencial",
                "S/ 320,000",
                "30/12/2026",
                "Preventa",
                tipologias1,
                Arrays.asList("Carlos Ruiz", "Ana Torres"),
                new ArrayList<>(),
                "01/01/2025"
        ));

        // ── Proyecto 2: Mirador de Surco ─────────────────────────────────────
        List<Tipologia> tipologias2 = new ArrayList<>();
        tipologias2.add(new Tipologia("Loft A", 1, 1, 45.0, 180000.0, "Diseño moderno", ""));
        tipologias2.add(new Tipologia("Flat B", 2, 1, 65.0, 240000.0, "Amplio y luminoso", ""));
        demo.add(new AdminProyecto(
                "demo-002",
                "Mirador de Surco",
                "Departamentos modernos con vista panorámica a todo Santiago de Surco.",
                "Calle Las Flores 230",
                "Santiago de Surco",
                "3000",
                "180000",
                "Surco Mirador",
                "S/ 195,000",
                "15/06/2026",
                "En planos",
                tipologias2,
                Arrays.asList("Luis Medina"),
                new ArrayList<>(),
                "15/02/2025"
        ));

        // ── Proyecto 3: Alto San Felipe ───────────────────────────────────────
        demo.add(new AdminProyecto(
                "demo-003",
                "Alto San Felipe",
                "Exclusivo condominio en zona residencial privilegiada con acceso directo " +
                        "a clubhouse y áreas verdes.",
                "Jr. San Felipe 890",
                "Jesús María",
                "8000",
                "450000",
                "San Felipe Premium",
                "S/ 480,000",
                "01/03/2027",
                "En venta",
                new ArrayList<>(),
                Arrays.asList("María Quispe", "Pedro Salinas", "Rosa Díaz"),
                new ArrayList<>(),
                "20/03/2025"
        ));

        return demo;
    }
}

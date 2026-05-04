package com.example.bitbusters.data;

import com.example.bitbusters.models.AdminAsesor;
import com.example.bitbusters.models.AdminSeparacion;
import com.example.bitbusters.models.AdminHistorialSeparacion;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AdminDataRepository {

    private AdminDataRepository() {
    }

    /**
     * Retorna lista de ~35 asesores disponibles para asignación
     */
    public static List<AdminAsesor> getAsesores() {
        return new ArrayList<>(Arrays.asList(
                new AdminAsesor("A1", "Carlos Ruiz", "CR", 3, "Activo", "carlos@inmobxyz.pe", "999-111-001"),
                new AdminAsesor("A2", "Ana Torres", "AT", 5, "Activo", "ana@inmobxyz.pe", "999-111-002"),
                new AdminAsesor("A3", "Mario Pérez", "MP", 2, "Activo", "mario@inmobxyz.pe", "999-111-003"),
                new AdminAsesor("A4", "Lucia Vargas", "LV", 8, "Activo", "lucia@inmobxyz.pe", "999-111-004"),
                new AdminAsesor("A5", "Roberto Silva", "RS", 1, "Activo", "roberto@inmobxyz.pe", "999-111-005"),
                new AdminAsesor("A6", "Patricia Mendez", "PM", 4, "Activo", "patricia@inmobxyz.pe", "999-111-006"),
                new AdminAsesor("A7", "Fernando Lopez", "FL", 6, "Activo", "fernando@inmobxyz.pe", "999-111-007"),
                new AdminAsesor("A8", "Marcela Gutierrez", "MG", 3, "Activo", "marcela@inmobxyz.pe", "999-111-008"),
                new AdminAsesor("A9", "Diego Castillo", "DC", 7, "Activo", "diego@inmobxyz.pe", "999-111-009"),
                new AdminAsesor("A10", "Valeria Romero", "VR", 2, "Inactivo", "valeria@inmobxyz.pe", "999-111-010"),
                new AdminAsesor("A11", "Gustavo Morales", "GM", 5, "Activo", "gustavo@inmobxyz.pe", "999-111-011"),
                new AdminAsesor("A12", "Sofía Delgado", "SD", 4, "Activo", "sofia@inmobxyz.pe", "999-111-012"),
                new AdminAsesor("A13", "Javier Campos", "JC", 9, "Activo", "javier@inmobxyz.pe", "999-111-013"),
                new AdminAsesor("A14", "Isabel Herrera", "IH", 3, "Activo", "isabel@inmobxyz.pe", "999-111-014"),
                new AdminAsesor("A15", "Miguel Ángel Soto", "MAS", 6, "Activo", "miguel@inmobxyz.pe", "999-111-015"),
                new AdminAsesor("A16", "Carmen Rodriguez", "CR", 2, "Activo", "carmen@inmobxyz.pe", "999-111-016"),
                new AdminAsesor("A17", "Pablo Navarro", "PN", 7, "Activo", "pablo@inmobxyz.pe", "999-111-017"),
                new AdminAsesor("A18", "Daniela Flores", "DF", 4, "Activo", "daniela@inmobxyz.pe", "999-111-018"),
                new AdminAsesor("A19", "Enrique Vargas", "EV", 5, "Activo", "enrique@inmobxyz.pe", "999-111-019"),
                new AdminAsesor("A20", "Rosario Jimenez", "RJ", 3, "Inactivo", "rosario@inmobxyz.pe", "999-111-020"),
                new AdminAsesor("A21", "Santiago Medina", "SM", 8, "Activo", "santiago@inmobxyz.pe", "999-111-021"),
                new AdminAsesor("A22", "Veronica Castro", "VC", 2, "Activo", "veronica@inmobxyz.pe", "999-111-022"),
                new AdminAsesor("A23", "Andres Quintero", "AQ", 6, "Activo", "andres@inmobxyz.pe", "999-111-023"),
                new AdminAsesor("A24", "Mariela Sánchez", "MS", 4, "Activo", "mariela@inmobxyz.pe", "999-111-024"),
                new AdminAsesor("A25", "Felipe Ramírez", "FR", 7, "Activo", "felipe@inmobxyz.pe", "999-111-025"),
                new AdminAsesor("A26", "Alejandra Ruiz", "AR", 3, "Activo", "alejandra@inmobxyz.pe", "999-111-026"),
                new AdminAsesor("A27", "Cristian Muñoz", "CM", 5, "Activo", "cristian@inmobxyz.pe", "999-111-027"),
                new AdminAsesor("A28", "Lorena Pino", "LP", 2, "Activo", "lorena@inmobxyz.pe", "999-111-028"),
                new AdminAsesor("A29", "Hector Reyes", "HR", 9, "Activo", "hector@inmobxyz.pe", "999-111-029"),
                new AdminAsesor("A30", "Giannina Moreno", "GM", 4, "Activo", "giannina@inmobxyz.pe", "999-111-030"),
                new AdminAsesor("A31", "Leo Contreras", "LC", 6, "Activo", "leo@inmobxyz.pe", "999-111-031"),
                new AdminAsesor("A32", "Natalia Vega", "NV", 3, "Activo", "natalia@inmobxyz.pe", "999-111-032"),
                new AdminAsesor("A33", "Oscar Valencia", "OV", 5, "Inactivo", "oscar@inmobxyz.pe", "999-111-033"),
                new AdminAsesor("A34", "Roxana Fuentes", "RF", 7, "Activo", "roxana@inmobxyz.pe", "999-111-034"),
                new AdminAsesor("A35", "Wilfredo Gutierrez", "WG", 2, "Activo", "wilfredo@inmobxyz.pe", "999-111-035")
        ));
    }

    /**
     * Retorna lista de ~35 separaciones
     */
    public static List<AdminSeparacion> getSeparaciones() {
        return new ArrayList<>(Arrays.asList(
                new AdminSeparacion("S1", "Edificio Los Álamos", "S/ 5,000", "01/abr/2026", "Carlos Ruiz", "Aprobada"),
                new AdminSeparacion("S2", "Mirador de Surco", "S/ 8,500", "31/mar/2026", "Ana Torres", "Aprobada"),
                new AdminSeparacion("S3", "Alto San Felipe", "S/ 6,200", "28/mar/2026", "Mario Pérez", "Pendiente"),
                new AdminSeparacion("S4", "Residencial Verde", "S/ 7,100", "20/abr/2026", "Lucia Vargas", "Aprobada"),
                new AdminSeparacion("S5", "Torres Unidas", "S/ 4,500", "15/abr/2026", "Roberto Silva", "Pendiente"),
                new AdminSeparacion("S6", "Catalina Ventor", "S/ 9,000", "10/abr/2026", "Patricia Mendez", "Rechazada"),
                new AdminSeparacion("S7", "Torre Miramar", "S/ 5,800", "05/abr/2026", "Fernando Lopez", "Aprobada"),
                new AdminSeparacion("S8", "Residencial El Park", "S/ 6,500", "02/abr/2026", "Marcela Gutierrez", "Pendiente"),
                new AdminSeparacion("S9", "Vista Marina Residencial", "S/ 10,200", "25/mar/2026", "Diego Castillo", "Aprobada"),
                new AdminSeparacion("S10", "Torres del Sol", "S/ 4,800", "22/mar/2026", "Valeria Romero", "Rechazada"),
                new AdminSeparacion("S11", "Casa Linda", "S/ 5,500", "18/mar/2026", "Gustavo Morales", "Aprobada"),
                new AdminSeparacion("S12", "Casa Fuapa", "S/ 6,300", "14/mar/2026", "Sofía Delgado", "Pendiente"),
                new AdminSeparacion("S13", "Los Robles", "S/ 7,200", "10/mar/2026", "Javier Campos", "Aprobada"),
                new AdminSeparacion("S14", "Condominio Las Lomas", "S/ 5,900", "08/mar/2026", "Isabel Herrera", "Aprobada"),
                new AdminSeparacion("S15", "Catalina Sky", "S/ 8,700", "05/mar/2026", "Miguel Ángel Soto", "Rechazada"),
                new AdminSeparacion("S16", "Los Álamos", "S/ 4,200", "01/mar/2026", "Carmen Rodriguez", "Pendiente"),
                new AdminSeparacion("S17", "Edificio Los Álamos", "S/ 5,600", "28/feb/2026", "Pablo Navarro", "Aprobada"),
                new AdminSeparacion("S18", "Mirador de Surco", "S/ 7,400", "25/feb/2026", "Daniela Flores", "Pendiente"),
                new AdminSeparacion("S19", "Alto San Felipe", "S/ 6,100", "22/feb/2026", "Enrique Vargas", "Aprobada"),
                new AdminSeparacion("S20", "Residencial Verde", "S/ 9,100", "20/feb/2026", "Rosario Jimenez", "Rechazada"),
                new AdminSeparacion("S21", "Torres Unidas", "S/ 4,900", "18/feb/2026", "Santiago Medina", "Aprobada"),
                new AdminSeparacion("S22", "Catalina Ventor", "S/ 8,200", "15/feb/2026", "Veronica Castro", "Pendiente"),
                new AdminSeparacion("S23", "Torre Miramar", "S/ 5,300", "12/feb/2026", "Andres Quintero", "Aprobada"),
                new AdminSeparacion("S24", "Residencial El Park", "S/ 7,800", "10/feb/2026", "Mariela Sánchez", "Aprobada"),
                new AdminSeparacion("S25", "Vista Marina Residencial", "S/ 9,500", "08/feb/2026", "Felipe Ramírez", "Rechazada"),
                new AdminSeparacion("S26", "Torres del Sol", "S/ 5,100", "05/feb/2026", "Alejandra Ruiz", "Aprobada"),
                new AdminSeparacion("S27", "Casa Linda", "S/ 6,800", "02/feb/2026", "Cristian Muñoz", "Pendiente"),
                new AdminSeparacion("S28", "Casa Fuapa", "S/ 7,600", "30/ene/2026", "Lorena Pino", "Aprobada"),
                new AdminSeparacion("S29", "Los Robles", "S/ 4,700", "28/ene/2026", "Hector Reyes", "Aprobada"),
                new AdminSeparacion("S30", "Condominio Las Lomas", "S/ 6,400", "25/ene/2026", "Giannina Moreno", "Rechazada"),
                new AdminSeparacion("S31", "Catalina Sky", "S/ 8,900", "22/ene/2026", "Leo Contreras", "Pendiente"),
                new AdminSeparacion("S32", "Los Álamos", "S/ 5,400", "20/ene/2026", "Natalia Vega", "Aprobada"),
                new AdminSeparacion("S33", "Edificio Los Álamos", "S/ 6,900", "18/ene/2026", "Oscar Valencia", "Aprobada"),
                new AdminSeparacion("S34", "Mirador de Surco", "S/ 7,300", "15/ene/2026", "Roxana Fuentes", "Rechazada"),
                new AdminSeparacion("S35", "Alto San Felipe", "S/ 5,700", "12/ene/2026", "Wilfredo Gutierrez", "Pendiente")
        ));
    }

    /**
     * Retorna lista de ~35 historial de separaciones
     */
    public static List<AdminHistorialSeparacion> getHistorialSeparaciones() {
        return new ArrayList<>(Arrays.asList(
                new AdminHistorialSeparacion("H1", "Diario", "S/ 12,000", 2, 1, "01/abr/2026", "Edificio Los Álamos"),
                new AdminHistorialSeparacion("H2", "Diario", "S/ 8,500", 1, 1, "31/mar/2026", "Mirador de Surco"),
                new AdminHistorialSeparacion("H3", "Diario", "S/ 6,200", 1, 1, "28/mar/2026", "Alto San Felipe"),
                new AdminHistorialSeparacion("H4", "Diario", "S/ 7,100", 1, 1, "20/abr/2026", "Residencial Verde"),
                new AdminHistorialSeparacion("H5", "Diario", "S/ 4,500", 1, 1, "15/abr/2026", "Torres Unidas"),
                new AdminHistorialSeparacion("H6", "Diario", "S/ 9,000", 1, 1, "10/abr/2026", "Catalina Ventor"),
                new AdminHistorialSeparacion("H7", "Diario", "S/ 5,800", 1, 1, "05/abr/2026", "Torre Miramar"),
                new AdminHistorialSeparacion("H8", "Diario", "S/ 6,500", 1, 1, "02/abr/2026", "Residencial El Park"),
                new AdminHistorialSeparacion("H9", "Diario", "S/ 10,200", 1, 1, "25/mar/2026", "Vista Marina Residencial"),
                new AdminHistorialSeparacion("H10", "Diario", "S/ 4,800", 1, 1, "22/mar/2026", "Torres del Sol"),
                new AdminHistorialSeparacion("H11", "Diario", "S/ 5,500", 1, 1, "18/mar/2026", "Casa Linda"),
                new AdminHistorialSeparacion("H12", "Diario", "S/ 6,300", 1, 1, "14/mar/2026", "Casa Fuapa"),
                new AdminHistorialSeparacion("H13", "Diario", "S/ 7,200", 1, 1, "10/mar/2026", "Los Robles"),
                new AdminHistorialSeparacion("H14", "Diario", "S/ 5,900", 1, 1, "08/mar/2026", "Condominio Las Lomas"),
                new AdminHistorialSeparacion("H15", "Diario", "S/ 8,700", 1, 1, "05/mar/2026", "Catalina Sky"),
                new AdminHistorialSeparacion("H16", "Diario", "S/ 4,200", 1, 1, "01/mar/2026", "Los Álamos"),
                new AdminHistorialSeparacion("H17", "Diario", "S/ 5,600", 1, 1, "28/feb/2026", "Edificio Los Álamos"),
                new AdminHistorialSeparacion("H18", "Diario", "S/ 7,400", 1, 1, "25/feb/2026", "Mirador de Surco"),
                new AdminHistorialSeparacion("H19", "Diario", "S/ 6,100", 1, 1, "22/feb/2026", "Alto San Felipe"),
                new AdminHistorialSeparacion("H20", "Diario", "S/ 9,100", 1, 1, "20/feb/2026", "Residencial Verde"),
                new AdminHistorialSeparacion("H21", "Diario", "S/ 4,900", 1, 1, "18/feb/2026", "Torres Unidas"),
                new AdminHistorialSeparacion("H22", "Diario", "S/ 8,200", 1, 1, "15/feb/2026", "Catalina Ventor"),
                new AdminHistorialSeparacion("H23", "Diario", "S/ 5,300", 1, 1, "12/feb/2026", "Torre Miramar"),
                new AdminHistorialSeparacion("H24", "Diario", "S/ 7,800", 1, 1, "10/feb/2026", "Residencial El Park"),
                new AdminHistorialSeparacion("H25", "Diario", "S/ 9,500", 1, 1, "08/feb/2026", "Vista Marina Residencial"),
                new AdminHistorialSeparacion("H26", "Diario", "S/ 5,100", 1, 1, "05/feb/2026", "Torres del Sol"),
                new AdminHistorialSeparacion("H27", "Diario", "S/ 6,800", 1, 1, "02/feb/2026", "Casa Linda"),
                new AdminHistorialSeparacion("H28", "Diario", "S/ 7,600", 1, 1, "30/ene/2026", "Casa Fuapa"),
                new AdminHistorialSeparacion("H29", "Diario", "S/ 4,700", 1, 1, "28/ene/2026", "Los Robles"),
                new AdminHistorialSeparacion("H30", "Diario", "S/ 6,400", 1, 1, "25/ene/2026", "Condominio Las Lomas"),
                new AdminHistorialSeparacion("H31", "Diario", "S/ 8,900", 1, 1, "22/ene/2026", "Catalina Sky"),
                new AdminHistorialSeparacion("H32", "Diario", "S/ 5,400", 1, 1, "20/ene/2026", "Los Álamos"),
                new AdminHistorialSeparacion("H33", "Diario", "S/ 6,900", 1, 1, "18/ene/2026", "Edificio Los Álamos"),
                new AdminHistorialSeparacion("H34", "Diario", "S/ 7,300", 1, 1, "15/ene/2026", "Mirador de Surco"),
                new AdminHistorialSeparacion("H35", "Diario", "S/ 5,700", 1, 1, "12/ene/2026", "Alto San Felipe")
        ));
    }

    /**
     * Retorna lista de ~35 asesores de la inmobiliaria
     */
    public static List<AdminAsesorInmobiliaria> getAsesoresInmobiliaria() {
        return new ArrayList<>(Arrays.asList(
                new AdminAsesorInmobiliaria("AI1", "Carlos Ruiz", "CR", "carlos@inmobxyz.pe", "999-111-001", "Activo"),
                new AdminAsesorInmobiliaria("AI2", "Ana Torres", "AT", "ana@inmobxyz.pe", "999-111-002", "Activo"),
                new AdminAsesorInmobiliaria("AI3", "Mario Pérez", "MP", "mario@inmobxyz.pe", "999-111-003", "Activo"),
                new AdminAsesorInmobiliaria("AI4", "Lucia Vargas", "LV", "lucia@inmobxyz.pe", "999-111-004", "Activo"),
                new AdminAsesorInmobiliaria("AI5", "Roberto Silva", "RS", "roberto@inmobxyz.pe", "999-111-005", "Activo"),
                new AdminAsesorInmobiliaria("AI6", "Patricia Mendez", "PM", "patricia@inmobxyz.pe", "999-111-006", "Inactivo"),
                new AdminAsesorInmobiliaria("AI7", "Fernando Lopez", "FL", "fernando@inmobxyz.pe", "999-111-007", "Activo"),
                new AdminAsesorInmobiliaria("AI8", "Marcela Gutierrez", "MG", "marcela@inmobxyz.pe", "999-111-008", "Activo"),
                new AdminAsesorInmobiliaria("AI9", "Diego Castillo", "DC", "diego@inmobxyz.pe", "999-111-009", "Activo"),
                new AdminAsesorInmobiliaria("AI10", "Valeria Romero", "VR", "valeria@inmobxyz.pe", "999-111-010", "Inactivo"),
                new AdminAsesorInmobiliaria("AI11", "Gustavo Morales", "GM", "gustavo@inmobxyz.pe", "999-111-011", "Activo"),
                new AdminAsesorInmobiliaria("AI12", "Sofía Delgado", "SD", "sofia@inmobxyz.pe", "999-111-012", "Activo"),
                new AdminAsesorInmobiliaria("AI13", "Javier Campos", "JC", "javier@inmobxyz.pe", "999-111-013", "Activo"),
                new AdminAsesorInmobiliaria("AI14", "Isabel Herrera", "IH", "isabel@inmobxyz.pe", "999-111-014", "Activo"),
                new AdminAsesorInmobiliaria("AI15", "Miguel Ángel Soto", "MS", "miguel@inmobxyz.pe", "999-111-015", "Activo"),
                new AdminAsesorInmobiliaria("AI16", "Carmen Rodriguez", "CR", "carmen@inmobxyz.pe", "999-111-016", "Activo"),
                new AdminAsesorInmobiliaria("AI17", "Pablo Navarro", "PN", "pablo@inmobxyz.pe", "999-111-017", "Activo"),
                new AdminAsesorInmobiliaria("AI18", "Daniela Flores", "DF", "daniela@inmobxyz.pe", "999-111-018", "Inactivo"),
                new AdminAsesorInmobiliaria("AI19", "Enrique Vargas", "EV", "enrique@inmobxyz.pe", "999-111-019", "Activo"),
                new AdminAsesorInmobiliaria("AI20", "Rosario Jimenez", "RJ", "rosario@inmobxyz.pe", "999-111-020", "Activo"),
                new AdminAsesorInmobiliaria("AI21", "Santiago Medina", "SM", "santiago@inmobxyz.pe", "999-111-021", "Activo"),
                new AdminAsesorInmobiliaria("AI22", "Veronica Castro", "VC", "veronica@inmobxyz.pe", "999-111-022", "Activo"),
                new AdminAsesorInmobiliaria("AI23", "Andres Quintero", "AQ", "andres@inmobxyz.pe", "999-111-023", "Inactivo"),
                new AdminAsesorInmobiliaria("AI24", "Mariela Sánchez", "MS", "mariela@inmobxyz.pe", "999-111-024", "Activo"),
                new AdminAsesorInmobiliaria("AI25", "Felipe Ramírez", "FR", "felipe@inmobxyz.pe", "999-111-025", "Activo"),
                new AdminAsesorInmobiliaria("AI26", "Alejandra Ruiz", "AR", "alejandra@inmobxyz.pe", "999-111-026", "Activo"),
                new AdminAsesorInmobiliaria("AI27", "Cristian Muñoz", "CM", "cristian@inmobxyz.pe", "999-111-027", "Activo"),
                new AdminAsesorInmobiliaria("AI28", "Lorena Pino", "LP", "lorena@inmobxyz.pe", "999-111-028", "Activo"),
                new AdminAsesorInmobiliaria("AI29", "Hector Reyes", "HR", "hector@inmobxyz.pe", "999-111-029", "Activo"),
                new AdminAsesorInmobiliaria("AI30", "Giannina Moreno", "GM", "giannina@inmobxyz.pe", "999-111-030", "Inactivo"),
                new AdminAsesorInmobiliaria("AI31", "Leo Contreras", "LC", "leo@inmobxyz.pe", "999-111-031", "Activo"),
                new AdminAsesorInmobiliaria("AI32", "Natalia Vega", "NV", "natalia@inmobxyz.pe", "999-111-032", "Activo"),
                new AdminAsesorInmobiliaria("AI33", "Oscar Valencia", "OV", "oscar@inmobxyz.pe", "999-111-033", "Activo"),
                new AdminAsesorInmobiliaria("AI34", "Roxana Fuentes", "RF", "roxana@inmobxyz.pe", "999-111-034", "Activo"),
                new AdminAsesorInmobiliaria("AI35", "Wilfredo Gutierrez", "WG", "wilfredo@inmobxyz.pe", "999-111-035", "Activo")
        ));
    }
}

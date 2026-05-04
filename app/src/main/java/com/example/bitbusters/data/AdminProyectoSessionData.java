package com.example.bitbusters.data;

/**
 * Singleton que guarda temporalmente los datos del proyecto
 * mientras el admin navega entre los pasos de creación.
 * Se limpia al guardar o cancelar.
 */
public class AdminProyectoSessionData {

    private static AdminProyectoSessionData instance;

    // Datos del proyecto
    public String nombreProyecto   = "";
    public String descripcion      = "";
    public String direccion        = "";
    public String distrito         = "";
    public String costoSeparacion  = "";
    public String precioTotal      = "";
    public String nombreComercial  = "";
    public String precioPublicado  = "";
    public String fechaEntrega     = "";
    public String estado           = ""; // "En planos", "En preventa", "En venta"

    // Constructor privado — solo se accede por getInstance()
    private AdminProyectoSessionData() {}

    // Obtener instancia única
    public static AdminProyectoSessionData getInstance() {
        if (instance == null) {
            instance = new AdminProyectoSessionData();
        }
        return instance;
    }

    // Limpiar todos los datos al terminar o cancelar
    public void clear() {
        instance            = null;
        nombreProyecto      = "";
        descripcion         = "";
        direccion           = "";
        distrito            = "";
        costoSeparacion     = "";
        precioTotal         = "";
        nombreComercial     = "";
        precioPublicado     = "";
        fechaEntrega        = "";
        estado              = "";
    }
}
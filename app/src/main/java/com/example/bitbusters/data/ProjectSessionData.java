package com.example.bitbusters.data;

import com.example.bitbusters.models.Proyecto;
import java.util.ArrayList;
import java.util.List;

public class ProjectSessionData {

    public static List<Proyecto> getProyectos() {
        List<Proyecto> lista = new ArrayList<>();

        // Departamentos (20 items)
        lista.add(new Proyecto("Torres Unidas",            "S/. 280,000", "4.9", "La Perla, Callao",      "Departamento", "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400"));
        lista.add(new Proyecto("Catalina Ventor",          "S/. 280,000", "4.9", "Santa Catalina, Lima",  "Departamento", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400"));
        lista.add(new Proyecto("Torre Miramar",            "S/. 195,000", "4.9", "Miraflores, Lima",      "Departamento", "https://images.unsplash.com/photo-1574362848149-11496d93a7c7?w=400"));
        lista.add(new Proyecto("Residencial El Park",      "S/. 248,000", "4.8", "San Miguel, Lima",      "Departamento", "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400"));
        lista.add(new Proyecto("Vista Marina Residencial", "S/. 310,000", "4.6", "San Miguel, Lima",      "Departamento", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400"));
        lista.add(new Proyecto("Torres del Sol",           "S/. 195,000", "4.5", "Miraflores, Lima",      "Departamento", "https://images.unsplash.com/photo-1567684014761-b65e2e59b9eb?w=400"));
        lista.add(new Proyecto("Edificio Los Álamos",      "S/. 265,000", "4.7", "Barranco, Lima",        "Departamento", "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400"));
        lista.add(new Proyecto("Residencial Verde",        "S/. 225,000", "4.8", "La Molina, Lima",       "Departamento", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400"));
        lista.add(new Proyecto("Mirador de Surco",         "S/. 305,000", "4.9", "Surco, Lima",           "Departamento", "https://images.unsplash.com/photo-1574362848149-11496d93a7c7?w=400"));
        lista.add(new Proyecto("Alto San Felipe",          "S/. 275,000", "4.7", "Jesús María, Lima",     "Departamento", "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400"));
        lista.add(new Proyecto("Torres Marina Bay",        "S/. 320,000", "4.8", "San Isidro, Lima",      "Departamento", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400"));
        lista.add(new Proyecto("Residencial Andes",        "S/. 245,000", "4.6", "Miraflores, Lima",      "Departamento", "https://images.unsplash.com/photo-1567684014761-b65e2e59b9eb?w=400"));
        lista.add(new Proyecto("Parque Central Dpto",      "S/. 290,000", "4.8", "La Perla, Callao",      "Departamento", "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400"));
        lista.add(new Proyecto("Orquídea Blanca",          "S/. 255,000", "4.7", "Santa Catalina, Lima",  "Departamento", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400"));
        lista.add(new Proyecto("Vistas del Pacífico",      "S/. 330,000", "4.9", "Miraflores, Lima",      "Departamento", "https://images.unsplash.com/photo-1574362848149-11496d93a7c7?w=400"));
        lista.add(new Proyecto("Centro Financiero",        "S/. 310,000", "4.8", "San Isidro, Lima",      "Departamento", "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400"));
        lista.add(new Proyecto("Residencial Platinum",     "S/. 350,000", "4.9", "San Miguel, Lima",      "Departamento", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400"));
        lista.add(new Proyecto("Towers Garden",            "S/. 275,000", "4.7", "Barranco, Lima",        "Departamento", "https://images.unsplash.com/photo-1567684014761-b65e2e59b9eb?w=400"));
        lista.add(new Proyecto("Modern Living Complex",    "S/. 240,000", "4.6", "La Molina, Lima",       "Departamento", "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400"));
        lista.add(new Proyecto("Elite Residences",         "S/. 295,000", "4.8", "Surco, Lima",           "Departamento", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400"));

        // Casas (10 items)
        lista.add(new Proyecto("Casa Linda",               "S/. 235,000", "4.7", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=400"));
        lista.add(new Proyecto("Casa Fuapa",               "S/. 271,000", "4.8", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));
        lista.add(new Proyecto("Los Robles",               "S/. 220,000", "4.8", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=400"));
        lista.add(new Proyecto("Condominio Las Lomas",     "S/. 220,000", "4.7", "Surco, Lima",           "Casa",         "https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?w=400"));
        lista.add(new Proyecto("Casa Campestre",           "S/. 285,000", "4.9", "Chaclacayo, Lima",      "Casa",         "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=400"));
        lista.add(new Proyecto("Mansión del Lago",         "S/. 420,000", "4.9", "Lurín, Lima",           "Casa",         "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));
        lista.add(new Proyecto("Villa Tropical",           "S/. 310,000", "4.8", "Cieneguilla, Lima",     "Casa",         "https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=400"));
        lista.add(new Proyecto("Casa Moderna",             "S/. 265,000", "4.7", "San Isidro, Lima",      "Casa",         "https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?w=400"));
        lista.add(new Proyecto("Residencia Privada",       "S/. 295,000", "4.8", "Miraflores, Lima",      "Casa",         "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=400"));
        lista.add(new Proyecto("Casa Familiar Plus",       "S/. 240,000", "4.6", "Barranco, Lima",        "Casa",         "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));

        // Terrenos (6 items)
        lista.add(new Proyecto("Catalina Sky",             "S/. 320,000", "4.9", "Miraflores, Lima",      "Terreno",      "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400"));
        lista.add(new Proyecto("Los Álamos",               "S/. 180,000", "4.6", "San Miguel, Lima",      "Terreno",      "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400"));
        lista.add(new Proyecto("Terreno Central",          "S/. 250,000", "4.7", "Surco, Lima",           "Terreno",      "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400"));
        lista.add(new Proyecto("Terreno Premium",          "S/. 380,000", "4.9", "San Isidro, Lima",      "Terreno",      "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400"));
        lista.add(new Proyecto("Terreno Desarrollo",       "S/. 290,000", "4.8", "La Molina, Lima",       "Terreno",      "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400"));
        lista.add(new Proyecto("Terreno Inversión",        "S/. 210,000", "4.6", "Chaclacayo, Lima",      "Terreno",      "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400"));

        // Comercial/Oficinas (4 items bonus)
        lista.add(new Proyecto("Centro Comercial Plus",    "S/. 450,000", "4.8", "San Miguel, Lima",      "Comercial",     "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400"));
        lista.add(new Proyecto("Oficinas Ejecutivas",      "S/. 380,000", "4.7", "San Isidro, Lima",      "Comercial",     "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400"));
        lista.add(new Proyecto("Plaza de Negocios",        "S/. 420,000", "4.9", "Miraflores, Lima",      "Comercial",     "https://images.unsplash.com/photo-1574362848149-11496d93a7c7?w=400"));
        lista.add(new Proyecto("Business Complex",         "S/. 500,000", "4.9", "Surco, Lima",           "Comercial",     "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400"));

        return lista;
    }
}
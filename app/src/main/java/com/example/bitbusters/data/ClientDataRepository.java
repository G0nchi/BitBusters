package com.example.bitbusters.data;

import com.example.bitbusters.R;
import com.example.bitbusters.models.Chat;
import com.example.bitbusters.models.ClientAppointment;
import com.example.bitbusters.models.ClientMessage;
import com.example.bitbusters.models.ClientReview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClientDataRepository {

    private ClientDataRepository() {
    }

    public static List<Chat> getChats() {
        return new ArrayList<>(Arrays.asList(
                new Chat("1", "Milano", "Tiene acceso a la piscina", "10:45", "MI", "#4ECDC4", 2, true),
                new Chat("2", "Samuel Ella", "Si, es petfriendly", "11:00", "SE", "#FF6B9D", 0, true),
                new Chat("3", "Santa Lcyua", "Hoy no creo que pueda ver", "12:50", "SL", "#FF8C42", 1, true),
                new Chat("4", "Sandra Sotomayor", "Dependiendo la hora en que se pueda...", "Ayer", "SS", "#9B59B6", 0, false),
                new Chat("5", "Valerai CW", "Muchas gracias por agendar la cita", "Hace 2 dias", "VC", "#27AE60", 0, false)
        ));
    }

    public static List<ClientMessage> getConversation(String contactName) {
        List<ClientMessage> messages = new ArrayList<>();
        messages.add(new ClientMessage("Hola " + contactName + ", tengo interes en el proyecto.", "10:42", true));
        messages.add(new ClientMessage("Hola, con gusto te ayudo con la informacion.", "10:43", false));
        messages.add(new ClientMessage("Se puede tener mascotas en ese departamento?", "10:45", true));
        messages.add(new ClientMessage("Por ahora no, pero tendremos opciones pet friendly pronto.", "10:46", false));
        return messages;
    }

    public static List<ClientAppointment> getAppointments() {
        return new ArrayList<>(Arrays.asList(
                new ClientAppointment(
                        "A1", "Vista Marina Residencial", "San Miguel, Lima", "14 abr", "10:00 AM",
                        "Juan Garcia", "JG", R.color.avatar_blue_light, ClientAppointment.STATUS_CONFIRMED
                ),
                new ClientAppointment(
                        "A2", "Torres del Sol", "Miraflores, Lima", "19 abr", "03:00 PM",
                        "Maria Lopez", "ML", R.color.avatar_pink, ClientAppointment.STATUS_PENDING
                ),
                new ClientAppointment(
                        "A3", "Condominio Los Pinos", "Surco, Lima", "27 mar", "11:00 AM",
                        "Carlos Ruiz", "CR", R.color.avatar_green, ClientAppointment.STATUS_COMPLETED
                ),
                new ClientAppointment(
                        "A4", "Catalina Sky", "Callao, Lima", "10 mar", "09:30 AM",
                        "Milagros Vera", "MV", R.color.avatar_teal, ClientAppointment.STATUS_CANCELED
                )
        ));
    }

    public static List<ClientReview> getReviews() {
        return new ArrayList<>(Arrays.asList(
                new ClientReview("Courtois", 4, "Es muy limpia y grande, un lugar perfecto para estar en familia.", "Hace 10 min"),
                new ClientReview("Kounde", 5, "Me gusto mucho, tiene un estilo moderno y se siente muy segura.", "Hace 25 min"),
                new ClientReview("Kay Swanson", 4, "Tiene mucha iluminacion natural y excelente distribucion.", "Hace 1 h"),
                new ClientReview("Samuel Ella", 5, "Muy comoda y totalmente recomendada para familias.", "Hace 3 h")
        ));
    }
}

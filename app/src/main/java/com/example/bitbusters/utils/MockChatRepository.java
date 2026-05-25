package com.example.bitbusters.utils;

import com.example.bitbusters.activities.asesor.MensajeAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implementación de desarrollo de {@link ChatRepository}.
 *
 * - Singleton: una sola instancia comparte el cache entre aperturas de
 *   ConversacionActivity, por lo que los mensajes enviados persisten
 *   durante toda la sesión de la app.
 * - Datos iniciales: mensajes estáticos por chatId.
 * - Sin red: funciona offline completamente.
 *
 * Para producción: reemplazar por {@link FirestoreChatRepository}.
 */
public class MockChatRepository implements ChatRepository {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static MockChatRepository instance;

    /** Obtener la instancia única (crea una si todavía no existe). */
    public static MockChatRepository getInstance() {
        if (instance == null) instance = new MockChatRepository();
        return instance;
    }

    private MockChatRepository() {}

    /** Mensajes en memoria para cada chat (persistencia en sesión de la app). */
    private final Map<String, List<MensajeAdapter.Mensaje>> cache = new HashMap<>();

    @Override
    public void loadMessages(String chatId, MessagesCallback callback) {
        if (!cache.containsKey(chatId)) {
            cache.put(chatId, buildStaticMessages(chatId));
        }
        // Devolver copia para evitar modificaciones externas
        callback.onMessages(new ArrayList<>(cache.get(chatId)));
    }

    @Override
    public void sendMessage(String chatId, String text, SendCallback callback) {
        if (!cache.containsKey(chatId)) {
            cache.put(chatId, buildStaticMessages(chatId));
        }
        String hora = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        cache.get(chatId).add(new MensajeAdapter.Mensaje(text, hora, true));
        callback.onSuccess();
    }

    @Override
    public void release() { /* sin listeners que liberar en mock */ }

    // ── Datos estáticos por chat ──────────────────────────────────────────────

    private List<MensajeAdapter.Mensaje> buildStaticMessages(String chatId) {
        if (chatId == null) return defaultMsgs();
        switch (chatId) {
            case "1": return chatCarlosMendoza();
            case "2": return chatRosaTorres();
            case "3": return chatAnaLopez();
            case "4": return chatMarcoParedes();
            case "5": return chatJorgeCastro();
            case "6": return chatSandraVega();
            case "7": return chatLuisVargas();
            default:  return defaultMsgs();
        }
    }

    private static MensajeAdapter.Mensaje msg(String t, String h, boolean s) {
        return new MensajeAdapter.Mensaje(t, h, s);
    }

    private List<MensajeAdapter.Mensaje> chatCarlosMendoza() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Buenos días, quería confirmar si la cita del lunes sigue en pie.", "9:45 AM", false));
        m.add(msg("¡Buenos días Carlos! Sí, todo confirmado para el lunes 7 a las 10:30 AM.", "9:48 AM", true));
        m.add(msg("Perfecto. ¿El departamento 302 sigue disponible?", "9:50 AM", false));
        m.add(msg("Sí, está disponible. Te espero con toda la información del proyecto.", "9:51 AM", true));
        m.add(msg("¡Excelente! Ahí estaré puntual 🤝", "10:02 AM", false));
        m.add(msg("¿Podemos confirmar la cita del lunes?", "10:32 AM", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> chatRosaTorres() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Hola Rosa, ¿cómo estuvo su visita al departamento 108?", "11:00 AM", true));
        m.add(msg("¡Muy bien! Me encantó la vista y la distribución del espacio.", "11:05 AM", false));
        m.add(msg("Nos alegra mucho. ¿Tiene alguna consulta adicional?", "11:06 AM", true));
        m.add(msg("Sí, ¿cuánto tiempo tarda el proceso de separación?", "11:10 AM", false));
        m.add(msg("El proceso toma entre 24 y 48 horas hábiles una vez registrado el pago.", "11:12 AM", true));
        m.add(msg("Muchas gracias por la atención 🙏", "9:15 AM", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> chatAnaLopez() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Buenas tardes Ana, le recuerdo su cita agendada para el martes 8 a las 3 PM.", "2:00 PM", true));
        m.add(msg("Gracias por el recordatorio. ¿Dónde es exactamente la visita?", "2:15 PM", false));
        m.add(msg("En Torres del Sol, Miraflores. Le envío la ubicación exacta por aquí.", "2:17 PM", true));
        m.add(msg("¿Hay estacionamiento disponible para visitantes?", "2:20 PM", false));
        m.add(msg("Sí, contamos con estacionamiento de visita en el sótano.", "2:22 PM", true));
        m.add(msg("Perfecto, quedamos el martes entonces.", "Ayer", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> chatMarcoParedes() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Buenas tardes Marco, ¿tiene alguna consulta sobre el Dpto 210?", "3:00 PM", true));
        m.add(msg("Sí, me interesa saber sobre las áreas comunes del edificio.", "3:05 PM", false));
        m.add(msg("El proyecto cuenta con gimnasio, sala de usos múltiples y terraza.", "3:07 PM", true));
        m.add(msg("¿Hay zona de parrillas también?", "3:10 PM", false));
        m.add(msg("Sí, en la terraza hay zona de parrillas con vista panorámica.", "3:12 PM", true));
        m.add(msg("¿El departamento tiene estacionamiento?", "Ayer", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> chatJorgeCastro() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Hola Jorge, fue un placer mostrarle el Dpto 601. ¿Qué le pareció?", "5:00 PM", true));
        m.add(msg("Muy interesante, los acabados son de primera.", "5:10 PM", false));
        m.add(msg("Nos alegra. ¿Tiene alguna duda sobre el financiamiento?", "5:12 PM", true));
        m.add(msg("Tengo que consultarlo con mi esposa. Le cuento el lunes.", "5:20 PM", false));
        m.add(msg("Claro, tómese el tiempo necesario. Estoy a su disposición.", "5:21 PM", true));
        m.add(msg("Gracias, lo voy a pensar con mi familia.", "Lun", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> chatSandraVega() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Buenos días Sandra, ¿en qué le puedo ayudar?", "9:00 AM", true));
        m.add(msg("Me interesa el Dpto 415. ¿Cuántos dormitorios tiene?", "9:05 AM", false));
        m.add(msg("El Dpto 415 tiene 3 dormitorios, 2 baños y 85 m² de área total.", "9:07 AM", true));
        m.add(msg("¿Tienen planos disponibles para revisarlos con mi arquitecto?", "9:12 AM", false));
        m.add(msg("Sí, le puedo enviar los planos completos. ¿Me da su correo?", "9:13 AM", true));
        m.add(msg("¿Pueden enviarme los planos del dpto 4…", "Dom", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> chatLuisVargas() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Hola Luis, lamento que no haya podido asistir a la cita de ayer.", "10:00 AM", true));
        m.add(msg("Disculpe, tuve un imprevisto de último momento.", "10:05 AM", false));
        m.add(msg("No se preocupe. ¿Le gustaría reagendar para la próxima semana?", "10:06 AM", true));
        m.add(msg("Sí, ¿tienen disponibilidad el miércoles?", "10:10 AM", false));
        m.add(msg("Sí, el miércoles a las 11 AM o a las 4 PM, ¿cuál le viene mejor?", "10:12 AM", true));
        m.add(msg("Ok, reagendamos para la próxima semana.", "Sáb", false));
        return m;
    }

    private List<MensajeAdapter.Mensaje> defaultMsgs() {
        List<MensajeAdapter.Mensaje> m = new ArrayList<>();
        m.add(msg("Hola, ¿en qué le puedo ayudar?", "9:00 AM", true));
        m.add(msg("Tengo una consulta sobre el proyecto.", "9:05 AM", false));
        m.add(msg("Con gusto, cuénteme.", "9:06 AM", true));
        return m;
    }
}

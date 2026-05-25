package com.example.bitbusters.utils;

import androidx.annotation.NonNull;

import com.example.bitbusters.models.ProyectoApi;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit listo para usar en toda la app.
 *
 * Arquitectura:
 *   OkHttpClient
 *     ├── MockInterceptor  → responde con JSON falso (sin servidor real)
 *     └── LoggingInterceptor → loguea requests/responses en Logcat
 *   Retrofit
 *     ├── BaseURL: https://api.bitbusters.com/v1/
 *     └── GsonConverterFactory → deserializa JSON a objetos Java
 *
 * Para producción: eliminar MockInterceptor y apuntar la BASE_URL al servidor.
 */
public class ApiClient {

    private static final String BASE_URL = "https://api.bitbusters.com/v1/";

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new MockInterceptor())  // simula backend
                    .addInterceptor(logging)                // log en Logcat
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    /** Devuelve el servicio tipado. */
    public static BitBustersApiService getApiService() {
        return getInstance().create(BitBustersApiService.class);
    }

    // ── MockInterceptor ───────────────────────────────────────────────────────

    /**
     * Intercepta requests HTTP y devuelve respuestas JSON prefabricadas.
     * Simula el backend durante el desarrollo sin necesidad de servidor real.
     *
     * Patrón demostrado en Clase 04.2; en producción se elimina este
     * interceptor y OkHttp envía las requests a la BASE_URL real.
     */
    static class MockInterceptor implements Interceptor {

        private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            String path     = request.url().encodedPath();

            if (path.contains("/proyectos")) {
                return mockProyectos(request);
            }

            // Para cualquier otro endpoint: dejar pasar (fallará en demo sin internet,
            // pero la arquitectura está lista para conectar un servidor real)
            return chain.proceed(request);
        }

        private Response mockProyectos(Request request) {
            List<ProyectoApi> proyectos = Arrays.asList(
                new ProyectoApi(1,
                    "Vista Marina Residencial", "San Miguel, Lima",
                    "S/ 320,000", "En Venta", "4.9",
                    "Departamento", "marina"),
                new ProyectoApi(2,
                    "Torres del Sol", "Miraflores, Lima",
                    "S/ 450,000", "Preventa", "4.8",
                    "Departamento", "torres"),
                new ProyectoApi(3,
                    "Condominio Los Pinos", "Surco, Lima",
                    "S/ 580,000", "En Planos", "4.7",
                    "Villa", "pinos")
            );

            String json = new Gson().toJson(proyectos);
            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(json, JSON))
                    .build();
        }
    }
}

package com.example.bitbusters.utils;

import com.example.bitbusters.models.ProyectoApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interfaz Retrofit que define los endpoints del backend BitBusters.
 *
 * Retrofit genera automáticamente la implementación en tiempo de ejecución.
 * Cada anotación (@GET, @POST…) corresponde a un endpoint REST.
 *
 * En esta fase se usa MockInterceptor (ApiClient) para simular respuestas
 * sin servidor real; en producción sólo se cambia la BASE_URL.
 */
public interface BitBustersApiService {

    /**
     * Obtiene la lista de proyectos inmobiliarios.
     *
     * GET /api/v1/proyectos
     * GET /api/v1/proyectos?tipo=Departamento   (filtrado en servidor)
     */
    @GET("proyectos")
    Call<List<ProyectoApi>> getProyectos(@Query("tipo") String tipo);

    /** Sin filtro — devuelve todos los proyectos. */
    @GET("proyectos")
    Call<List<ProyectoApi>> getAllProyectos();
}

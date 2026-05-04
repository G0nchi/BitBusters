package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class AsesorOfertasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_ofertas);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_ofertas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new OfertaAdapter(buildOfertas()));
    }

    private List<Oferta> buildOfertas() {
        return Arrays.asList(
            new Oferta("Descuento de lanzamiento",
                "Vista Marina Residencial", "10% OFF",
                "Aplica a unidades seleccionadas en piso 3 y 4.",
                "01 Abr – 30 Abr 2025", "Activa",
                "#DFFBEC", "#186A3B", "#8BC83F"),
            new Oferta("Financiamiento especial",
                "Torres del Sol", "0% interés 6 meses",
                "Financiamiento directo sin intereses para los primeros 3 compradores.",
                "15 Abr – 15 May 2025", "Activa",
                "#DFFBEC", "#186A3B", "#1F4C6B"),
            new Oferta("Bono de separación",
                "Condominio Los Pinos", "S/ 5,000 BONO",
                "Bono descontable del precio final al firmar contrato dentro de 30 días.",
                "01 Abr – 31 May 2025", "Activa",
                "#DFFBEC", "#186A3B", "#1B5E20"),
            new Oferta("Descuento por referido",
                "Vista Marina Residencial", "5% OFF",
                "El cliente referido recibe 5% de descuento al concretar la compra.",
                "01 Mar – 30 Jun 2025", "Activa",
                "#DFFBEC", "#186A3B", "#8BC83F"),
            new Oferta("Preventa exclusiva",
                "Torres del Sol · Fase 2", "Precio preferencial",
                "Unidades de preventa con precio 12% inferior al precio de lanzamiento.",
                "10 Abr – 10 May 2025", "Próximamente",
                "#FFF3DC", "#9A5700", "#FF8F00")
        );
    }

    static class Oferta {
        final String titulo, proyecto, descuento, descripcion, vigencia, estado;
        final String estadoBg, estadoText, stripeColor;

        Oferta(String titulo, String proyecto, String descuento, String descripcion,
               String vigencia, String estado, String estadoBg, String estadoText, String stripeColor) {
            this.titulo = titulo;
            this.proyecto = proyecto;
            this.descuento = descuento;
            this.descripcion = descripcion;
            this.vigencia = vigencia;
            this.estado = estado;
            this.estadoBg = estadoBg;
            this.estadoText = estadoText;
            this.stripeColor = stripeColor;
        }
    }

    static class OfertaAdapter extends RecyclerView.Adapter<OfertaAdapter.VH> {
        private final List<Oferta> items;

        OfertaAdapter(List<Oferta> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oferta_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Oferta o = items.get(position);
            h.tvTitulo.setText(o.titulo);
            h.tvProyecto.setText(o.proyecto);
            h.tvDescuento.setText(o.descuento);
            h.tvDescripcion.setText(o.descripcion);
            h.tvVigencia.setText("Vigente: " + o.vigencia);
            h.tvEstado.setText(o.estado);
            h.tvEstado.setBackgroundColor(Color.parseColor(o.estadoBg));
            h.tvEstado.setTextColor(Color.parseColor(o.estadoText));
            h.vStripe.setBackgroundColor(Color.parseColor(o.stripeColor));
            h.btnCompartir.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Oferta compartida con el cliente", Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            View vStripe;
            TextView tvTitulo, tvEstado, tvProyecto, tvDescuento, tvDescripcion, tvVigencia;
            MaterialButton btnCompartir;

            VH(View v) {
                super(v);
                vStripe = v.findViewById(R.id.v_stripe);
                tvTitulo = v.findViewById(R.id.tv_titulo);
                tvEstado = v.findViewById(R.id.tv_estado);
                tvProyecto = v.findViewById(R.id.tv_proyecto);
                tvDescuento = v.findViewById(R.id.tv_descuento);
                tvDescripcion = v.findViewById(R.id.tv_descripcion);
                tvVigencia = v.findViewById(R.id.tv_vigencia);
                btnCompartir = v.findViewById(R.id.btn_compartir);
            }
        }
    }
}

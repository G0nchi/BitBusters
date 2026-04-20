package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MensajesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);
        setupConversationClicks();
        setupBottomNav();
    }

    private void setupConversationClicks() {
        Intent intent = new Intent(this, ConversacionActivity.class);
        if (findViewById(R.id.conv_carlos) != null)
            findViewById(R.id.conv_carlos).setOnClickListener(v -> startActivity(intent));
        if (findViewById(R.id.conv_jorge) != null)
            findViewById(R.id.conv_jorge).setOnClickListener(v -> startActivity(intent));
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_chat);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
                finish();
            }
            return true;
        });
    }
}

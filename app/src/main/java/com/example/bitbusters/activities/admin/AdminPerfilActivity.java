package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import com.example.bitbusters.R;

public class AdminPerfilActivity extends AdminMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_perfil);
        setupBottomNavigation(R.id.nav_perfil);
    }
}

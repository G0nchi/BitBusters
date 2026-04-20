package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import com.example.bitbusters.R;

public class AdminProyectosActivity extends AdminMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_proyectos);
        setupBottomNavigation(R.id.nav_proyectos);
    }
}

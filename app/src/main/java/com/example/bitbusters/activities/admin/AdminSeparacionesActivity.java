package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import com.example.bitbusters.R;

public class AdminSeparacionesActivity extends AdminMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_separaciones);
        setupBottomNavigation(R.id.nav_separaciones);
    }
}

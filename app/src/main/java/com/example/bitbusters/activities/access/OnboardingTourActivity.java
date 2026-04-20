package com.example.bitbusters.activities.access;

import android.graphics.Typeface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.cliente.HomeActivity;

public class OnboardingTourActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding_tour);

        TextView titleLine2 = findViewById(R.id.titleLine2);
        String line2 = getString(R.string.onboarding_title_line_2_a) + getString(R.string.onboarding_title_line_2_b);
        SpannableString styledLine2 = new SpannableString(line2);
        int accentStart = line2.indexOf(getString(R.string.onboarding_title_line_2_b));
        if (accentStart >= 0) {
            int accentEnd = accentStart + getString(R.string.onboarding_title_line_2_b).length();
            styledLine2.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.brand_blue_dark)),
                    accentStart,
                    accentEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            styledLine2.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    accentStart,
                    accentEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        titleLine2.setText(styledLine2);

        findViewById(R.id.skipButton).setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingTourActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

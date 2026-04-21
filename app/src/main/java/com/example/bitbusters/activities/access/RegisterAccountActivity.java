package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;

public class RegisterAccountActivity extends AppCompatActivity {

    private EditText fullNameInput, docNumberInput, birthDateInput, emailInput, phoneInput, addressInput;
    private AutoCompleteTextView docTypeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_account);

        fullNameInput = findViewById(R.id.fullNameInput);
        docTypeInput = findViewById(R.id.docTypeInput);
        docNumberInput = findViewById(R.id.docNumberInput);
        birthDateInput = findViewById(R.id.birthDateInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);

        setupDocTypeSelector();

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton nextButton = findViewById(R.id.nextButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                if (validateFields()) {
                    startActivity(new Intent(this, RegisterOtpActivity.class));
                }
            });
        }

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupDocTypeSelector() {
        String[] docTypes = getResources().getStringArray(R.array.doc_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, docTypes);
        docTypeInput.setAdapter(adapter);
        
        // Show dropdown when clicked
        docTypeInput.setOnClickListener(v -> docTypeInput.showDropDown());
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (fullNameInput.getText().toString().trim().isEmpty()) {
            fullNameInput.setError(getString(R.string.validation_required));
            isValid = false;
        }
        if (docTypeInput.getText().toString().trim().isEmpty()) {
            docTypeInput.setError(getString(R.string.validation_required));
            isValid = false;
        }
        if (docNumberInput.getText().toString().trim().isEmpty()) {
            docNumberInput.setError(getString(R.string.validation_required));
            isValid = false;
        }
        if (birthDateInput.getText().toString().trim().isEmpty()) {
            birthDateInput.setError(getString(R.string.validation_required));
            isValid = false;
        }
        
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.validation_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.validation_invalid_email));
            isValid = false;
        }

        String phone = phoneInput.getText().toString().trim();
        if (phone.isEmpty()) {
            phoneInput.setError(getString(R.string.validation_required));
            isValid = false;
        } else if (phone.length() < 9) {
            phoneInput.setError(getString(R.string.validation_invalid_phone));
            isValid = false;
        }

        if (addressInput.getText().toString().trim().isEmpty()) {
            addressInput.setError(getString(R.string.validation_required));
            isValid = false;
        }

        return isValid;
    }
}

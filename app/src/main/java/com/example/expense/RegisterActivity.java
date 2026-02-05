package com.example.expense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.data.User;
import com.example.expense.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button registerButton;
    private TextView loginTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        nameEditText = findViewById(R.id.editTextName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);
        loginTextView = findViewById(R.id.textViewLogin);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> performRegister());

        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void performRegister() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUtils.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Save user data to Firestore
                    String userId = FirebaseUtils.getAuth().getCurrentUser() != null ? 
                        FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
                    if (userId != null) {
                        User user = new User(
                            userId,
                            name,
                            email,
                            new Timestamp(System.currentTimeMillis() / 1000, 0)
                        );

                        FirebaseUtils.getFirestore().collection(FirebaseUtils.USERS_COLLECTION)
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(exception -> {
                                Toast.makeText(this, "Failed to save user data: " + exception.getMessage(), 
                                    Toast.LENGTH_LONG).show();
                            });
                    }
                } else {
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : "Registration failed";
                    Toast.makeText(this, "Registration failed: " + errorMessage, 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
}
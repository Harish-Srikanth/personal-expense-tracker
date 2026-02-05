package com.example.expense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.data.User;
import com.example.expense.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private Button editProfileButton;
    private Button changePasswordButton;
    private Button logoutButton;
    private ImageButton buttonBack;

    private boolean isEditing = false;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Set the content view and handle any potential inflation errors
            setContentView(R.layout.activity_profile);

            // Initialize views safely
            initViews();
            setupClickListeners();
            
            // Load profile data after UI initialization
            // Don't check authentication here as it might be valid during async operations
            loadUserProfile();
        } catch (Exception e) {
            // Show error message and finish
            try {
                Toast.makeText(this, "Error initializing profile: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            } catch (Exception toastException) {
                // If even the toast fails, just finish
            }
            finish(); // Close the activity if there's an initialization error
        }
    }

    private void initViews() {
        try {
            imageViewProfile = findViewById(R.id.imageViewProfile);
            nameEditText = findViewById(R.id.editTextName);
            emailEditText = findViewById(R.id.editTextEmail);
            editProfileButton = findViewById(R.id.buttonEditProfile);
            changePasswordButton = findViewById(R.id.buttonChangePassword);
            logoutButton = findViewById(R.id.buttonLogout);
            buttonBack = findViewById(R.id.buttonBack);

            // Verify key views are not null and handle appropriately
            if (nameEditText == null) {
                Toast.makeText(this, "Error: Name field not found", Toast.LENGTH_LONG).show();
            }
            if (emailEditText == null) {
                Toast.makeText(this, "Error: Email field not found", Toast.LENGTH_LONG).show();
            }
            if (buttonBack == null) {
                Toast.makeText(this, "Error: Back button not found", Toast.LENGTH_LONG).show();
            }
            
            // Initially disable buttons until profile is loaded
            if (editProfileButton != null) {
                editProfileButton.setEnabled(false);
                editProfileButton.setText("Edit Profile");
            }
            if (changePasswordButton != null) {
                changePasswordButton.setEnabled(false);
            }
            if (logoutButton != null) {
                logoutButton.setEnabled(false);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadUserProfile() {
        try {
            String userId = FirebaseUtils.getAuth().getCurrentUser() != null ?
                FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
            if (userId == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
                // Don't finish here, let the user continue but show the issue
                return;
            }

            FirebaseUtils.getFirestore().collection(FirebaseUtils.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    runOnUiThread(() -> {
                        if (document.exists()) {
                            currentUser = document.toObject(User.class);
                            if (currentUser != null) {
                                // Safely update UI components
                                if (nameEditText != null) {
                                    nameEditText.setText(currentUser.getName());
                                    nameEditText.setEnabled(false); // Initially disable editing
                                }
                                if (emailEditText != null) {
                                    emailEditText.setText(currentUser.getEmail());
                                    emailEditText.setEnabled(false); // Email is usually not editable
                                }
                                
                                // Enable buttons after profile is loaded
                                if (editProfileButton != null) {
                                    editProfileButton.setEnabled(true);
                                    editProfileButton.setText("Edit Profile");
                                }
                                if (changePasswordButton != null) {
                                    changePasswordButton.setEnabled(true);
                                }
                                if (logoutButton != null) {
                                    logoutButton.setEnabled(true);
                                }
                            } else {
                                Toast.makeText(this, "User profile data is empty", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "User profile not found", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(exception -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to load profile: " + exception.getMessage(),
                            Toast.LENGTH_LONG).show();
                    });
                });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error loading profile: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
        }
    }

    private void setupClickListeners() {
        try {
            if (buttonBack != null) {
                buttonBack.setOnClickListener(v -> finish());
            }

            if (editProfileButton != null) {
                editProfileButton.setOnClickListener(v -> {
                    // Check authentication before allowing edit
                    if (FirebaseUtils.getAuth().getCurrentUser() == null) {
                        Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (isEditing) {
                        // Save profile changes
                        updateProfile();
                    } else {
                        // Enable editing
                        enableEditMode();
                    }
                });
            }

            if (changePasswordButton != null) {
                changePasswordButton.setOnClickListener(v -> {
                    // Check authentication before allowing password change
                    if (FirebaseUtils.getAuth().getCurrentUser() == null) {
                        Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
                        return;
                    }
                    showChangePasswordDialog();
                });
            }

            if (logoutButton != null) {
                logoutButton.setOnClickListener(v -> {
                    // Check authentication before logout
                    if (FirebaseUtils.getAuth().getCurrentUser() == null) {
                        Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
                        return;
                    }
                    showLogoutConfirmationDialog();
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    private void enableEditMode() {
        isEditing = true;
        if (nameEditText != null) {
            nameEditText.setEnabled(true);
            nameEditText.requestFocus();
        }
        if (editProfileButton != null) {
            editProfileButton.setText("Save Profile");
        }
    }

    private void disableEditMode() {
        isEditing = false;
        if (nameEditText != null) {
            nameEditText.setEnabled(false);
        }
        if (editProfileButton != null) {
            editProfileButton.setText("Edit Profile");
        }
    }

    private void updateProfile() {
        // Verify user is authenticated
        if (FirebaseUtils.getAuth().getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (nameEditText == null) {
            Toast.makeText(this, "Name field not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = nameEditText.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ?
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("name", newName);

        FirebaseUtils.getFirestore().collection(FirebaseUtils.USERS_COLLECTION)
            .document(userId)
            .update(updatedUser)
            .addOnSuccessListener(aVoid -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    if (currentUser != null) {
                        // Update current user object
                        currentUser.setName(newName);
                    }
                    disableEditMode();
                });
            })
            .addOnFailureListener(exception -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to update profile: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
            });
    }

    private void showChangePasswordDialog() {
        // Verify user is authenticated
        if (FirebaseUtils.getAuth().getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            TextInputEditText oldPasswordEditText = dialogView.findViewById(R.id.editTextOldPassword);
            TextInputEditText newPasswordEditText = dialogView.findViewById(R.id.editTextNewPassword);

            if (oldPasswordEditText == null || newPasswordEditText == null) {
                Toast.makeText(this, "Error loading password fields", Toast.LENGTH_LONG).show();
                return;
            }

            new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    String oldPassword = oldPasswordEditText.getText().toString().trim();
                    String newPassword = newPasswordEditText.getText().toString().trim();

                    if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Re-authenticate user before changing password
                    FirebaseAuth auth = FirebaseUtils.getAuth();
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String email = auth.getCurrentUser().getEmail();
                    if (email == null) {
                        Toast.makeText(this, "Email not available", Toast.LENGTH_LONG).show();
                        return;
                    }

                    com.google.firebase.auth.AuthCredential credential =
                        EmailAuthProvider.getCredential(email, oldPassword);

                    auth.getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(reauthTask -> {
                            runOnUiThread(() -> {
                                if (reauthTask.isSuccessful()) {
                                    auth.getCurrentUser().updatePassword(newPassword)
                                        .addOnCompleteListener(updateTask -> {
                                            runOnUiThread(() -> {
                                                if (updateTask.isSuccessful()) {
                                                    Toast.makeText(this, "Password changed successfully",
                                                        Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String errorMessage = updateTask.getException() != null ?
                                                        updateTask.getException().getMessage() : "Unknown error";
                                                    Toast.makeText(this, "Failed to change password: " + errorMessage,
                                                        Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        });
                                } else {
                                    String errorMessage = reauthTask.getException() != null ?
                                        reauthTask.getException().getMessage() : "Authentication failed";
                                    Toast.makeText(this, "Authentication failed: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                                }
                            });
                        });
                })
                .setNegativeButton("Cancel", null)
                .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing password dialog: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        // Verify user is authenticated
        if (FirebaseUtils.getAuth().getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUtils.getAuth().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing logout dialog: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }
}
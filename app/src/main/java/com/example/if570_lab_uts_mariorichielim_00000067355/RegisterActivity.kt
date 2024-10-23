package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nameInput = findViewById<TextInputEditText>(R.id.name_input_field)
        val emailInput = findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input_field)
        val submitButton = findViewById<Button>(R.id.go_home_button)
        val backButton = findViewById<Button>(R.id.go_login_button)

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val name = nameInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            finish() // Go back to LoginActivity after successful registration
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

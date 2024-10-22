package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide Bottom Navigation when logging in
        (activity as MainActivity).hideBottomNavigation()

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Setup UI components
        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password_input_field)
        val registerButton = view.findViewById<Button>(R.id.register_button)
        val loginButton = view.findViewById<Button>(R.id.submit_button)

        // Navigate to RegisterFragment when clicking on register
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password are required", Toast.LENGTH_SHORT).show()
            } else {
                // Firebase Authentication for login
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Navigate to MainFragment after login success
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
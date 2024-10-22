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
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val registerButton = view.findViewById<Button>(R.id.submit_button)
        registerButton.setOnClickListener {
            val email = view.findViewById<TextInputEditText>(R.id.email_input_field).text.toString()
            val password = view.findViewById<TextInputEditText>(R.id.password_input_field).text.toString()
            val userName = view.findViewById<TextInputEditText>(R.id.name_input_field).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)  // Set the user's display name
                                .build()

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                if (it.isSuccessful) {
                                    findNavController().navigate(R.id.loginFragment)
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

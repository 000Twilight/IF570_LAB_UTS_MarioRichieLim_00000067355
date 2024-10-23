package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI components
        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val emailTextView = view.findViewById<TextView>(R.id.profile_email)
        val statusTextView = view.findViewById<TextView>(R.id.profile_status)
        val nameInput = view.findViewById<TextInputEditText>(R.id.profile_name_input)
        val nimInput = view.findViewById<TextInputEditText>(R.id.profile_nim_input)
        val saveButton = view.findViewById<Button>(R.id.profile_save_button)

        // Get the current user
        val user = auth.currentUser

        if (user != null) {
            // Set user's email
            emailTextView.text = user.email ?: "No Email"

            // Load existing profile data
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nameTextView.text = document.getString("name") ?: "No Name"
                        nameInput.setText(document.getString("name"))
                        nimInput.setText(document.getString("nim"))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }

            // Check today's attendance status
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            firestore.collection("attendance")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("date", currentDate)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // No attendance record for today
                        statusTextView.text = "No attendance for today"
                    } else {
                        // Check if it's Clock In or Clock Out
                        val attendance = documents.first().toObject(Attendance::class.java)
                        statusTextView.text = if (attendance.clockIn) "Clocked In" else "Not Clocked In"
                    }
                }
                .addOnFailureListener { e ->
                    statusTextView.text = "Failed to fetch status"
                }

            // Save profile data
            saveButton.setOnClickListener {
                val name = nameInput.text.toString().trim()
                val nim = nimInput.text.toString().trim()

                if (name.isEmpty() || nim.isEmpty()) {
                    Toast.makeText(context, "Name and NIM are required", Toast.LENGTH_SHORT).show()
                } else {
                    val userProfile = hashMapOf(
                        "name" to name,
                        "nim" to nim
                    )

                    firestore.collection("users").document(user.uid).set(userProfile)
                        .addOnSuccessListener {
                            nameTextView.text = name
                            Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
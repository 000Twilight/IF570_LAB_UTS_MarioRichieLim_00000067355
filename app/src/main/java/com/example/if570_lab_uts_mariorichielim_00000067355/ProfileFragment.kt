package com.example.if570_lab_uts_mariorichielim_00000067355

import android.content.Context
import android.content.Intent
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
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()

        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val emailTextView = view.findViewById<TextView>(R.id.profile_email)
        val statusTextView = view.findViewById<TextView>(R.id.profile_status)
        val nameInput = view.findViewById<TextInputEditText>(R.id.profile_name_input)
        val nimInput = view.findViewById<TextInputEditText>(R.id.profile_nim_input)
        val saveButton = view.findViewById<Button>(R.id.profile_save_button)
        val logoutButton = view.findViewById<Button>(R.id.profile_logout_button)

        val user = auth.currentUser

        if (user != null) {
            emailTextView.text = user.email ?: "No Email"

            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nameTextView.text = document.getString("name") ?: "No Name"
                        nameInput.setText(document.getString("name"))
                        nimInput.setText(document.getString("nim"))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }

            AttendanceCheck.checkAttendanceStatus(requireContext(), storageRef) { isClockIn, isClockOut ->
                statusTextView.text = when {
                    isClockIn && !isClockOut -> "Clocked In"
                    isClockIn && isClockOut -> "Clocked Out"
                    else -> "No attendance for today"
                }
            }

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
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            logoutButton.setOnClickListener {
                auth.signOut()

                val sharedPreferences = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("IS_LOGGED_IN", false)
                editor.apply()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                requireActivity().finish()
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
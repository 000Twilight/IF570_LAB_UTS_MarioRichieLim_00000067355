package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI components
        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val emailTextView = view.findViewById<TextView>(R.id.profile_email)
        val statusTextView = view.findViewById<TextView>(R.id.profile_status)

        // Get the current user
        val user = auth.currentUser

        if (user != null) {
            // Set user's name and email
            nameTextView.text = user.displayName ?: "No Name"
            emailTextView.text = user.email ?: "No Email"

            // Check today's attendance status
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            firestore.collection("attendances")
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
                        statusTextView.text = attendance.status
                    }
                }
                .addOnFailureListener { e ->
                    statusTextView.text = "Failed to fetch status"
                }
        } else {
            // User not logged in
            nameTextView.text = "Unknown User"
            emailTextView.text = "Unknown Email"
            statusTextView.text = "No attendance status"
        }
    }
}

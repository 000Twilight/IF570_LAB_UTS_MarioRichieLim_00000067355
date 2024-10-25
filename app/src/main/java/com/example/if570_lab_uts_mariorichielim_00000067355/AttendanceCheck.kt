package com.example.if570_lab_uts_mariorichielim_00000067355

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

object AttendanceCheck {
    fun checkAttendanceStatus(
        context: Context,
        storageRef: FirebaseStorage,
        onStatusChecked: (isClockIn: Boolean, isClockOut: Boolean) -> Unit
    ) {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d("AttendanceUtils", "No authenticated user found")
            Toast.makeText(context, "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val userName = user.email?.replace(".", "_") ?: "Unknown User"
        val storagePath = "absensi/${userName}/"

        Log.d("AttendanceUtils", "Checking attendance status for user: $userName on date: $currentDate")

        storageRef.reference.child(storagePath).listAll()
            .addOnSuccessListener { result ->
                var clockInFound = false
                var clockOutFound = false

                for (fileRef in result.items) {
                    val fileName = fileRef.name
                    Log.d("AttendanceUtils", "Found file: $fileName")
                    if (fileName.contains(currentDate) && fileName.contains("clock_in")) {
                        clockInFound = true
                        Log.d("AttendanceUtils", "Clock-in file found: $fileName")
                    }
                    if (fileName.contains(currentDate) && fileName.contains("clock_out")) {
                        clockOutFound = true
                        Log.d("AttendanceUtils", "Clock-out file found: $fileName")
                    }
                }

                onStatusChecked(clockInFound, clockOutFound)
            }
            .addOnFailureListener { e ->
                Log.e("AttendanceUtils", "Error checking attendance", e)
                Toast.makeText(context, "Error checking attendance: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
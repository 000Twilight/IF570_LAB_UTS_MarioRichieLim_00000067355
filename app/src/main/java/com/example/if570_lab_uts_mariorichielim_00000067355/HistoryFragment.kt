package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: AttendanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        historyAdapter = AttendanceAdapter(emptyList())
        recyclerView.adapter = historyAdapter

        loadAttendanceHistory()
    }

    fun refreshAttendanceHistory() {
        loadAttendanceHistory()
    }

    private fun loadAttendanceHistory() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d("HistoryFragment", "No authenticated user found")
            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val email = user.email?.replace(".", "_") ?: "unknown_user"
        val storagePath = "absensi/${email}/"

        FirebaseStorage.getInstance().reference.child(storagePath).listAll()
            .addOnSuccessListener { result ->
                val attendanceList = mutableListOf<Attendance>()

                val inputDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val inputTimeFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
                val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                for (fileRef in result.items) {
                    val fileName = fileRef.name
                    val parts = fileName.split("_")
                    if (parts.size >= 6) {
                        val date = parts[2]
                        val time = parts[3]

                        val parsedDate = inputDateFormat.parse(date)
                        val formattedDate = outputDateFormat.format(parsedDate)

                        val parsedTime = inputTimeFormat.parse(time)
                        val formattedTime = outputTimeFormat.format(parsedTime)

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            attendanceList.add(Attendance(date = formattedDate, time = formattedTime, photoUrl = uri.toString()))
                        }
                    }
                }

                val hardcodedAttendanceList = listOf(
                    Attendance(date = "2024-10-01", time = "08:00", photoUrl = "https://dummyimage.com/800x600/ff0000/fff"),
                    Attendance(date = "2024-10-01", time = "10:00", photoUrl = "https://dummyimage.com/700x500/ff6600/fff"),
                    Attendance(date = "2024-10-05", time = "14:00", photoUrl = "https://dummyimage.com/800x600/ff0000/fff"),
                    Attendance(date = "2024-10-05", time = "18:05", photoUrl = "https://dummyimage.com/450x350/00cccc/000"),
                    Attendance(date = "2024-10-10", time = "12:10", photoUrl = "https://dummyimage.com/750x550/996633/fff")
                )
                attendanceList.addAll(hardcodedAttendanceList)

                attendanceList.sortWith(compareByDescending<Attendance> { it.date }.thenByDescending { it.time })

                historyAdapter.updateData(attendanceList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load attendance history", Toast.LENGTH_SHORT).show()
            }
    }
}
package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: AttendanceAdapter
    private val handler = Handler(Looper.getMainLooper())

    private val historyRunnable = object : Runnable {
        override fun run() {
            loadAttendanceHistory()
            handler.postDelayed(this, 2000)
        }
    }

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

    override fun onResume() {
        super.onResume()
        handler.post(historyRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(historyRunnable)
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
                var processedItems = 0

                for (fileRef in result.items) {
                    val fileName = fileRef.name
                    val date: String
                    val time: String

                    if (fileName.contains("clock_in")) {
                        date = fileName.substringBefore("_clock_in").takeLast(15).substring(0, 4) + "-" +
                                fileName.substringBefore("_clock_in").takeLast(15).substring(4, 6) + "-" +
                                fileName.substringBefore("_clock_in").takeLast(15).substring(6, 8)
                        time = fileName.substringBefore("_clock_in").takeLast(6).substring(0, 2) + ":" +
                                fileName.substringBefore("_clock_in").takeLast(6).substring(2, 4)
                    } else if (fileName.contains("clock_out")) {
                        date = fileName.substringBefore("_clock_out").takeLast(15).substring(0, 4) + "-" +
                                fileName.substringBefore("_clock_out").takeLast(15).substring(4, 6) + "-" +
                                fileName.substringBefore("_clock_out").takeLast(15).substring(6, 8)
                        time = fileName.substringBefore("_clock_out").takeLast(6).substring(0, 2) + ":" +
                                fileName.substringBefore("_clock_out").takeLast(6).substring(2, 4)
                    } else {
                        processedItems++
                        continue
                    }

                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        attendanceList.add(Attendance(date = date, time = time, photoUrl = uri.toString()))
                        processedItems++
                        if (processedItems == result.items.size) {
                            updateAdapter(attendanceList)
                        }
                    }.addOnFailureListener {
                        processedItems++
                        if (processedItems == result.items.size) {
                            updateAdapter(attendanceList)
                        }
                    }
                }

                if (result.items.isEmpty()) {
                    updateAdapter(attendanceList)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load attendance history", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAdapter(attendanceList: MutableList<Attendance>) {
        val hardcodedAttendanceList = listOf(
            Attendance(date = "2024-10-01", time = "08:00:00", photoUrl = "https://dummyimage.com/800x600/ff0000/fff"),
            Attendance(date = "2024-10-01", time = "10:00:00", photoUrl = "https://dummyimage.com/700x500/ff6600/fff"),
            Attendance(date = "2024-10-05", time = "14:00:00", photoUrl = "https://dummyimage.com/800x600/ff0000/fff"),
            Attendance(date = "2024-10-05", time = "18:05:34", photoUrl = "https://dummyimage.com/450x350/00cccc/000"),
            Attendance(date = "2024-10-10", time = "12:05:21", photoUrl = "https://dummyimage.com/750x550/996633/fff")
        )
        attendanceList.addAll(hardcodedAttendanceList)

        attendanceList.sortWith(compareByDescending<Attendance> { it.date }.thenByDescending { it.time })

        historyAdapter.updateData(attendanceList)
    }
}
package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: AttendanceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        historyAdapter = AttendanceAdapter(emptyList())
        recyclerView.adapter = historyAdapter

        loadAttendanceHistory()
    }

    private fun loadAttendanceHistory() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        firestore.collection("attendances")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val attendanceList = result.map { doc -> doc.toObject(Attendance::class.java) }
                historyAdapter.updateData(attendanceList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load attendance history", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.if570_lab_uts_mariorichielim_00000067355

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceAdapter(private var attendanceList: List<Attendance>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateView: TextView = view.findViewById(R.id.attendance_date)
        val statusView: TextView = view.findViewById(R.id.attendance_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.attendance_item, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val attendance = attendanceList[position]
        holder.dateView.text = attendance.date
        holder.statusView.text = if (attendance.clockOutTime != null) "Completed" else "Pending"
    }

    override fun getItemCount(): Int = attendanceList.size

    fun updateData(newAttendanceList: List<Attendance>) {
        attendanceList = newAttendanceList
        notifyDataSetChanged()
    }
}

package com.example.if570_lab_uts_mariorichielim_00000067355

import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceAdapter(private var attendanceList: List<Attendance>) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val attendance = attendanceList[position]
        holder.dateTextView.text = attendance.date
        holder.timeTextView.text = attendance.time
        Glide.with(holder.itemView.context)
            .load(attendance.photoUrl)
            .into(holder.photoImageView)
    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }

    fun updateData(newAttendanceList: List<Attendance>) {
        attendanceList = newAttendanceList
        notifyDataSetChanged()
    }
}
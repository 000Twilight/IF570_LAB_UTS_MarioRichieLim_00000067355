package com.example.if570_lab_uts_mariorichielim_00000067355

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var storage: FirebaseFirestore
    private lateinit var storageRef: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var photoUri: Uri
    private lateinit var dateTextView: TextView
    private lateinit var clockInTextView: TextView
    private lateinit var clockOutTextView: TextView
    private lateinit var currentDate: String
    private lateinit var currentTime: String
    private var capturedImage: Bitmap? = null
    private var isClockOut = false
    private var clockInTime: String? = null
    private var clockOutTime: String? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()
        imageView = view.findViewById(R.id.attendance_image)
        dateTextView = view.findViewById(R.id.date_text)
        clockInTextView = view.findViewById(R.id.clock_in_text)
        clockOutTextView = view.findViewById(R.id.clock_out_text)
        val takeAttendanceButton = view.findViewById<Button>(R.id.take_attendance_button)
        uploadButton = view.findViewById(R.id.upload_image_button)

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        currentDate = dateFormat.format(Date())
        dateTextView.text = currentDate

        loadAttendanceStatus()

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take attendance", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageView.setImageURI(photoUri)
                capturedImage = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoUri)
                uploadButton.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Camera action failed", Toast.LENGTH_SHORT).show()
            }
        }

        takeAttendanceButton.setOnClickListener { checkAttendanceStatus() }
        uploadButton.setOnClickListener { showConfirmationDialog() }
    }

    override fun onResume() {
        super.onResume()
        handler.post(timeUpdateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeUpdateRunnable)
    }

    private fun updateTime() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        currentDate = dateFormat.format(Date())
        currentTime = timeFormat.format(Date())
        dateTextView.text = currentDate
    }

    private fun checkAttendanceStatus() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val userName = user.displayName ?: "Unknown User"
        storage.collection("attendance")
            .whereEqualTo("name", userName)
            .whereEqualTo("date", currentDate)
            .get()
            .addOnSuccessListener { documents ->
                when (documents.size()) {
                    0 -> {
                        isClockOut = false
                        checkAndRequestCameraPermission()
                    }
                    1 -> {
                        isClockOut = true
                        checkAndRequestCameraPermission()
                    }
                    2 -> {
                        Toast.makeText(requireContext(), "You have already completed attendance for today.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error checking attendance: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAndRequestCameraPermission() {
        when {
            requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera access is required to take attendance photos", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            photoUri = createImageFile()
            takePictureLauncher.launch(photoUri)
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): Uri {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "unknown_user"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val clockStatus = if (isClockOut) "clock_out" else "clock_in"
        val fileName = "${email}_${timeStamp}_${clockStatus}.jpg"

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create MediaStore entry")
        } else {
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(fileName, ".jpg", storageDir)
            FileProvider.getUriForFile(requireContext(), "com.example.if570_lab_uts_mariorichielim_00000067355.fileprovider", file)
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.confirm_attendance_title))
        builder.setMessage(getString(R.string.confirm_attendance))
        builder.setPositiveButton("Yes") { _, _ ->
            uploadAbsensi()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun uploadAbsensi() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val email = user.email?.replace(".", "_") ?: "unknown_user"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val clockStatus = if (isClockOut) "clock_out" else "clock_in"
        val fileName = "${email}_${timeStamp}_${clockStatus}.jpg"
        val absensiRef = storage.collection("absensi")
            .document(email)
            .collection("harian")
            .document(currentDate)

        absensiRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                Toast.makeText(requireContext(), "Anda sudah melakukan absensi hari ini", Toast.LENGTH_SHORT).show()
            } else {
                val storageRef = storageRef.reference.child("absensi/${email}/${fileName}")
                val baos = ByteArrayOutputStream()
                capturedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = storageRef.putBytes(data)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val absensiData = mapOf(
                            "email" to email,
                            "photoUrl" to uri.toString(),
                            "date" to currentDate,
                            "time" to currentTime,
                            "clockStatus" to clockStatus
                        )
                        absensiRef.set(absensiData).addOnSuccessListener {
                            Toast.makeText(requireContext(), "Absensi berhasil disimpan", Toast.LENGTH_SHORT).show()

                            if (clockStatus == "clock_in") {
                                clockInTime = currentDate
                                clockInTextView.text = getString(R.string.clock_in, currentDate)
                            } else {
                                clockOutTime = currentDate
                                clockOutTextView.text = getString(R.string.clock_out, currentDate)
                            }

                            uploadButton.visibility = View.GONE
                            isClockOut = !isClockOut
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadAttendanceStatus() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        clockInTime = sharedPref.getString("clock_in_time", null)
        clockOutTime = sharedPref.getString("clock_out_time", null)

        clockInTextView.text = if (clockInTime != null) {
            getString(R.string.clock_in, clockInTime)
        } else {
            getString(R.string.no_clock_in)
        }

        clockOutTextView.text = if (clockOutTime != null) {
            getString(R.string.clock_out, clockOutTime)
        } else {
            getString(R.string.no_clock_out)
        }
    }
}
package com.example.if570_lab_uts_mariorichielim_00000067355

import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
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
//
//class HomeFragment : Fragment() {
//    private lateinit var storage: FirebaseFirestore
//    private lateinit var imageView: ImageView
//    private lateinit var uploadButton: Button
//    private lateinit var photoUri: Uri
//    private lateinit var dateTextView: TextView
//    private var isClockOut = false // Track if user is clocking out
//
//    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
//    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
//
//    // Handler for updating time
//    private val handler = Handler(Looper.getMainLooper())
//    private val timeUpdateRunnable = object : Runnable {
//        override fun run() {
//            updateTime()
//            handler.postDelayed(this, 1000) // Update every second
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_home, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        storage = FirebaseFirestore.getInstance()
//        imageView = view.findViewById(R.id.attendance_image)
//        dateTextView = view.findViewById(R.id.date_time_text)
//        val takeAttendanceButton = view.findViewById<Button>(R.id.take_attendance_button)
//        uploadButton = view.findViewById(R.id.upload_image_button)
//
//        // Initialize permission launcher
//        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                openCamera()
//            } else {
//                Toast.makeText(requireContext(), "Camera permission is required to take attendance", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Initialize picture launcher
//        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//            if (success) {
//                imageView.setImageURI(photoUri)
//                uploadButton.visibility = View.VISIBLE
//                showConfirmationDialog() // Show confirmation dialog after taking a picture
//            } else {
//                Toast.makeText(requireContext(), "Camera action failed", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Button actions
//        takeAttendanceButton.setOnClickListener { checkAttendanceStatus() }
//        uploadButton.setOnClickListener { uploadImageToFirebase() }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        handler.post(timeUpdateRunnable)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        handler.removeCallbacks(timeUpdateRunnable)
//    }
//
//    private fun updateTime() {
//        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", Locale.getDefault())
//        dateTextView.text = sdf.format(Date())
//    }
//
//    private fun checkAndRequestCameraPermission() {
//        when {
//            requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
//                openCamera() // Permission already granted
//            }
//            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
//                Toast.makeText(requireContext(), "Camera access is required to take attendance photos", Toast.LENGTH_LONG).show()
//                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//            }
//            else -> {
//                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//            }
//        }
//    }
//
//    // Check attendance status to ensure user can only attend twice per day (Clock In and Clock Out)
//    private fun checkAttendanceStatus() {
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//        val user = FirebaseAuth.getInstance().currentUser
//
//        if (user == null) {
//            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val userName = user.displayName ?: "Unknown User"
//        storage.collection("attendance")
//            .whereEqualTo("name", userName)
//            .whereEqualTo("date", currentDate)
//            .get()
//            .addOnSuccessListener { documents ->
//                when (documents.size()) {
//                    0 -> {
//                        // No attendance record for today, user can clock in
//                        isClockOut = false
//                        checkAndRequestCameraPermission()
//                    }
//                    1 -> {
//                        // User has already clocked in, allow clock out
//                        isClockOut = true
//                        checkAndRequestCameraPermission()
//                    }
//                    2 -> {
//                        // User has already clocked in and out, attendance complete
//                        Toast.makeText(requireContext(), "You have already completed attendance for today.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Error checking attendance: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun openCamera() {
//        try {
//            photoUri = createImageFile() // Use MediaStore or FileProvider based on Android version
//            takePictureLauncher.launch(photoUri)
//        } catch (e: IOException) {
//            Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun createImageFile(): Uri {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val fileName = "JPEG_${timeStamp}_.jpg"
//
//        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            val contentValues = ContentValues().apply {
//                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//            }
//            requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//                ?: throw IOException("Failed to create MediaStore entry")
//        } else {
//            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            val file = File.createTempFile(fileName, ".jpg", storageDir)
//            FileProvider.getUriForFile(requireContext(), "com.example.if570_lab_uts_mariorichielim_00000067355.fileprovider", file)
//        }
//    }
//
//    private fun showConfirmationDialog() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Confirm Attendance")
//        builder.setMessage("Do you want to confirm this attendance?")
//        builder.setPositiveButton("Yes") { _, _ ->
//            uploadImageToFirebase()
//        }
//        builder.setNegativeButton("No") { dialog, _ ->
//            dialog.dismiss() // Dismiss the dialog and don't upload
//        }
//        builder.show()
//    }
//
//    private fun uploadImageToFirebase() {
//        val storageRef = FirebaseStorage.getInstance().reference.child("attendance_photos/${UUID.randomUUID()}.jpg")
//        storageRef.putFile(photoUri)
//            .addOnSuccessListener {
//                storageRef.downloadUrl.addOnSuccessListener { uri ->
//                    val user = FirebaseAuth.getInstance().currentUser
//                    val userName = user?.displayName ?: "Unknown User"
//                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//                    val attendanceData = hashMapOf(
//                        "imageUrl" to uri.toString(),
//                        "name" to userName,
//                        "date" to currentDate,
//                        "status" to if (isClockOut) "Clock Out" else "Clock In"
//                    )
//
//                    storage.collection("attendance").add(attendanceData).addOnSuccessListener {
//                        Toast.makeText(requireContext(), "Attendance saved", Toast.LENGTH_SHORT).show()
//                    }.addOnFailureListener { e ->
//                        Toast.makeText(requireContext(), "Error saving attendance: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                Log.e("FirebaseUpload", "Upload failed", e)
//            }
//    }
//}

class HomeFragment : Fragment() {
    private lateinit var storage: FirebaseFirestore
    private lateinit var storageRef: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var submitButton: Button
    private lateinit var photoUri: Uri
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var currentDate: String
    private lateinit var currentTime: String
    private var capturedImage: Bitmap? = null
    private var isClockOut = false

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Handler for updating time
    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Update every second
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
        timeTextView = view.findViewById(R.id.time_text)
        val takeAttendanceButton = view.findViewById<Button>(R.id.take_attendance_button)
        uploadButton = view.findViewById(R.id.upload_image_button)
        submitButton = view.findViewById(R.id.submit_button)

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take attendance", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize picture launcher
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageView.setImageURI(photoUri)
                uploadButton.visibility = View.VISIBLE
                submitButton.isEnabled = true
                showConfirmationDialog()
            } else {
                Toast.makeText(requireContext(), "Camera action failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Button actions
        takeAttendanceButton.setOnClickListener { checkAttendanceStatus() }
        uploadButton.setOnClickListener { uploadAbsensi() }
        submitButton.setOnClickListener {
            capturedImage?.let {
                showConfirmationDialog()
            } ?: Toast.makeText(requireContext(), "Please take a photo first", Toast.LENGTH_SHORT).show()
        }
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
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        currentDate = dateFormat.format(Date())
        currentTime = timeFormat.format(Date())
        dateTextView.text = currentDate
        timeTextView.text = currentTime
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

    private fun openCamera() {
        try {
            photoUri = createImageFile()
            takePictureLauncher.launch(photoUri)
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_${timeStamp}_.jpg"

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
        builder.setTitle("Confirm Attendance")
        builder.setMessage("Do you want to confirm this attendance?")
        builder.setPositiveButton("Yes") { _, _ ->
            uploadAbsensi()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun uploadAbsensi() {
        val userId = "user123"  // Replace with actual user ID
        val absensiRef = storage.collection("absensi")
            .document(userId)
            .collection("harian")
            .document(currentDate)

        absensiRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                Toast.makeText(requireContext(), "Anda sudah melakukan absensi hari ini", Toast.LENGTH_SHORT).show()
            } else {
                // Upload photo to Firebase Storage
                val storageRef = storageRef.reference.child("absensi/${userId}/${currentDate}.jpg")
                val baos = ByteArrayOutputStream()
                capturedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = storageRef.putBytes(data)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val absensiData = mapOf(
                            "userId" to userId,
                            "photoUrl" to uri.toString(),
                            "date" to currentDate,
                            "time" to currentTime
                        )
                        absensiRef.set(absensiData).addOnSuccessListener {
                            Toast.makeText(requireContext(), "Absensi berhasil disimpan", Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

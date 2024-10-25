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

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var storage: FirebaseFirestore
    private lateinit var storageRef: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var photoUri: Uri
    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var clockInTextView: TextView
    private lateinit var clockOutTextView: TextView
    private lateinit var currentDate: String
    private lateinit var currentTime: String
    private var capturedImage: Bitmap? = null
    private var isClockIn = false
    private var isClockOut = false
    private var clockInTime: String? = null
    private var clockOutTime: String? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            loadAttendanceStatus()
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
        timeTextView = view.findViewById(R.id.time_text)
        clockInTextView = view.findViewById(R.id.clock_in_text)
        clockOutTextView = view.findViewById(R.id.clock_out_text)
        val takeAttendanceButton = view.findViewById<Button>(R.id.take_attendance_button)
        uploadButton = view.findViewById(R.id.upload_image_button)

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        currentDate = dateFormat.format(Date())
        currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate
        timeTextView.text = currentTime

        loadAttendanceStatus()

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("HomeFragment", "Camera permission granted")
                openCamera()
            } else {
                Log.d("HomeFragment", "Camera permission denied")
                Toast.makeText(requireContext(), "Camera permission is required to take attendance", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Log.d("HomeFragment", "Picture taken successfully")
                imageView.setImageURI(photoUri)
                capturedImage = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoUri)
                uploadButton.visibility = View.VISIBLE
            } else {
                Log.d("HomeFragment", "Failed to take picture")
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
        timeTextView.text = currentTime
        Log.d("HomeFragment", "Time updated: $currentTime")
    }

    private fun checkAndRequestCameraPermission() {
        when {
            requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("HomeFragment", "Camera permission already granted")
                openCamera()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                Log.d("HomeFragment", "Showing camera permission rationale")
                Toast.makeText(requireContext(), "Camera access is required to take attendance photos", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            else -> {
                Log.d("HomeFragment", "Requesting camera permission")
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            photoUri = createImageFile()
            Log.d("HomeFragment", "Opening camera with URI: $photoUri")
            takePictureLauncher.launch(photoUri)
        } catch (e: IOException) {
            Log.e("HomeFragment", "Failed to create image file", e)
            Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): Uri {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "unknown_user"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val clockStatus = if (isClockOut) {
            "clock_out"
        } else {
            "clock_in"
        }

        val fileName = "${email}_${timeStamp}_${clockStatus}.jpg"
        Log.d("HomeFragment", "Creating image file: $fileName")

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
            Log.d("HomeFragment", "User confirmed attendance")
            uploadAbsensi()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            Log.d("HomeFragment", "User canceled attendance confirmation")
            dialog.dismiss()
        }
        builder.show()
    }

    private fun checkAttendanceStatus() {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d("HomeFragment", "No authenticated user found")
            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val userName = user.email?.replace(".", "_") ?: "Unknown User"
        val storagePath = "absensi/${userName}/"

        Log.d("HomeFragment", "Checking attendance status for user: $userName on date: $currentDate")

        storageRef.reference.child(storagePath).listAll()
            .addOnSuccessListener { result ->
                var clockInFound = false
                var clockOutFound = false

                for (fileRef in result.items) {
                    val fileName = fileRef.name
                    Log.d("HomeFragment", "Found file: $fileName")
                    if (fileName.contains(currentDate) && fileName.contains("clock_in")) {
                        clockInFound = true
                        Log.d("HomeFragment", "Clock-in file found: $fileName")
                    }
                    if (fileName.contains(currentDate) && fileName.contains("clock_out")) {
                        clockOutFound = true
                        Log.d("HomeFragment", "Clock-out file found: $fileName")
                    }
                }

                when {
                    clockInFound && !clockOutFound -> {
                        isClockIn = true
                        isClockOut = true // Set to true to indicate the next action is clock-out
                        Log.d("HomeFragment", "User has clocked in but not clocked out")
                        checkAndRequestCameraPermission() // proceed to clock out
                    }
                    clockInFound && clockOutFound -> {
                        isClockIn = true
                        isClockOut = true
                        Log.d("HomeFragment", "User has completed attendance for today")
                        Toast.makeText(requireContext(), "Attendance completed for today.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        isClockIn = false
                        isClockOut = false
                        Log.d("HomeFragment", "User has not clocked in yet")
                        checkAndRequestCameraPermission() // proceed to clock in
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error checking attendance", e)
                Toast.makeText(requireContext(), "Error checking attendance: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // HomeFragment.kt
    private fun uploadAbsensi() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d("HomeFragment", "No authenticated user found")
            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val email = user.email?.replace(".", "_") ?: "unknown_user"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val clockStatus = if (isClockOut) "clock_out" else "clock_in"
        val fileName = "${email}_${timeStamp}_${clockStatus}.jpg"
        val absensiRef = storage.collection("attendance")
            .document(email)
            .collection(currentDate)
            .document("status")

        Log.d("HomeFragment", "Uploading attendance: $fileName")
        absensiRef.get().addOnSuccessListener { documentSnapshot ->
            val storageRef = storageRef.reference.child("absensi/${email}/${fileName}")
            val baos = ByteArrayOutputStream()
            capturedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = storageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val absensiData = mutableMapOf(
                        "email" to email,
                        "photoUrl" to uri.toString(),
                        "date" to currentDate,
                        "time" to currentTime,
                        "clockInTime" to if (!isClockOut) currentTime else clockInTime,
                        "clockOutTime" to if (isClockOut) currentTime else null
                    )

                    absensiRef.set(absensiData).addOnSuccessListener {
                        Log.d("HomeFragment", "Attendance successfully saved in Firestore")
                        Toast.makeText(requireContext(), "Absensi berhasil disimpan", Toast.LENGTH_SHORT).show()

                        if (!isClockOut) {
                            clockInTime = currentTime
                            clockInTextView.text = getString(R.string.clock_in, currentTime)
                            isClockIn = true
                        } else {
                            clockOutTime = currentTime
                            clockOutTextView.text = getString(R.string.clock_out, currentTime)
                            isClockOut = true
                        }

                        uploadButton.visibility = View.GONE

                        // Refresh attendance history
                        val historyFragment = parentFragmentManager.findFragmentById(R.id.historyFragment) as? HistoryFragment
                        historyFragment?.refreshAttendanceHistory()
                    }
                }
            }.addOnFailureListener {
                Log.e("HomeFragment", "Failed to upload photo", it)
                Toast.makeText(requireContext(), "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAttendanceStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d("HomeFragment", "No authenticated user found")
            Toast.makeText(requireContext(), "No authenticated user found", Toast.LENGTH_SHORT).show()
            return
        }

        val userName = user.email?.replace(".", "_") ?: "unknown_user"
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val storagePath = "absensi/${userName}/"

        Log.d("HomeFragment", "Checking attendance status for user: $userName on date: $currentDate")

        storageRef.reference.child(storagePath).listAll()
            .addOnSuccessListener { result ->
                var clockInTime: String? = null
                var clockOutTime: String? = null

                for (fileRef in result.items) {
                    val fileName = fileRef.name
                    Log.d("HomeFragment", "Found file: $fileName")
                    if (fileName.contains(currentDate) && fileName.contains("clock_in")) {
                        clockInTime = fileName.substringBefore("_clock_in").takeLast(6).substring(0, 2) + ":" + fileName.substringBefore("_clock_in").takeLast(6).substring(2, 4)
                        Log.d("HomeFragment", "Clock-in file found: $fileName")
                    }
                    if (fileName.contains(currentDate) && fileName.contains("clock_out")) {
                        clockOutTime = fileName.substringBefore("_clock_out").takeLast(6).substring(0, 2) + ":" + fileName.substringBefore("_clock_out").takeLast(6).substring(2, 4)
                        Log.d("HomeFragment", "Clock-out file found: $fileName")
                    }
                }

                clockInTextView.text = getString(R.string.clock_in, clockInTime ?: "-")
                clockOutTextView.text = getString(R.string.clock_out, clockOutTime ?: "-")
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error loading attendance status", e)
                Toast.makeText(requireContext(), "Error loading attendance status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToSharedPref(key: String, value: String) {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
        Log.d("HomeFragment", "Saved to shared preferences: $key=$value")
    }
}
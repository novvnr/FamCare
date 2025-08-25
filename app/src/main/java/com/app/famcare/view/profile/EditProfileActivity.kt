package com.app.famcare.view.profile

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.famcare.R
import com.app.famcare.databinding.ActivityEditProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityEditProfileBinding
    private val storageRef = Firebase.storage.reference
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding.profileImageView.setImageResource(R.drawable.user)

        binding.editProfileImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        binding.saveChanges.setOnClickListener {
            if (validateInput()) {
                binding.saveChangesProgressBar.visibility = View.VISIBLE
                uploadImageToFirebaseStorage()
            } else {
                Toast.makeText(this, "Please fill all fields and select gender", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.icDate.setOnClickListener {
            showDatePickerDialog()
        }

        loadUserDataFromFirestore()
        addTextWatchers()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                binding.selectedDateTextView.text = dateFormat.format(selectedDate.time)
            }, year, month, day
        )

        datePickerDialog.datePicker.maxDate = calendar.timeInMillis - 3L * 365 * 24 * 60 * 60 * 1000
        datePickerDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            binding.profileImageView.setImageURI(imageUri)
        }
    }

    private fun loadUserDataFromFirestore() {
        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            firestore.collection("User").document(currentUserUid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("fullName")
                        val phone = document.getString("phone")
                        val address = document.getString("address") ?: ""
                        val gender = document.getString("gender") ?: ""
                        val birthDate = document.getString("birthDate") ?: ""

                        binding.fullNameEditText.setText(name)
                        binding.phoneEditText.setText(phone)
                        binding.addressEditText.setText(address)
                        if (birthDate.isNullOrEmpty()) {
                            binding.selectedDateTextView.text = "dd/mm/yyyy"
                        } else {
                            binding.selectedDateTextView.text = birthDate
                        }

                        when (gender) {
                            "Male" -> binding.genderRadioGroup.check(R.id.radioMale)
                            "Female" -> binding.genderRadioGroup.check(R.id.radioFemale)
                            else -> binding.genderRadioGroup.clearCheck()
                        }

                        val imageUrl = document.getString("profileImageUrl")
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            Glide.with(this).load(imageUrl).into(binding.profileImageView)
                        }

                    }

                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        val currentUserUid = firebaseAuth.currentUser?.uid
        if (currentUserUid != null && imageUri != null) {
            val imageName = "profile_${currentUserUid}.jpg"
            val imageRef = storageRef.child("User/$imageName")

            val bitmapDrawable = binding.profileImageView.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            val uploadTask = imageRef.putBytes(imageData)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveUserDataToFirestore(imageUrl)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveUserDataToFirestore("")
        }
    }

    private fun saveUserDataToFirestore(imageUrl: String) {
        val currentUserUid = firebaseAuth.currentUser?.uid
        if (currentUserUid != null) {
            val name = binding.fullNameEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()
            val address = binding.addressEditText.text.toString()
            val birthDate = binding.selectedDateTextView.text.toString()
            val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                else -> ""
            }

            val userData = mutableMapOf<String, Any>(
                "fullName" to name,
                "phone" to phone,
                "address" to address,
                "gender" to gender,
                "birthDate" to birthDate
            )

            if (imageUrl.isNotEmpty()) {
                userData["profileImageUrl"] = imageUrl
            }

            firestore.collection("User").document(currentUserUid).update(userData)
                .addOnSuccessListener {
                    binding.saveChangesProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                    binding.saveChangesProgressBar.visibility = View.GONE
                }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.fullNameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()
        val gender = binding.genderRadioGroup.checkedRadioButtonId
        val birthDate = binding.selectedDateTextView.text.toString().trim()

        return name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty() && gender != -1 && birthDate.isNotEmpty()
    }

    private fun addTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.saveChanges.isEnabled = validateInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.fullNameEditText.addTextChangedListener(watcher)
        binding.phoneEditText.addTextChangedListener(watcher)
        binding.addressEditText.addTextChangedListener(watcher)
        binding.genderRadioGroup.setOnCheckedChangeListener { _, _ ->
            binding.saveChanges.isEnabled = validateInput()
        }

        binding.saveChanges.isEnabled = validateInput()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
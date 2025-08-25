package com.app.famcare.view.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.app.famcare.databinding.ActivityRegisterBinding
import com.app.famcare.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            val fullName = binding.fullnameEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && fullName.isNotEmpty() && phone.isNotEmpty()) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { createUserTask ->
                            if (createUserTask.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                val uid = user?.uid

                                uid?.let { uid ->
                                    saveUserDataToFirestore(uid, fullName, email, phone)
                                }
                                sendEmailVerification()
                                firebaseAuth.signOut()
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.putExtra("isNewUser", true)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this, createUserTask.exception.toString(), Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this, "Passwords do not match. Please try again.", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.bottom2TextViews.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserDataToFirestore(
        uid: String, fullName: String, email: String, phone: String
    ) {
        val userData = hashMapOf(
            "uid" to uid,
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "address" to "",
            "gender" to "",
            "birth" to ""
        )

        firestore.collection("User").document(uid).set(userData).addOnSuccessListener {}
            .addOnFailureListener { exception ->
                Log.e("RegisterActivity", "Error writing document", exception)
            }
    }

    private fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
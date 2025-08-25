package com.app.famcare.view.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.famcare.R
import com.app.famcare.databinding.ActivityLoginBinding
import com.app.famcare.view.customview.EmailEditText
import com.app.famcare.view.main.MainActivity
import com.app.famcare.view.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = firebaseAuth.currentUser
                            if (currentUser != null && currentUser.isEmailVerified) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this, "Please verify your email first", Toast.LENGTH_SHORT
                                ).show()
                                firebaseAuth.signOut()
                            }
                        } else {
                            handleLoginError(task.exception)
                        }
                    }
            } else {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.bottom2TextViews.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordPopup()
        }
    }

    private fun handleLoginError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show()
            }

            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(this, "Login failed: ${exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showForgotPasswordPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_reset_password, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val emailEditText = dialogView.findViewById<EmailEditText>(R.id.emailEditText)
        val resetPasswordButton = dialogView.findViewById<Button>(R.id.resetPasswordButton)
        val bottom2TextViews = dialogView.findViewById<TextView>(R.id.bottom2TextViews)

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            val firestore = FirebaseFirestore.getInstance()
            val usersCollection = firestore.collection("User")
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val alertDialog = AlertDialog.Builder(this).setTitle("Email Sent")
                                    .setMessage("Password reset email sent to $email")
                                    .setPositiveButton("OK") { _, _ ->
                                        dialog.dismiss()
                                    }.setCancelable(false).create()
                                alertDialog.show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to send reset email: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        val alertDialog = AlertDialog.Builder(this).setTitle("Email Not Registered")
                            .setMessage("The entered email is not registered. Please check your email or register a new account.")
                            .setPositiveButton("OK") { _, _ ->
                                emailEditText.text?.clear()
                                dialog.dismiss()
                            }.setCancelable(false).create()
                        alertDialog.show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this, "Failed to check email validity: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
        }

        bottom2TextViews.setOnClickListener {
            dialog.dismiss()
        }
    }

}
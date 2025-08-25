package com.app.famcare.view.detailpost

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.famcare.R
import com.app.famcare.databinding.ActivityDetailPostBinding
import com.app.famcare.model.Nanny
import com.app.famcare.view.booking.BookDailyActivity
import com.app.famcare.view.booking.BookMonthlyActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class DetailPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPostBinding
    private lateinit var nannyId: String
    private lateinit var userId: String
    private var nanny: Nanny? = null
    private var isBookmarked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nannyId = intent.getStringExtra("nannyId")
            ?: throw IllegalArgumentException("Nanny ID must be provided")

        userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be logged in to view this page")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        loadNannyDataFromFirestore()

        binding.buttonBookNanny.setOnClickListener {
            navigateToBookingActivity()
        }

        binding.ivBookmark.setOnClickListener {
            toggleBookmark()
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadNannyDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Nanny").document(nannyId)

        docRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                nanny = document.toObject(Nanny::class.java)

                nanny?.let { nannyData ->
                    isBookmarked = document.get("bookmarkedBy.$userId") != null
                    val buttonBookNanny = binding.buttonBookNanny
                    buttonBookNanny.text = if (nannyData.gender == "male") {
                        "Book Manny"
                    } else {
                        "Book Nanny"
                    }

                    with(binding) {
                        Glide.with(this@DetailPostActivity).load(nannyData.pict)
                            .placeholder(R.drawable.placeholder_image).into(imageViewNannyDP)

                        imageViewGenderDP.setImageResource(
                            if (nannyData.gender == "male") R.drawable.ic_male else R.drawable.ic_female
                        )

                        textViewNameDP.text = nannyData.name
                        textViewAgeDP.text = "${nannyData.age} years old"
                        textViewRateDP.text = nannyData.rate
                        textViewTypeDP.text = nannyData.type
                        textViewPerHour.text = if (nannyData.type == "daily") "per hour" else "per month"
                        textViewExperienceDP.text = "${nannyData.experience} experiences"
                        textViewLocationDP.text = nannyData.location
                        textViewSalaryDP.text = nannyData.pricing.formatToCurrency()
                        skillListLayout.removeAllViews()
                        nannyData.skills.forEach { skill ->
                            val textView = TextView(this@DetailPostActivity).apply {
                                text = "• $skill"
                                setTextColor(
                                    ContextCompat.getColor(
                                        this@DetailPostActivity, android.R.color.black
                                    )
                                )
                                textSize = 17f
                                typeface = ResourcesCompat.getFont(
                                    this@DetailPostActivity, R.font.montserrat
                                )
                            }
                            skillListLayout.addView(textView)
                        }

                        updateBookmarkIcon()
                    }
                } ?: run {
                    Log.e("DetailPostActivity", "Nanny object is null")
                    Toast.makeText(this, "Failed to load nanny details", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("DetailPostActivity", "Document does not exist or is null")
                Toast.makeText(this, "Nanny data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("DetailPostActivity", "Error getting document", exception)
            Toast.makeText(this, "Failed to load nanny data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.formatToCurrency(): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(this)
    }

    private fun toggleBookmark() {
        val db = FirebaseFirestore.getInstance()
        val bookmarkRef = db.collection("Bookmarks").document(nannyId)

        if (isBookmarked) {
            bookmarkRef.delete().addOnSuccessListener {
                isBookmarked = false
                updateBookmarkIcon()
                Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.e("DetailPostActivity", "Error removing bookmark", e)
                Toast.makeText(this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
            }
        } else {
            val bookmarkData = hashMapOf(
                "userId" to userId,
                "nannyId" to nannyId,
                "name" to nanny?.name,
                "type" to nanny?.type,
                "rate" to nanny?.rate,
            )

            bookmarkRef.set(bookmarkData).addOnSuccessListener {
                isBookmarked = true
                updateBookmarkIcon()
                Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.e("DetailPostActivity", "Error adding bookmark", e)
                Toast.makeText(this, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBookmarkIcon() {
        val bookmarkIcon = binding.ivBookmark
        if (isBookmarked) {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_24)
        } else {
            bookmarkIcon.setImageResource(R.drawable.outline_bookmark_24)
        }
    }

    private fun navigateToBookingActivity() {
        val intent = when (nanny?.type) {
            "daily" -> Intent(this, BookDailyActivity::class.java)
            "monthly" -> Intent(this, BookMonthlyActivity::class.java)
            else -> {
                Toast.makeText(this, "Invalid nanny type", Toast.LENGTH_SHORT).show()
                return
            }
        }

        intent.putExtra("nannyId", nannyId)
        intent.putExtra("activityName", this::class.java.simpleName)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

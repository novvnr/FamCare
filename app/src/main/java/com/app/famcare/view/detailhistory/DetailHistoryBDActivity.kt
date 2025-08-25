package com.app.famcare.view.detailhistory

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.app.famcare.R
import com.app.famcare.model.BookingDailyHistory
import com.app.famcare.view.history.HistoryActivity
import com.app.famcare.view.historyimport.HistoryBDFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class DetailHistoryBDActivity : AppCompatActivity() {

    private lateinit var bookingID: String
    private lateinit var nannyID: String
    private lateinit var imageProfile: ImageView
    private lateinit var textName: TextView
    private lateinit var textEmail: TextView
    private lateinit var viewBookDate: TextView
    private lateinit var viewBookingDuration: TextView
    private lateinit var viewStartTime: TextView
    private lateinit var viewEndTime: TextView
    private lateinit var textSalary: TextView
    private lateinit var textViewID: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history_b_d)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        imageProfile = findViewById(R.id.image_profile)
        textName = findViewById(R.id.text_name)
        textEmail = findViewById(R.id.text_email)
        viewBookDate = findViewById(R.id.viewBookDate)
        viewBookingDuration = findViewById(R.id.viewBookingDuration)
        viewStartTime = findViewById(R.id.viewStartTime)
        viewEndTime = findViewById(R.id.viewEndTime)
        textSalary = findViewById(R.id.text_salary)
        textViewID = findViewById(R.id.textViewID)

        bookingID = intent.getStringExtra("bookingID") ?: ""
        if (bookingID.isNotEmpty()) {
            loadBookingData(bookingID)
        } else {
            Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show()
        }

        val buttonCancelBook = findViewById<Button>(R.id.buttonCancelBook)
        buttonCancelBook.setOnClickListener {
            showConfirmationDialog()
        }
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

    private fun loadBookingData(bookingID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("BookingDaily").document(bookingID).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val booking = document.toObject(BookingDailyHistory::class.java)
                booking?.let {
                    textViewID.text = bookingID
                    textName.text = it.nannyName
                    textEmail.text = it.type.name.toLowerCase(Locale.ROOT)
                    viewBookDate.text = it.bookDate
                    viewBookingDuration.text = "${it.bookDuration} Hours"
                    viewStartTime.text = it.bookHours
                    viewEndTime.text = it.endHours

                    val localeID = Locale("id", "ID")
                    val formattedCurrency = NumberFormat.getCurrencyInstance(localeID).format(it.totalPricing.toLong())
                    textSalary.text = formattedCurrency

                    nannyID = it.nannyID

                    loadNannyData(it.nannyID)
                }
            } else {
                Toast.makeText(this, "No booking data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting booking document", exception)
            Toast.makeText(this, "Failed to load booking data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNannyData(nannyID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Nanny").document(nannyID).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nannyName = document.getString("name") ?: ""
                val nannyPict = document.getString("pict") ?: ""
                textName.text = nannyName

                Glide.with(this).load(nannyPict).placeholder(R.drawable.nanny_placeholder)
                    .error(R.drawable.placeholder_image).into(imageProfile)
            } else {
                Toast.makeText(this, "Nanny data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting nanny document", exception)
            Toast.makeText(this, "Failed to load nanny data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancel Booking")
        builder.setMessage("Are you sure you want to cancel this booking?")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            cancelBooking()
        }
        builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }
        builder.show()
    }

    private fun cancelBooking() {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("User").document(currentUserID)
            .update("bookIDs", FieldValue.arrayRemove(bookingID)).addOnSuccessListener {
                db.collection("BookingDaily").document(bookingID).delete().addOnSuccessListener {
                    showSuccessDialog()
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting booking document", e)
                    Toast.makeText(this, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error updating user document", e)
                Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Booking Cancelled")
        builder.setMessage("Your booking has been cancelled successfully.")
        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("selectedTab", 0)
            startActivity(intent)
            finish()
        }
        builder.show()
    }

    companion object {
        private const val TAG = "DetailHistoryBDActivity"
    }
}

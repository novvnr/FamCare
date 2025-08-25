package com.app.famcare.view.historyimport

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.app.famcare.adapter.BookingAdapter
import com.app.famcare.model.BookingDaily
import com.app.famcare.model.BookingType
import com.app.famcare.view.chat.ChatActivity
import com.app.famcare.view.detailhistory.DetailHistoryBDActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryBDFragment : Fragment(), BookingAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookingAdapter
    private var selectedBookingID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_history_b_d, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BookingAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        fetchDataFromFirestore()

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchDataFromFirestore() {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading data...")
        progressDialog.show()

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(currentUserID).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val bookIDs = document.get("bookIDs") as? List<String> ?: emptyList()
                if (bookIDs.isNotEmpty()) {
                    progressDialog.dismiss()
                    fetchBookings(bookIDs)
                } else {
                    Log.w(TAG, "No bookings found for user")
                    progressDialog.dismiss()
                }
            } else {
                Log.w(TAG, "User document does not exist")
                progressDialog.dismiss()
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting user document: ", exception)
            progressDialog.dismiss()
        }
    }

    private fun fetchBookings(bookIDs: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val bookingList = mutableListOf<BookingDaily>()

        bookIDs.forEach { bookID ->
            db.collection("BookingDaily").document(bookID).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nannyID = document.getString("nannyID") ?: ""
                    val bookDate = document.getString("bookDate") ?: ""
                    val bookHours = document.getString("bookHours") ?: ""
                    val endHours = document.getString("endHours") ?: ""
                    db.collection("Nanny").document(nannyID).get()
                        .addOnSuccessListener { nannyDoc ->
                            val nannyName = nannyDoc.getString("name") ?: ""
                            val bookingDaily = BookingDaily(
                                bookID, nannyName, bookDate, bookHours, endHours, BookingType.DAILY
                            )
                            bookingList.add(bookingDaily)

                            adapter.setData(bookingList.reversed())
                        }.addOnFailureListener { e ->
                            Log.w(TAG, "Error getting nanny document", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error getting booking document: ", e)
            }
        }
    }

    override fun onItemClick(bookingID: String) {
        selectedBookingID = bookingID
        val intent = Intent(requireContext(), DetailHistoryBDActivity::class.java)
        intent.putExtra("bookingID", selectedBookingID)
        startActivity(intent)
    }

    override fun onChatClick(bookingID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("BookingDaily").document(bookingID).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nannyID = document.getString("nannyID") ?: ""
                if (nannyID.isNotEmpty()) {
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra("nannyID", nannyID)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Nanny ID not found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to load booking", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "HistoryBDFragment"
    }
}
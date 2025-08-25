package com.app.famcare.view.bookmark

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.app.famcare.adapter.BookmarkAdapter
import com.app.famcare.databinding.ActivityBookmarkBinding
import com.app.famcare.model.Nanny
import com.app.famcare.view.detailpost.DetailPostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookmarkActivity : AppCompatActivity(), BookmarkAdapter.OnBookmarkItemClickListener {

    private lateinit var binding: ActivityBookmarkBinding
    private lateinit var adapter: BookmarkAdapter
    private val bookmarkedNannies = mutableListOf<Nanny>()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be logged in to view this page")

        adapter = BookmarkAdapter(
            this, bookmarkedNannies, this
        )
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        binding.recyclerView.adapter = adapter

        loadBookmarkedNannies()
    }

    private fun loadBookmarkedNannies() {
        val db = FirebaseFirestore.getInstance()
        val bookmarkRef = db.collection("Bookmarks").whereEqualTo("userId", userId)

        bookmarkRef.get().addOnSuccessListener { documents ->
            bookmarkedNannies.clear()

            for (document in documents) {
                val nannyId = document.getString("nannyId") ?: continue

                db.collection("Nanny").document(nannyId).get().addOnSuccessListener { nannyDoc ->
                    if (nannyDoc != null && nannyDoc.exists()) {
                        val nanny = nannyDoc.toObject(Nanny::class.java)
                        nanny?.let {
                            it.id = nannyId
                            it.isBookmarked = true
                            bookmarkedNannies.add(it)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                    Toast.makeText(
                        this@BookmarkActivity, "Failed to load nanny details", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            Toast.makeText(
                this@BookmarkActivity, "Failed to load bookmarked nannies", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onBookmarkItemClick(nanny: Nanny) {
        val intent = Intent(this, DetailPostActivity::class.java)
        intent.putExtra("nannyId", nanny.id)
        startActivity(intent)
    }

    override fun onBookmarkIconClick(nanny: Nanny) {
        val db = FirebaseFirestore.getInstance()
        val bookmarkRef = db.collection("Bookmarks").document("${userId}_${nanny.id}")

        if (nanny.isBookmarked) {
            bookmarkRef.delete().addOnSuccessListener {
                removeNannyFromList(nanny)
                adapter.notifyDataSetChanged()
                Toast.makeText(this@BookmarkActivity, "Bookmark removed", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(
                    this@BookmarkActivity, "Failed to remove bookmark", Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val bookmarkData = hashMapOf(
                "userId" to userId,
                "nannyId" to nanny.id,
                "name" to nanny.name,
                "type" to nanny.type,
                "rate" to nanny.rate,
            )

            bookmarkRef.set(bookmarkData).addOnSuccessListener {
                nanny.isBookmarked = true
                bookmarkedNannies.add(nanny)
                adapter.notifyDataSetChanged()
                Toast.makeText(this@BookmarkActivity, "Bookmark added", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(
                    this@BookmarkActivity, "Failed to add bookmark", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removeNannyFromList(nanny: Nanny) {
        val iterator = bookmarkedNannies.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.id == nanny.id) {
                iterator.remove()
                break
            }
        }
    }

}
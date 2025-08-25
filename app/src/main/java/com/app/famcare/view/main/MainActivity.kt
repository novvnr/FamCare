package com.app.famcare.view.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.famcare.R
import com.app.famcare.adapter.NannyAdapter
import com.app.famcare.repository.NannyRepository
import com.app.famcare.view.detailpost.DetailPostActivity
import com.app.famcare.view.facilities.FacilitiesActivity
import com.app.famcare.view.history.HistoryActivity
import com.app.famcare.view.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), FilterFragment.FilterListener {
    private lateinit var editTextSearch: EditText
    private lateinit var imageViewSearch: ImageView
    private lateinit var imageViewFilter: ImageView
    private lateinit var recyclerViewNanny: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyData: TextView
    private lateinit var ivEmptyData: ImageView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var tvName: TextView
    private lateinit var listOption: ImageView
    private lateinit var adapter: NannyAdapter
    private val nannyRepository = NannyRepository()
    private var currentQueryText: String = ""
    private var isGrid: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        tvName = findViewById(R.id.tv_name)
        val firestore = FirebaseFirestore.getInstance()

        if (currentUser != null) {
            loadUserName(currentUser.uid)
        } else {
            tvName.text = "Guest"
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.page_1
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.page_1 -> true
                R.id.page_2 -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                R.id.page_3 -> {
                    startActivity(Intent(this, FacilitiesActivity::class.java))
                    true
                }

                R.id.page_4 -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        setupRecyclerView(isGrid)
        fetchData()

        imageViewFilter.setOnClickListener {
            val filterFragment = FilterFragment()
            filterFragment.setFilterListener(this)
            filterFragment.show(supportFragmentManager, "FilterFragment")
        }

        imageViewSearch.setOnClickListener {
            performSearch()
        }

        listOption.setOnClickListener {
            toggleRecyclerViewLayout()
        }

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val queryText = s.toString().trim()
                if (queryText.isEmpty()) {
                    currentQueryText = ""
                    fetchData()
                } else {
                    currentQueryText = queryText
                    performSearch()
                }
            }
        })
    }

    private fun loadUserName(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("User").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val fullName = document.getString("fullName")
                tvName.text = "${fullName ?: "No Name"}!"
            } else {
                tvName.text = "No Document!"
            }
        }.addOnFailureListener { exception ->
            tvName.text = "Error: ${exception.message}!"
        }
    }

    private fun setupViews() {
        editTextSearch = findViewById(R.id.editTextSearch)
        imageViewSearch = findViewById(R.id.imageViewSearch)
        imageViewFilter = findViewById(R.id.imageViewFilter)
        recyclerViewNanny = findViewById(R.id.recyclerViewNanny)
        progressBar = findViewById(R.id.loadingIndicator)
        tvEmptyData = findViewById(R.id.tvEmptyData)
        ivEmptyData = findViewById(R.id.ivEmptyData)
        listOption = findViewById(R.id.listOption)
    }

    private fun setupRecyclerView(isGrid: Boolean) {
        val layoutManager = if (isGrid) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        recyclerViewNanny.layoutManager = layoutManager

        listOption.setImageResource(if (isGrid) R.drawable.ic_list else R.drawable.ic_grid)

        val layoutResource = if (isGrid) {
            R.layout.item_row_nanny
        } else {
            R.layout.item_list_nanny
        }

        if (!::adapter.isInitialized || adapter.layoutResource != layoutResource) {
            adapter = NannyAdapter(this, layoutResource) { nannyId ->
                val intent = Intent(this, DetailPostActivity::class.java)
                intent.putExtra("nannyId", nannyId)
                startActivity(intent)
            }
            recyclerViewNanny.adapter = adapter
        } else {
            adapter.updateLayoutResource(layoutResource)
        }
    }

    private fun fetchData() {
        progressBar.visibility = View.VISIBLE
        tvEmptyData.visibility = View.GONE
        ivEmptyData.visibility = View.GONE

        nannyRepository.getNannies(onSuccess = { nannies ->
            progressBar.visibility = View.GONE
            adapter.updateNannyList(nannies)
            checkAdapterEmpty()
        }, onFailure = { exception ->
            progressBar.visibility = View.GONE
        })
    }

    private fun performSearch() {
        val query = editTextSearch.text.toString().trim()
        if (query.isEmpty()) {
            currentQueryText = ""
            fetchData()
        } else {
            currentQueryText = query
            progressBar.visibility = View.VISIBLE
            tvEmptyData.visibility = View.GONE

            nannyRepository.searchNannies(query, onSuccess = { nannies ->
                progressBar.visibility = View.GONE
                if (nannies.isEmpty()) {
                    tvEmptyData.visibility = View.VISIBLE
                    ivEmptyData.visibility = View.VISIBLE
                    recyclerViewNanny.visibility = View.GONE
                } else {
                    tvEmptyData.visibility = View.GONE
                    ivEmptyData.visibility = View.GONE
                    recyclerViewNanny.visibility = View.VISIBLE
                    adapter.updateNannyList(nannies)
                }
            }, onFailure = { exception ->
                progressBar.visibility = View.GONE
            })
        }
    }

    private fun toggleRecyclerViewLayout() {
        isGrid = !isGrid
        setupRecyclerView(isGrid)
        if (currentQueryText.isNotEmpty()) {
            performSearch()
        } else {
            fetchData()
        }
    }

    override fun onFilterApplied(filterCriteria: Map<String, Any>) {
        if (filterCriteria.isEmpty()) {
            fetchData()
        } else {
            adapter.applyFilter(filterCriteria) {
                checkAdapterEmpty()
            }
        }
    }

    private fun checkAdapterEmpty() {
        if (adapter.isEmpty()) {
            tvEmptyData.visibility = View.VISIBLE
            ivEmptyData.visibility = View.VISIBLE
            recyclerViewNanny.visibility = View.GONE
        } else {
            tvEmptyData.visibility = View.GONE
            ivEmptyData.visibility = View.GONE
            recyclerViewNanny.visibility = View.VISIBLE
        }
    }
}
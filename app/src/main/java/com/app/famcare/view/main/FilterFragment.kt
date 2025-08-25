package com.app.famcare.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import com.app.famcare.R

class FilterFragment : DialogFragment() {
    interface FilterListener {
        fun onFilterApplied(filterCriteria: Map<String, Any>)
    }

    private var filterListener: FilterListener? = null
    private var currentFilters: Map<String, Any>? = null

    fun setFilterListener(listener: FilterListener) {
        filterListener = listener
    }

    fun setCurrentFilters(filters: Map<String, Any>?) {
        currentFilters = filters
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.90).toInt()
        val height = ViewGroup.LayoutParams.MATCH_PARENT

        val leftMargin = (displayMetrics.widthPixels * 0.10).toInt()

        dialog?.window?.setLayout(width, height)

        val layoutParams = dialog?.window?.attributes
        layoutParams?.x = leftMargin
        dialog?.window?.attributes = layoutParams

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.DialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonApply = view.findViewById<Button>(R.id.buttonApply)
        val buttonReset = view.findViewById<Button>(R.id.buttonReset)

        val buttonNanny = view.findViewById<ToggleButton>(R.id.buttonNanny)
        val buttonManny = view.findViewById<ToggleButton>(R.id.buttonManny)
        val buttonDaily = view.findViewById<ToggleButton>(R.id.buttonDaily)
        val buttonMonthly = view.findViewById<ToggleButton>(R.id.buttonMonthly)
        val buttonJabodetabek = view.findViewById<ToggleButton>(R.id.buttonJabodetabek)
        val buttonElderlyCare = view.findViewById<ToggleButton>(R.id.buttonElderlyCare)
        val buttonChildCare = view.findViewById<ToggleButton>(R.id.buttonChildCare)
        val buttonDisabilityCare = view.findViewById<ToggleButton>(R.id.buttonDisabilityCare)
        val buttonExperience = view.findViewById<ToggleButton>(R.id.buttonExperience)

        currentFilters?.let { filters ->
            buttonNanny.isChecked = filters["gender"] == "female"
            buttonManny.isChecked = filters["gender"] == "male"
            buttonDaily.isChecked = filters["type"] == "daily"
            buttonMonthly.isChecked = filters["type"] == "monthly"
            buttonJabodetabek.isChecked =
                filters["location"] == listOf("Jakarta", "Bogor", "Depok", "Tanggerang", "Bekasi")
            buttonElderlyCare.isChecked = filters["skills"] == "Elderly Care"
            buttonChildCare.isChecked = filters["skills"] == "Child Care"
            buttonDisabilityCare.isChecked = filters["skills"] == "Disability Care"
            buttonExperience.isChecked = filters["experience"] == "> 5 years"
        }

        buttonApply.setOnClickListener {
            val filterCriteria = mutableMapOf<String, Any>()

            if (buttonNanny.isChecked) filterCriteria["gender"] = "female"
            if (buttonManny.isChecked) filterCriteria["gender"] = "male"
            if (buttonDaily.isChecked) filterCriteria["type"] = "daily"
            if (buttonMonthly.isChecked) filterCriteria["type"] = "monthly"
            if (buttonJabodetabek.isChecked) filterCriteria["location"] =
                listOf("Jakarta", "Bogor", "Depok", "Tanggerang", "Bekasi")
            if (buttonElderlyCare.isChecked) filterCriteria["skills"] = "Elderly Care"
            if (buttonChildCare.isChecked) filterCriteria["skills"] = "Child Care"
            if (buttonDisabilityCare.isChecked) filterCriteria["skills"] = "Disability Care"
            if (buttonExperience.isChecked) filterCriteria["experience"] = "> 5 years"

            filterListener?.onFilterApplied(filterCriteria)
            setCurrentFilters(filterCriteria)

            dismiss()
        }

        buttonReset.setOnClickListener {
            resetFilters(view)
            filterListener?.onFilterApplied(emptyMap())
            setCurrentFilters(emptyMap())
            dismiss()
        }

        restoreFilterState(view)
    }

    private fun resetFilters(view: View) {
        val buttonNanny = view.findViewById<ToggleButton>(R.id.buttonNanny)
        val buttonManny = view.findViewById<ToggleButton>(R.id.buttonManny)
        val buttonDaily = view.findViewById<ToggleButton>(R.id.buttonDaily)
        val buttonMonthly = view.findViewById<ToggleButton>(R.id.buttonMonthly)
        val buttonJabodetabek = view.findViewById<ToggleButton>(R.id.buttonJabodetabek)
        val buttonElderlyCare = view.findViewById<ToggleButton>(R.id.buttonElderlyCare)
        val buttonChildCare = view.findViewById<ToggleButton>(R.id.buttonChildCare)
        val buttonDisabilityCare = view.findViewById<ToggleButton>(R.id.buttonDisabilityCare)
        val buttonExperience = view.findViewById<ToggleButton>(R.id.buttonExperience)

        buttonNanny.isChecked = false
        buttonManny.isChecked = false
        buttonDaily.isChecked = false
        buttonMonthly.isChecked = false
        buttonJabodetabek.isChecked = false
        buttonElderlyCare.isChecked = false
        buttonChildCare.isChecked = false
        buttonDisabilityCare.isChecked = false
        buttonExperience.isChecked = false
    }

    private fun restoreFilterState(view: View) {
        val buttonNanny = view.findViewById<ToggleButton>(R.id.buttonNanny)
        val buttonManny = view.findViewById<ToggleButton>(R.id.buttonManny)
        val buttonDaily = view.findViewById<ToggleButton>(R.id.buttonDaily)
        val buttonMonthly = view.findViewById<ToggleButton>(R.id.buttonMonthly)
        val buttonJabodetabek = view.findViewById<ToggleButton>(R.id.buttonJabodetabek)
        val buttonElderlyCare = view.findViewById<ToggleButton>(R.id.buttonElderlyCare)
        val buttonChildCare = view.findViewById<ToggleButton>(R.id.buttonChildCare)
        val buttonDisabilityCare = view.findViewById<ToggleButton>(R.id.buttonDisabilityCare)
        val buttonExperience = view.findViewById<ToggleButton>(R.id.buttonExperience)

        currentFilters?.let { filters ->
            buttonNanny.isChecked = filters["gender"] == "female"
            buttonManny.isChecked = filters["gender"] == "male"
            buttonDaily.isChecked = filters["type"] == "daily"
            buttonMonthly.isChecked = filters["type"] == "monthly"
            buttonJabodetabek.isChecked = (filters["location"] as? List<String>)?.containsAll(
                listOf("Jakarta", "Bogor", "Depok", "Tanggerang", "Bekasi")
            ) ?: false
            buttonElderlyCare.isChecked = filters["skills"] == "Elderly Care"
            buttonChildCare.isChecked = filters["skills"] == "Child Care"
            buttonDisabilityCare.isChecked = filters["skills"] == "Disability Care"
            buttonExperience.isChecked = filters["experience"] == "> 5 years"
        }
    }

}
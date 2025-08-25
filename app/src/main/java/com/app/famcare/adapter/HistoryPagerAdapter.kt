package com.app.famcare.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.famcare.view.historyimport.HistoryBDFragment
import com.app.famcare.view.historyimport.HistoryBMFragment

class HistoryPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryBDFragment()
            1 -> HistoryBMFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    companion object {
        private const val NUM_PAGES = 2
    }
}
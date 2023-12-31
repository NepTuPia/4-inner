package com.example.a4_inner.OnBoardingFragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    var fragList = listOf<Fragment>()
    override fun getItemCount(): Int = fragList.size
    override fun createFragment(position: Int): Fragment = fragList[position]
}
package com.example.chitchat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.chitchat.ui.GroupsFragment
import com.example.chitchat.ui.FragmentChats
import com.example.chitchat.ui.SettingsFragment

class FragmentAdapter(fragmentManager:FragmentManager):FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        when(position){
            0->return FragmentChats()
            2->return SettingsFragment()
            1->return GroupsFragment()

        }
        return FragmentChats()
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0->return "Chats"
            2->return "Settings"
            1->return "Groups"
        }
        return ""
    }
}
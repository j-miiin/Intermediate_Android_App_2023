package com.example.chapter6_chatting.userlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chapter6_chatting.R
import com.example.chapter6_chatting.databinding.FragmentUserBinding

class UserFragment : Fragment(R.layout.fragment_user) {

    private lateinit var binding: FragmentUserBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)

        val userListAdapter = UserAdapter()
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userListAdapter
        }

        

        userListAdapter.submitList(
            mutableListOf<UserItem?>().apply {
                add(UserItem("11", "22", "33"))
            }
        )
    }

}
package com.example.books.ui.dashboard.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.databinding.FragmentDashboardAdminBinding
import com.example.books.databinding.FragmentDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth


class DashboardUserFragment : Fragment(R.layout.fragment_dashboard_user) {

    //view binding
    private lateinit var binding: FragmentDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardUserBinding.bind(view)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //handle click, logout
        binding.logout.setOnClickListener {
            firebaseAuth.signOut()
            findNavController().navigate(R.id.action_dashboardUserFragment_to_welcomeFragment)
        }
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //not logged in, user can stay in user dashboard without login too
            binding.userTv.text = "Not Logged In"
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            binding.userTv.text = email

        }
    }
}
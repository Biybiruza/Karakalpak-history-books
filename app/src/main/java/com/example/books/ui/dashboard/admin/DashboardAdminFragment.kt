package com.example.books.ui.dashboard.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.databinding.FragmentDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardAdminFragment : Fragment(R.layout.fragment_dashboard_admin) {
    //view binding
    private lateinit var binding: FragmentDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardAdminBinding.bind(view)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //handle click, logout
        binding.logout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //handle click, start add category page
        binding.addCategoryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAdminFragment_to_categoryAddFragment)
        }
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //not logged in, goto main screen
            findNavController().navigate(R.id.action_dashboardAdminFragment_to_welcomeFragment)
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            binding.userTv.text = email

        }
    }
}
package com.example.books.ui.dashboard.profile

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.books.R
import com.example.books.databinding.FragmentProfileBinding
import com.example.books.ui.dashboard.admin.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment() : Fragment(R.layout.fragment_profile) {

    //binding
    private lateinit var binding: FragmentProfileBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        //handle click, go back
        binding.btnBack.setOnClickListener {
            (activity as Activity?)?.onBackPressed()
        }

        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)
        }
    }

    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val timestamp = snapshot.child("timestamp").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.nameTv.text = snapshot.child("name").value.toString()
                    binding.emailTv.text = snapshot.child("email").value.toString()
                    binding.userTv.text = snapshot.child("email").value.toString()
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = snapshot.child("userType").value.toString()
                    //set image
                    try {
                        Glide.with(requireContext())
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileImageV)
                    } catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
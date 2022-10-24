package com.example.books.ui.register

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.collection.arrayMapOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.strictmode.SetRetainInstanceUsageViolation
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegisterFragment : Fragment(R.layout.fragment_register) {

    //view binding
    private lateinit var binding: FragmentRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        /*init firebase auth*/
        firebaseAuth = FirebaseAuth.getInstance()

        /*init progress dialog, will show while creating account I Register user*/
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        /*handle back button click*/
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed() /*goto previous screen*/
        }

        /*handle click, begin register*/
        binding.registerBtn.setOnClickListener {
            /*Steps
            * 1) Input Data
            * 2) Validate
            * 3) Create Account - firebase Auth
            * 4) Save user info - Firebase Realtime Database*/
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        // 1) Input Data
        name = binding.username.text.toString().trim()
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()
        val cPassword = binding.cPassword.text.toString().trim()

        // 2) Validate
        if (name.isEmpty()){
            //empty name...
            Toast.makeText(requireContext(), "Enter your name...", Toast.LENGTH_LONG).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email pattern
            Toast.makeText(requireContext(), "Invalid Email Pattern...", Toast.LENGTH_LONG).show()
        } else if (password.isEmpty()){
            //empty password...
            Toast.makeText(requireContext(), "Enter password...", Toast.LENGTH_LONG).show()
        } else if (cPassword.isEmpty()){
            //empty Confirm...
            Toast.makeText(requireContext(), "Enter confirm password...", Toast.LENGTH_LONG).show()
        } else if (password != cPassword){
            Toast.makeText(requireContext(), "Password doesn't match...", Toast.LENGTH_LONG).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        // 3) Create Account - firebase Auth

        // show progress
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        // create user in Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // account created, now add user info in db
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                //failed creating account
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed creating account due do ${e.message}.", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserInfo() {
        // 4) Save user info - Firebase Realtime Database

        progressDialog.setMessage("Saving user info...")
        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap = hashMapOf<String, Any?>()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // add empty, will do in profile edit
        hashMap["userType"] = "user" // possible value are user/admin, will change value to admin manually on firebase db
        hashMap["timestamp"] = timestamp

        // set data to db
        val ref =  FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                // user info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Account created...", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_registerFragment_to_dashboardUserFragment)
            }
            .addOnFailureListener { e ->
                // failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed saving user info due do ${e.message}.", Toast.LENGTH_LONG).show()
            }
    }
}


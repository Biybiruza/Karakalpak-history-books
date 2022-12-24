package com.example.books.ui.login

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginFragment : Fragment(R.layout.fragment_login) {

    //view binding
    private lateinit var binding: FragmentLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        /*init firebase auth*/
        firebaseAuth = FirebaseAuth.getInstance()

        /*init progress dialog, will show while creating account I Register user*/
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle click, not have account, goto register screen
        binding.notAccountTv.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        //back button click
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // handle click, begin login
        binding.btnLogin.setOnClickListener {
            /*Steps
            * 1) Input Data
            * 2) Validate data
            * 3) Login - firebase Auth
            * 4) Check user type - Firebase Auth
            *    if User - Move to user dashboard
            *    if Admin - Move to admin dashboard*/
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //1) Input Data
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        //2) Validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(requireContext(), "Invalid email format...", Toast.LENGTH_LONG).show()
        } else if (password.isEmpty()){
            Toast.makeText(requireContext(), "Enter password...", Toast.LENGTH_LONG).show()
        } else{
            loginUser()
        }
    }

    private fun loginUser() {
        //3) Login - firebase Auth

        //show progress
        progressDialog.setMessage("Login In...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login success
                checkUser()
            }
            .addOnFailureListener { e ->
                //failed login
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Login failed due do ${e.message}.", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkUser() {
       /* 4) Check user type - Firebase Auth
        *    if User - Move to user dashboard
        *    if Admin - Move to admin dashboard*/

        progressDialog.setMessage("Checking User...")

        val firebaseUser = firebaseAuth.currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    val userType = snapshot.child("userType").value
                    if (userType == "user"){
                        //its simple user, open user dashboard
                        findNavController().navigate(R.id.action_loginFragment_to_dashboardUserFragment)
                    } else if (userType == "admin"){
                        // its admin, open admin dashboard
                        findNavController().navigate(R.id.action_loginFragment_to_dashboardAdminFragment)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
package com.example.books.ui.forgot_password

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    //binding
    private lateinit var binding: FragmentForgotPasswordBinding
    //progress dialog
    private lateinit var progressDialog: ProgressDialog
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentForgotPasswordBinding.bind(view)

        //init/setup progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //handle click, go back
        binding.btnBack.setOnClickListener {
            (activity as MainActivity?)?.onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            validateData()
        }
    }
    private var email = ""
    private fun validateData() {
        //get data
        email = binding.email.text.toString()

        if (email.isEmpty()){
            Toast.makeText(requireContext(), "Enter email...", Toast.LENGTH_LONG).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(requireContext(), "Invalid email pattern...", Toast.LENGTH_LONG).show()
        } else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        //show progress
        progressDialog.setMessage("Sending password reset instructions to $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //sent
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Instructions sent to \n$email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                //failed
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed email", Toast.LENGTH_LONG).show()
            }
    }
}
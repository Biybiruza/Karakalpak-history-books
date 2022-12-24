package com.example.books.ui.category

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.databinding.FragmentCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddFragment : Fragment(R.layout.fragment_category_add) {

    //view binding
    private lateinit var binding: FragmentCategoryAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCategoryAddBinding.bind(view)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //configure progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        //handle click, begin upload category
        binding.btnAddCategory.setOnClickListener {
            validateData()
        }
    }

    private var category=""

    private fun validateData() {
        //validate data

        //get data
        category = binding.categoryEt.text.toString().trim()

        //validate data
        if (category.isEmpty()){
            Toast.makeText(requireContext(),"Enter Category...", Toast.LENGTH_LONG).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        //show progress
        progressDialog.dismiss()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        // set data to add in firebase db
        val hashMap: HashMap<String,Any?> = hashMapOf() //second param is Any; because the value could be of any type
        hashMap["id"] = "$timestamp" //put in string quotes because timestamp is in double, we need in string for id
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        // add to firebase db: Database Root > Categories > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //add successfully
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Add successfully...", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                //failed to add
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Failed to add due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
package com.example.books.ui.dashboard.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.data.ModelCategory
import com.example.books.databinding.FragmentDashboardAdminBinding
import com.example.books.ui.category.AdapterCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import kotlin.collections.ArrayList

@Suppress("UNREACHABLE_CODE")
class DashboardAdminFragment : Fragment(R.layout.fragment_dashboard_admin) {
    //view binding
    private lateinit var binding: FragmentDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //adapter
    private lateinit var categoryAdapter: AdapterCategory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardAdminBinding.bind(view)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
                Toast.makeText(requireContext(), "beforeTextChanged", Toast.LENGTH_LONG).show()
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try {
                    categoryAdapter.filter.filter(s)
                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }

        })

        //handle click, logout
        binding.logout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //handle click, start add category page
        binding.addCategoryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAdminFragment_to_categoryAddFragment)
        }

        //handle click, start add pdf page
        binding.addPdfFab.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAdminFragment_to_pdfAddFragment)
        }
    }

    private fun loadCategories() {
        //init arraylist
        categoryArrayList = ArrayList()

        //get all categories from firebase database... Firebase DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to arraylist
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                categoryAdapter = AdapterCategory(requireContext())
                categoryAdapter.categoryList = categoryArrayList
                //set adapter to recyclerView
                binding.categoriesRv.adapter = categoryAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
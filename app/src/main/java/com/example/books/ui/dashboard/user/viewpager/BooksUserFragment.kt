package com.example.books.ui.dashboard.user.viewpager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.FragmentBooksUserBinding
import com.example.books.ui.dashboard.user.AdapterPdfUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment() : Fragment(R.layout.fragment_books_user) {

    private lateinit var binding: FragmentBooksUserBinding

    companion object{
        const val TAG = "books user"

        //receive data from activity to load book e.g. categoryId, category, uid
        fun newIstance(categoryId: String, category: String, uid: String): BooksUserFragment{
            val fragment = BooksUserFragment()
            val bundle = Bundle()
            bundle.putString("categoryId", categoryId)
            bundle.putString("category", category)
            bundle.putString("uid", uid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    private lateinit var adapterPdfUser: AdapterPdfUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBooksUserBinding.bind(view)

        adapterPdfUser = AdapterPdfUser()
        //get arguments that we passed in newInstance method
        val arg = arguments
        if (arg != null){
            categoryId = arg.getString("categoryId") ?: ""
            category = arg.getString("category") ?: ""
            uid = arg.getString("uid") ?: ""
        }

        Log.d(TAG,"onViewCreated: category $category")
        if (category == "All"){
            //load all book
            loadAllBook()
        } else if (category == "Most Viewed"){
            //load most viewed books
            loadMostDownloadedBooks("viewsCount")
        } else if (category == "Most Downloaded"){
            //load most downloaded books
            loadMostDownloadedBooks("downloadsCount")
        } else {
            //load selected category books
            loadCategorizedBooks()
        }

        //search
        binding.searchEt.addTextChangedListener { object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                } catch (e: Exception){
                    Log.d(TAG,"onTextChanged: SEARCH EXCEPTION ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }

        } }

        adapterPdfUser.setOnClickItemListener { bookId ->
            val bundle = bundleOf("bookId" to bookId)
            findNavController().navigate(R.id.action_dashboardUserFragment_to_pdfDetailsFragment, bundle)
        }
    }

    private fun loadCategorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser.pdfList = pdfArrayList
                //setup adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadMostDownloadedBooks(orderby: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderby).limitToLast(10) //load 10 most viewed or most downloaded books. orderBy=""
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser.pdfList = pdfArrayList
                //setup adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadAllBook() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser.pdfList = pdfArrayList
                //setup adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}
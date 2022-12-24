package com.example.books.ui.dashboard.admin.pdf_list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.FragmentPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminFragment: Fragment(R.layout.fragment_pdf_list_admin){

    private lateinit var binding: FragmentPdfListAdminBinding
    //arraylist to hold books
    private var pdfArrayList = ArrayList<ModelPdf>()
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    //category id, title
    private var categoryId = ""
    private var category = ""

    companion object{
        const val TAG = "pdf_tag"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfListAdminBinding.bind(view)
        adapterPdfAdmin = AdapterPdfAdmin()

        //get from intent, that we passed from adapter
        categoryId = arguments?.getString("categoryId") ?: ""
        category = arguments?.getString("category") ?: ""

        //set pdf category
        binding.tvSubtitle.text = category

        //load pdf/books
        loadPdfList()

        //search
        binding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //filter data
                try {
                    adapterPdfAdmin.filter.filter(s)
                }catch (e: Exception){
                    Log.d(TAG,"onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }

        })

        //back pressed
        binding.btnBack.setOnClickListener {
//            requireActivity().onBackPressed()
        }
    }

    private fun loadPdfList() {
        pdfArrayList = arrayListOf()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before start adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add to list

                        pdfArrayList.add(model!!)
                        Log.d(TAG, "onDataChange ${model.title} ${model.categoryId}")
                    }
                    adapterPdfAdmin.pdfList = pdfArrayList
                    binding.rvBooks.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
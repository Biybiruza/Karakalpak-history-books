package com.example.books.ui.dashboard.admin.pdf_list

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.FragmentPdfListAdminBinding
import com.example.books.ui.dashboard.admin.MyApplication
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
        adapterPdfAdmin = AdapterPdfAdmin(requireContext())

        //get from intent, that we passed from adapter
        categoryId = arguments?.getString("categoryId") ?: ""
        Log.d(TAG, "categoryId: $categoryId")
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
        binding.toolbar.setNavigationOnClickListener {
            (activity as MainActivity?)?.onBackPressed()
        }

        adapterPdfAdmin.setOnClickMoteBtnListener {
            Log.d(TAG,"moreOptionsDialog: $it")
            moreOptionsDialog(it)
        }

        //item click, open PdfDetailsFragment activity, lets create it first
        adapterPdfAdmin.setOnClickItemListener {
            val bundle = bundleOf("bookId" to it)
            findNavController().navigate(R.id.action_pdfListAdminFragment_to_pdfDetailsFragment, bundle)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf) {
        //options to show in dialog
        val options = arrayOf("Edit","Delete")
        //alert dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Option")
            .setItems(options){ _, position->
                //item click
                if (position == 0){
                    //Edit is clicked
                    val bundle = bundleOf("bookId" to model.id)
                    findNavController().navigate(R.id.action_pdfListAdminFragment_to_pdfEditFragment,bundle)
                } else if (position == 1){
                    //delete is clicked

                    //show confirmation dialog first if you need...
                    MyApplication.deleteBook(requireContext(), model.id,model.url, model.title)
                }
            }
            .show()
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
                        Log.d(TAG, "onDataChange ${model}")
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
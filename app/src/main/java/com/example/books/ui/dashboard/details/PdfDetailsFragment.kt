package com.example.books.ui.dashboard.details

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.databinding.FragmentPdfDetailsBinding
import com.example.books.ui.dashboard.admin.MyApplication
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailsFragment : Fragment(R.layout.fragment_pdf_details) {

    //binding
    private lateinit var binding: FragmentPdfDetailsBinding

    //book id
    private var bookId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfDetailsBinding.bind(view)

        bookId = arguments?.getString("bookId") ?: ""

        //increment book view count, whenever this page stars
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        //handle back button click, go back
        binding.btnBack.setOnClickListener {
            (activity as MainActivity?)?.onBackPressed()
        }

        //handle click, open pdf view activity
        binding.readBtn.setOnClickListener {
            val bundle = bundleOf("bookId" to bookId)
            findNavController().navigate(R.id.action_pdfDetailsFragment_to_pdfViewFragment, bundle)
        }
    }

    private fun loadBookDetails() {
        //Books > bookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val download = snapshot.child("downloadCount").value.toString()
                    val timestamp = snapshot.child("timestamp").value.toString()
                    val viewsCount = snapshot.child("viewsCount").value.toString()
                    val uid = snapshot.child("uid").value.toString()
                    val url = snapshot.child("url").value.toString()

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.calegoryTv)
                    //load pdf thumbnail, pages count
                    MyApplication.loadPdfFromSinglePage(url, title, binding.pdfView, binding.progressBar, binding.pagesTv)
                    //load pdf size
                    MyApplication.loadPdfSize(url, title, binding.sizeTv)

                    //set data
                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadTv.text = download
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}
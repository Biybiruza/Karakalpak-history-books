package com.example.books.ui.dashboard.pdf_iew

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.databinding.FragmentPdfViewBinding
import com.example.books.ui.dashboard.admin.pdf_list.Constant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewFragment : Fragment(R.layout.fragment_pdf_view) {

    companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    //binding
    private lateinit var binding: FragmentPdfViewBinding

    //book id
    private var bookId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfViewBinding.bind(view)

        bookId = arguments?.getString("bookId") ?: ""
        loadBookDetails()

        binding.btnBack.setOnClickListener {
            (activity as MainActivity?)?.onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf URL from db")
        //Database Reference to get book details e.g. get book url using book id
        //Step(1) Get Book Url using Book Id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value.toString()
                    Log.d(TAG, "loadBookDetails: PDF_URL $pdfUrl")

                    binding.tvTitle.text = snapshot.child("title").value.toString()

                    //Step(2) load pdf using url from firebase storage
                    loadBookFromUrl(pdfUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: Get Pdf from firebase storage using URL")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constant.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookFromUrl: Pdf got from URL")

                binding.pdfView.fromBytes(bytes)
                    .onPageChange { page, pageCount ->
                        //set current and total pages in toolbar subtitle
                        val currentPage = page + 1 //page start from 0 so do +1 to start from 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount" //e.g. 3/231
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError { e ->
                        Log.d(TAG, "loadBookFromUrl: ${e.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: Failed to get pdf due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}
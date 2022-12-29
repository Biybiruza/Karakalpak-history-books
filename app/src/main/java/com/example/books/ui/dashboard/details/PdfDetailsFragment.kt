package com.example.books.ui.dashboard.details

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.databinding.FragmentPdfDetailsBinding
import com.example.books.ui.dashboard.admin.MyApplication
import com.example.books.ui.dashboard.admin.pdf_list.Constant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailsFragment : Fragment(R.layout.fragment_pdf_details) {

    companion object{
        const val TAG = "pdf details"
    }

    //binding
    private lateinit var binding: FragmentPdfDetailsBinding

    //book id
    private var bookId = ""
    //get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    private lateinit var progressDialog: ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfDetailsBinding.bind(view)

        bookId = arguments?.getString("bookId") ?: ""

        //init progress bar
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

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

        //handle click, download book/pdf
        binding.downloadBtn.setOnClickListener {
            //let's check WRITE_EXTERNAL_STORAGE permission first, if granted download book, if not granted request permission
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onCreate: STORE_PERMISSION is already granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORE_PERMISSION was not granted, Lets request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted: Boolean ->
        if (isGranted){
            downloadBook()
            Log.d(TAG, "onCreate: STORE_PERMISSION is already granted")
        }else {
            Log.d(TAG, "onCreate: STORE_PERMISSION is denied")
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadBook(){
        progressDialog.setMessage("Waiting book")
        progressDialog.show()

        //lets download book from firebase storage using url
        val storgeReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storgeReference.getBytes(Constant.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book downloaded...")
                saveToDownloadFolder(bytes)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "downloadBook: Failed to download book due to ${e.message}")
                Toast.makeText(requireContext(), "Failed to download book due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadFolder: Saving downloaded book")
        val nameWithExtention = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() //create folder if not exists

            val filePath = downloadsFolder.path + "/" + nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(requireContext(), "saveToDownloadFolder: saved to download folder", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
            incrementDownloadCount()


        } catch (e: Exception){
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadFolder: failed to save due to ${e.message}")
            Toast.makeText(requireContext(), "Saved to download folder", Toast.LENGTH_LONG).show()
        }
    }

    private fun incrementDownloadCount() {
        //increment download count to firebase db
        Log.d(TAG, "incrementDownloadCount: ")

        //Step1 Get previous download count
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get downloads count
                    var downloadsCount = snapshot.child("downloadsCount").value.toString()
                    Log.d(TAG, "incrementDownloadCount: $downloadsCount")
                    if (downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }
                    //convert to long and increment 1
                    val newDownloadsCount = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New downloads count $newDownloadsCount")

                    //setup data to update to db
                    val hashMap = HashMap<String, Any>()
                    hashMap["downloadsCount"] = newDownloadsCount

                    //set to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Failed to increment due to ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
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
                    bookTitle = snapshot.child("title").value.toString()
                    val download = snapshot.child("downloadCount").value.toString()
                    val timestamp = snapshot.child("timestamp").value.toString()
                    val viewsCount = snapshot.child("viewsCount").value.toString()
                    val uid = snapshot.child("uid").value.toString()
                    bookUrl = snapshot.child("url").value.toString()

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.calegoryTv)
                    //load pdf thumbnail, pages count
                    MyApplication.loadPdfFromSinglePage(bookUrl, bookTitle, binding.pdfView, binding.progressBar, binding.pagesTv)
                    //load pdf size
                    MyApplication.loadPdfSize(bookUrl, bookTitle, binding.sizeTv)

                    //set data
                    binding.titleTv.text = bookTitle
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
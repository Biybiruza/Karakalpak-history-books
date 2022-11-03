package com.example.books.ui.upload_pdf

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Application
import android.app.Instrumentation
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.books.R
import com.example.books.data.ModelCategory
import com.example.books.databinding.FragmentPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddFragment : Fragment(R.layout.fragment_pdf_add){

    private lateinit var binding: FragmentPdfAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog (show while uploading pdf
    private lateinit var progressDialog: ProgressDialog

    //arraylist to hold pdf category
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //uri of picked pdf
    private var pdfUri: Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfAddBinding.bind(view)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        //setup progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click. goBack
//        binding.btnBack.setOnClickListener {
//            activity?.onBackPressed()
//        }

        //handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //handle click, pick pdf intent
        binding.btnAttachPdf.setOnClickListener {
            pdfPickIntent()
        }

        //handle click, start uploading pdf/book
        binding.btnSubmit.setOnClickListener {
            /*Step1: Validate data
            * Step2: Upload pdf to Firebase storage
            * Step3: Get url for uploaded pdf
            * Step4: Upload dpf info to Firebase db*/
            validateData()
        }
    }

    private var title =""
    private var description = ""
    private var category = ""

    private fun validateData() {
        //Step1: Validate data
        Log.d(TAG,"validateData: Loading pdf categories")

        //get data
        title = binding.bookTitleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //validate data
        if (title.isEmpty()){
            Toast.makeText(requireContext(), "Enter Title...", Toast.LENGTH_LONG).show()
        } else if (description.isEmpty()){
            Toast.makeText(requireContext(), "Enter Description...", Toast.LENGTH_LONG).show()
        } else if (category.isEmpty()){
            Toast.makeText(requireContext(), "Pick Category...", Toast.LENGTH_LONG).show()
        } else if (pdfUri == null){
            Toast.makeText(requireContext(), "Pick PDF...", Toast.LENGTH_LONG).show()
        } else {
            //data validated, begin upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        //Step2: Upload pdf to Firebase storage
        Log.d(TAG,"uploadPdfToStorage: Uploading to storage...")

        //show progress dialog
        progressDialog.setMessage("Loading PDF...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"
        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")

                //Step3: Get url for uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadedPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener{ e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to upload due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadedPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // Step4: Upload dpf info to Firebase db
        Log.d(TAG, "uploadedPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        //uid of current user
        val uid = firebaseAuth.uid

        var hashMap: HashMap<String, Any?> = hashMapOf()
        hashMap["uid"] = uid
        hashMap["id"] = timestamp
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId
        hashMap["url"] = uploadedPdfUrl
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadCount"] = 0

        //db reference DB > Books > BookId > (book info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadedPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_LONG).show()
                pdfUri = null
            }
            .addOnFailureListener{ e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to upload due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading pdf categories")
        //init arraylist
        categoryArrayList = ArrayList()

        //db reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG,"onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: showing pdf category pick dialog")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){ dialog, which ->
                //handle item click
                //get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //set category to textView
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF pick cancelled")
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            }
        }
    )
}
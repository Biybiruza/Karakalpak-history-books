package com.example.books.ui.dashboard.admin.pdf_list.alert_dialog

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.books.MainActivity
import com.example.books.R
import com.example.books.databinding.FragmentPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditFragment() : Fragment(R.layout.fragment_pdf_edit) {

    private companion object{
        const val TAG = "PDG_EDIT_TAG"
    }

    //binding
    private lateinit var binding: FragmentPdfEditBinding

    //book id get from intent started from AdapterPdfAdmin
    private var bookId = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arraylist to hold category titles
    private lateinit var categoryTitleArraylist: ArrayList<String>

    //arraylist to hold category ids
    private lateinit var categoryIdArraylist: ArrayList<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfEditBinding.bind(view)

        //get book id to edit the book info
        bookId = arguments?.getString("bookId") ?: ""

        //setup progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //handle click, go back
        binding.toolbar.setNavigationOnClickListener {
            (activity as MainActivity?)?.onBackPressed()
        }

        //handle click, pick category
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //handle click, begin update
        binding.btnSubmit.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString().trim()
                    val title = snapshot.child("title").value.toString().trim()

                    //set to views
                    binding.bookTitleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //load book category info using categoryId
                    Log.d(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value
                                //set to textView
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private var title =""
    private var description = ""

    private fun validateData() {
        //get data
        title = binding.bookTitleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        //validate data
        if (title.isEmpty()){
            Toast.makeText(requireContext(), "Enter Title...", Toast.LENGTH_LONG).show()
        } else if (description.isEmpty()){
            Toast.makeText(requireContext(), "Enter Description...", Toast.LENGTH_LONG).show()
        } else if (selectedCategoryId.isEmpty()){
            Toast.makeText(requireContext(), "Pick Category...", Toast.LENGTH_LONG).show()
        } else {
            //data validated, begin upload
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG,"updatePdf: Starting updating pdf info...")

        //show progress
        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        //setup data to update to db, spellings of keys must be same as in firebase
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId

        //start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"updatePdf: Updated successfully")
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Updated successfully...", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG,"updatePdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to update due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        /*Show dialog to pick the category of pdf/book. we already got the categories*/

        //make string array from array lost of String
        val categoriesArray = arrayOfNulls<String>(categoryTitleArraylist.size)
        for (i in categoryTitleArraylist.indices){
            categoriesArray[i] = categoryTitleArraylist[i]
        }
        //alert dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Category")
            .setItems(categoriesArray.size){ dialog, position ->
                //handle click, save clicked category  id and title
                selectedCategoryId = categoryIdArraylist[position]
                selectedCategoryTitle = categoryTitleArraylist[position]

                //set to textView
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show() //show dialog
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: load categories...")

        categoryTitleArraylist = arrayListOf()
        categoryIdArraylist = arrayListOf()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into them
                categoryIdArraylist.clear()
                categoryTitleArraylist.clear()

                for (ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArraylist.add(id)
                    categoryTitleArraylist.add(category)

                    Log.d(TAG,"onDataChange: Category Id $id")
                    Log.d(TAG,"onDataChange: Category Category $id")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}
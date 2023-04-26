package com.example.books.ui.dashboard.admin

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.books.ui.dashboard.admin.pdf_list.Constant
import com.example.books.ui.dashboard.details.PdfDetailsFragment
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

    }

    companion object {
        //create a static method to convert timestamp to proper date format, so we can use it everywhere in project, no need to rewirte again
        fun formatTimeStamp(timeStamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timeStamp
            //format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            //using url we can get file and its medata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size bytes $bytes")

                    //convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e ->

                }
        }

        /*instead of making new function loadPdfPageCount() to just load pages count it would be more good to use same existing function to do that
        * i.e. loadPdfFromUrlSinglePage
        * We will add another parameter of type Textview e.g pageTv
        * Whenever we call that function
        *   1) if we require page numbers we will pass pagesTv (TextView)
        *   2) if we don't require page number we will pass null
        * And in function if pagesTv (TextView) parameter is not null we will set the page number count*/

        fun loadPdfFromSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfViewer: PDFView,
            progressBar: ProgressBar,
            pageTv: TextView?
        ) {
            //using url we can get file and its medata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)

            val TAG = "LoadPdfListener:"
            ref.getBytes(Constant.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize: Size bytes $bytes")

                    //SET TO PDFVIEW
                    pdfViewer.fromBytes(bytes)
                        .pages(0) //show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadPdfFromSinglePage: Pages $nbPages")
                            //pdf loaded, we can set page count, pdf thumbnail
                            progressBar.visibility = View.INVISIBLE

                            //if pageTv param is not null then set page numbers
                            if (pageTv != null) pageTv.text = "$nbPages"
                        }
                        .load()

                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            //load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get data
                        val category = "${snapshot.child("category").value}"

                        //set data
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            //param details
            //1) context, used when require e.g. for progress dialog, toast
            //2) bookId, to delete book from db
            //3) bookUrl, detele book from firebase storage
            //4) bookTitle, show in dialog etc
            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "deleteBook: deleting...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Deleted from db too...")
                            Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_LONG)
                                .show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Deleted to from db due to ${e.message}")
                            Toast.makeText(
                                context,
                                "Failed to delete due to ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Deleted to from storage due to ${e.message}")
                    Toast.makeText(
                        context,
                        "Failed to delete due to ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            //1) get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = snapshot.child("viewsCount").value.toString()

                        if (viewsCount == "0" || viewsCount == "null") {
                            viewsCount = "0"
                        }
                        //2) Increment views count
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        var hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        fun removeFromFavorite(context: Context, bookId: String) {
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFromFavorite: removing from favorite")

            val firebaseAuth = FirebaseAuth.getInstance()
            //database ref
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: removed from favorite")
                }
                .addOnFailureListener { e ->
                    Log.d(
                        TAG,
                        "removeFromFavorite: Failed to remove from favorite due to ${e.message}"
                    )
                    Toast.makeText(
                        context,
                        "Failed to add to favorite due to ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
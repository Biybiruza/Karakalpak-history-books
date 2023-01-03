package com.example.books.ui.dashboard.profile.edit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.RowPdfFavoriteBinding
import com.example.books.ui.dashboard.admin.MyApplication
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.PdfFavoriteViewHolder>() {

    inner class PdfFavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = RowPdfFavoriteBinding.bind(itemView)
        fun populateModel(modelPdf: ModelPdf){

            loadBooksDetails(binding, modelPdf)

            //handle click, open pdf details page, pass book id to load details
            itemView.setOnClickListener {
                onClickItem.invoke(modelPdf.id)
            }

            //handle click, favorite button
            binding.ibFavoriteBtn.setOnClickListener {
                onClickRemoveFromFavoriteBtn.invoke(modelPdf.id)
            }
        }
    }

    private fun loadBooksDetails(binding: RowPdfFavoriteBinding, modelPdf: ModelPdf) {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(modelPdf.id)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    val categoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val downloadsCount = snapshot.child("downloadsCount").value.toString()
                    val timestamp = snapshot.child("timestamp").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val uid = snapshot.child("uid").value.toString()
                    val url = snapshot.child("url").value.toString()
                    val viewsCount = snapshot.child("viewsCount").value.toString()

                    //set data to model
                    modelPdf.isFavorite = true
                    modelPdf.title = title
                    modelPdf.description = description
                    modelPdf.categoryId = categoryId
                    modelPdf.timestamp = timestamp.toLong()
                    modelPdf.uid = uid
                    modelPdf.url = url
                    modelPdf.downloadCount = downloadsCount.toLong()
                    modelPdf.viewsCount = viewsCount.toLong()

                    //set data
                    binding.tvTitle.text = title
                    binding.tvDescription.text = description
                    binding.tvDate.text = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadPdfSize(url, title, binding.tvPdfSize)
                    MyApplication.loadCategory(categoryId, binding.tvCategory)
                    MyApplication.loadPdfFromSinglePage(url, title, binding.pdfViewer, binding.progressBar, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private var onClickRemoveFromFavoriteBtn: (bookId: String) -> Unit = { }
    fun setOnClickRemoveFromFavoriteBtnListener(onClick: (bookId: String) -> Unit) {
        this.onClickRemoveFromFavoriteBtn = onClick
    }

    private var onClickItem: (bookId: String) -> Unit = { }
    fun setOnClickItemListener(onClick: (bookId: String) -> Unit) {
        this.onClickItem = onClick
    }

    //model list
    var model = arrayListOf<ModelPdf>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfFavoriteViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.row_pdf_favorite, parent, false)
        return PdfFavoriteViewHolder(item)
    }

    override fun onBindViewHolder(holder: PdfFavoriteViewHolder, position: Int) {
        holder.populateModel(model[position])
    }

    override fun getItemCount(): Int = model.size
}
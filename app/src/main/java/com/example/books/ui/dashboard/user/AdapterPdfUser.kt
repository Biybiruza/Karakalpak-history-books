package com.example.books.ui.dashboard.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.RowPdfUserBinding
import com.example.books.ui.dashboard.admin.MyApplication

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.PdfUserViewHolder>(), Filterable {

    private var filterList: ArrayList<ModelPdf> = arrayListOf()
    private var filter: FilterPdfUser? = null

    inner class PdfUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = RowPdfUserBinding.bind(itemView)
        fun populateModel(model: ModelPdf){
            //set data
            binding.tvTitle.text = model.title
            binding.tvDescription.text = model.description
            binding.tvDate.text = MyApplication.formatTimeStamp(model.timestamp)

            MyApplication.loadPdfFromSinglePage(model.url,model.title, binding.pdfViewer, binding.progressBar, null)//no need number of pages so pass null

            MyApplication.loadCategory(model.categoryId, binding.tvCategory)

            MyApplication.loadPdfSize(model.url, model.title, binding.tvPdfSize)

            val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.mix_animation)
            itemView.startAnimation(animation)

            itemView.setOnClickListener {
                onItemClick.invoke(model.id)
            }

        }
    }

    private var onItemClick: (bookId: String) -> Unit = { }
    fun setOnClickItemListener(onClick: (bookId: String) -> Unit){
        this.onItemClick = onClick
    }

    var pdfList = arrayListOf<ModelPdf>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfUserViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.row_pdf_user, parent, false)
        return PdfUserViewHolder(item)
    }

    override fun onBindViewHolder(holder: PdfUserViewHolder, position: Int) {
        holder.populateModel(pdfList[position])
    }

    override fun getItemCount(): Int = pdfList.size

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }
}
package com.example.books.ui.dashboard.admin.pdf_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.RowPdfAdminBinding
import com.example.books.ui.dashboard.admin.MyApplication

class AdapterPdfAdmin(context: Context) : RecyclerView.Adapter<AdapterPdfAdmin.PdfAdminViewHolder>(),Filterable {

    private var filterList: ArrayList<ModelPdf> = arrayListOf()
    private var filter: FilterPdfAdmin? = null
    val context = context

    inner class PdfAdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = RowPdfAdminBinding.bind(itemView)
        fun populateModel(model: ModelPdf){
            //get data, set data, click etc
            binding.tvTitle.text = model.title
            binding.tvDescription.text = model.description
            binding.tvDate.text = MyApplication.formatTimeStamp(model.timestamp)

            //load further details like category, pdf from url, pdf size

            //load category
            MyApplication.loadCategory(model.categoryId, binding.tvCategory)

            //we don't need page number here, pas null for page number || load pdf thumbnail
            MyApplication.loadPdfFromSinglePage(model.url, model.title, binding.pdfViewer,binding.progressBar, null)

            //load pdf Size
            MyApplication.loadPdfSize(model.url, model.title, binding.tvPdfSize)

            //item click, show dialog with options 10) Edit book 2)Delete book
            binding.ibMoreBtn.setOnClickListener {
                onClickItem.invoke(model)
            }
            //lets create an application class that will contain the functions that will be used multiple places in app
        }
    }

    private var onClickItem: (model: ModelPdf) -> Unit = { }
    fun setOnClickItemListener(onClick: (model: ModelPdf) -> Unit) {
        this.onClickItem = onClick
    }

    var pdfList = arrayListOf<ModelPdf>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfAdminViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_pdf_admin,parent,false)
        return PdfAdminViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PdfAdminViewHolder, position: Int) {
        holder.populateModel(pdfList[position])
    }

    override fun getItemCount(): Int = pdfList.size


    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }
}
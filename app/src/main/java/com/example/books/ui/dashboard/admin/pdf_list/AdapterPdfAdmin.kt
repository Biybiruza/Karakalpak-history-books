package com.example.books.ui.dashboard.admin.pdf_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.books.R
import com.example.books.data.ModelPdf
import com.example.books.databinding.RowPdfAdminBinding

class AdapterPdfAdmin(val context: Context) : RecyclerView.Adapter<AdapterPdfAdmin.PdfAdminViewHolder>() {

    inner class PdfAdminViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        var binding = RowPdfAdminBinding.bind(itemView)
        fun populateModel(model: ModelPdf){

        }
    }

    var pdfList = arrayListOf<ModelPdf>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_pdf_admin,parent,true)
        return PdfAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfAdminViewHolder, position: Int) {
        holder.populateModel(pdfList[position])
    }

    override fun getItemCount(): Int = pdfList.size
}
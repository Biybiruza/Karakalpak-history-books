package com.example.books.ui.dashboard.admin.pdf_list

import android.widget.Filter
import com.example.books.data.ModelPdf

class FilterPdfAdmin(var filterList: ArrayList<ModelPdf>,var adapterPdfAdmin: AdapterPdfAdmin) : Filter() {

    //arrayList in which we want to search
//    lateinit var filterList: ArrayList<ModelPdf>
    //adapter in which filter need to be implemented
//    lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint // value to search
        var results = FilterResults()
        //value to be searched should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()){
            //change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            val filteredModels = arrayListOf<ModelPdf>()
            for (i in filterList.indices){
                //validate if match
                if (filterList[i].title.lowercase().contains(constraint)){
                    //searched value is similar to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            //searched value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results // don't miss
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        //apply filter changed
        adapterPdfAdmin.pdfList = results?.values as ArrayList<ModelPdf>
    }
}
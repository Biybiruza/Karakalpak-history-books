package com.example.books.ui.dashboard.user

import android.util.Log
import android.widget.Filter
import com.example.books.data.ModelPdf

class FilterPdfUser(var filterList: ArrayList<ModelPdf>,var adapterPdfUser: AdapterPdfUser): Filter() {

    override fun performFiltering(constraint: CharSequence): Filter.FilterResults {

        var constraint: CharSequence? = constraint // value to search
        Log.d("TAG", "CharSequence: constraint $constraint")
        var results = FilterResults()
        //value to be searched should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()){
            //change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            Log.d("TAG", "CharSequence: constraint $constraint")
            val filteredModels = arrayListOf<ModelPdf>()
            Log.d("TAG", "CharSequence: constraint $filteredModels")
            for (i in filterList.indices){
                //validate if match
                if (filterList[i].title.lowercase().contains(constraint)){
                    //searched value is similar to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                    Log.d("TAG", "CharSequence: constraint ${filterList[i]}")
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

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changed
        adapterPdfUser.pdfList = results.values as ArrayList<ModelPdf>
    }
}
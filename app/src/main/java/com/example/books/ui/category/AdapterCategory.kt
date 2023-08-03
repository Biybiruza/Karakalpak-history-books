package com.example.books.ui.category

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.books.R
import com.example.books.data.ModelCategory
import com.example.books.databinding.RowCategoryBinding
import com.example.books.ui.category.AdapterCategory.*
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory(context: Context) : RecyclerView.Adapter<CategoryViewHolder>(), Filterable {

    private var filterList: ArrayList<ModelCategory> = arrayListOf()
    private var filter: FilterCategory? = null
    private lateinit var navController: NavController

    val context = context

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = RowCategoryBinding.bind(itemView)
        fun populateModel(model: ModelCategory) {
            navController = NavController(context)

            //set data
            binding.categoryTv.text = model.category

            //get data
            val id = model.id
            val category = model.category
            val uid = model.uid
            val timestamp = model.timestamp

            //hanlde click, delete category
            binding.btnDelete.setOnClickListener {

                //confirm before delete
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Delete")
                    .setMessage("Are sure you want to delete this category?")
                    .setPositiveButton("Confirm") { a, d ->
                        Toast.makeText(context, "Deleting...", Toast.LENGTH_LONG).show()
                        deleteCategory(model)
                    }
                    .setNegativeButton("Cancel") { a, d ->
                        a.dismiss()
                    }
                    .show()
            }

            //handle click, start pdf listen admin activity, also pas pdf id, title
            itemView.setOnClickListener {
                val bundle = bundleOf("categoryId" to id, "category" to category)
                navController.navigate(R.id.action_dashboardAdminFragment_to_pdfListAdminFragment, bundle)
            }
        }
    }

    private fun deleteCategory(model: ModelCategory) {
        //get id of category delete
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleting...", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "unable to delete due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

     var categoryList = arrayListOf<ModelCategory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onClickItem: (model: ModelCategory) -> Unit = {}

    fun setOnClickItemListener(onClick: (models: ModelCategory) -> Unit) {
        this.onClickItem = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.populateModel(categoryList[position])
    }

    override fun getItemCount(): Int = categoryList.size //number of item in list

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }

}

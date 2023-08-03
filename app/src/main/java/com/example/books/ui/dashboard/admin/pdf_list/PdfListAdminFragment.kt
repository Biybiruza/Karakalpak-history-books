package com.example.books.ui.dashboard.admin.pdf_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.books.R
import com.example.books.databinding.FragmentPdfListAdminBinding

class PdfListAdminFragment() : Fragment(R.layout.fragment_pdf_list_admin){

    private lateinit var binding: FragmentPdfListAdminBinding

    //category id, title
    private var categoryId = ""
    private var category = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfListAdminBinding.bind(view)

        //get from intent, that we passed from adapter
        categoryId = arguments?.getString("categoryId") ?: ""
        category = arguments?.getString("category") ?: ""

    }
}
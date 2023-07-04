package com.example.books.data

data class ModelPdf(
    var uid: String = "",
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var categoryId: String = "",
    var url: String = "",
    var timestamp: Long = 0,
    var viewsCount: Long = 0,
    var downloadCount: Int = 0,
    var isFavorite: Boolean = false
)

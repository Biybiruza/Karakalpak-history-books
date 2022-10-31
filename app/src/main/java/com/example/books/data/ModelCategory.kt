package com.example.books.data

data class ModelCategory (
    //variable, must match as in firebase
    var id: String = "",
    var category: String = "",
    var timestamp: Long = 0,
    var uid: String = ""
    )
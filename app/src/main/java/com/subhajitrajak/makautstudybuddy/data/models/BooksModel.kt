package com.subhajitrajak.makautstudybuddy.data.models

import java.io.Serializable

data class BooksModel (
    val semester: String = "",
    val bookName: String = "",
    val bookPDF: String = "",
    val contributor: String? = null
): Serializable
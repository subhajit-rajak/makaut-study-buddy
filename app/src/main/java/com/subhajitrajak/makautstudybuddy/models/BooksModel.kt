package com.subhajitrajak.makautstudybuddy.models

import java.io.Serializable

data class BooksModel (
    val semester: String = "",
    val bookName: String = "",
    val bookPDF: String = "",
): Serializable
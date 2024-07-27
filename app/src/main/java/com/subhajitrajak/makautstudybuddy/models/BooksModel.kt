package com.subhajitrajak.makautstudybuddy.models

import java.io.Serializable

data class BooksModel (
    val branch: String = "",
    val bookName: String = "",
    val bookPDF: String = "",
): Serializable
package com.subhajitrajak.makautstudybuddy.data.models

import com.subhajitrajak.makautstudybuddy.utils.Constants
import java.io.Serializable

data class BooksModel (
    val id: String = "",
    val semester: String = "",
    val bookName: String = "",
    val bookPDF: String = "",
    val contributor: String? = null,
    val contributorEmail: String? = null,
    val type: String? = null,
    val branch: String? = null,
    val status: String? = null
): Serializable
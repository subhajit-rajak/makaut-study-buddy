package com.subhajitrajak.makautstudybuddy.data.models

import java.io.Serializable

data class SyllabusSemModel(
    val id: String = "",
    val semester:String?=null,
    val branch:String?=null,
    val file: String = "",
    val lastUpdated: String = "",
): Serializable
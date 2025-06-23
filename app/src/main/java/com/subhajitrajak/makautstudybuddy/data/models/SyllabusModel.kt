package com.subhajitrajak.makautstudybuddy.data.models

data class SyllabusModel(
    val id: String = "",
    val branch:String?=null,
    val semList:ArrayList<SyllabusSemModel>?=null,
)
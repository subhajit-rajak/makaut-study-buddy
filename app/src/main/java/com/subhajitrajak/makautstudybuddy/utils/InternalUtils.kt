package com.subhajitrajak.makautstudybuddy.utils

import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOKS
import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOKS_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.CE
import com.subhajitrajak.makautstudybuddy.utils.Constants.CSE
import com.subhajitrajak.makautstudybuddy.utils.Constants.ECE
import com.subhajitrajak.makautstudybuddy.utils.Constants.EE
import com.subhajitrajak.makautstudybuddy.utils.Constants.IT
import com.subhajitrajak.makautstudybuddy.utils.Constants.ME
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.ORGANIZERS_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

fun getBranchCode(branch: String): String {
    return when(branch) {
        CSE -> "0"
        IT -> "1"
        ECE -> "2"
        ME -> "3"
        CE -> "4"
        EE -> "5"
        else -> "9999"
    }
}

fun getTypeCode(type: String): String {
    return when(type) {
        NOTES -> NOTES_DATA
        BOOKS -> BOOKS_DATA
        else -> ORGANIZERS_DATA
    }
}

suspend fun hasFastInternet(): Boolean = withContext(Dispatchers.IO) {
    try {
        val start = System.currentTimeMillis()
        val url = URL("https://clients3.google.com/generate_204")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 1000 // 1 second
        connection.readTimeout = 1000
        connection.connect()
        val end = System.currentTimeMillis()
        connection.responseCode == 204 && (end - start) < 1000 // under 1s
    } catch (e: Exception) {
        false
    }
}
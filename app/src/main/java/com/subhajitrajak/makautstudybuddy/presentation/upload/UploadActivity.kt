package com.subhajitrajak.makautstudybuddy.presentation.upload

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.ActivityUploadBinding

class UploadActivity : AppCompatActivity() {
    private val binding: ActivityUploadBinding by lazy {
        ActivityUploadBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val branchList = arrayOf("Computer Science & Engineering", "Information Technology", "Electronics & Communication", "Mechanical Engineering", "Civil Engineering", "Electrical Engineering")
        val branchAdapter= ArrayAdapter(this, R.layout.item_dropdown_list, branchList)
        val branchDropdown = binding.listOfBranches
        branchDropdown.setAdapter(branchAdapter)

        val semList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
        val semAdapter= ArrayAdapter(this, R.layout.item_dropdown_list, semList)
        val semDropDown = binding.listOfSemesters
        semDropDown.setAdapter(semAdapter)
    }
}
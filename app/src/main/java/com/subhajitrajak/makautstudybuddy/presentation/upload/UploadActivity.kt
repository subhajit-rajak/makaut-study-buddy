package com.subhajitrajak.makautstudybuddy.presentation.upload

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.chip.Chip
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.data.repository.UploadRepo
import com.subhajitrajak.makautstudybuddy.data.repository.userLogin.GoogleAuthUiClient
import com.subhajitrajak.makautstudybuddy.data.repository.userLogin.UserData
import com.subhajitrajak.makautstudybuddy.databinding.ActivityUploadBinding
import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOKS
import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOK_LIST
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES
import com.subhajitrajak.makautstudybuddy.utils.Constants.PENDING
import com.subhajitrajak.makautstudybuddy.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.getBranchCode
import com.subhajitrajak.makautstudybuddy.utils.getTypeCode
import com.subhajitrajak.makautstudybuddy.utils.log
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showToast
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim
import java.lang.String.format

@Suppress("LABEL_NAME_CLASH")
class UploadActivity : AppCompatActivity() {
    // for firebase upload purposes
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private var selectedPdfUri: Uri? = null
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    // initializing binding
    private val binding: ActivityUploadBinding by lazy {
        ActivityUploadBinding.inflate(layoutInflater)
    }

    // to get current user name
    private lateinit var googleAuthUiClient: GoogleAuthUiClient
    private var userData: UserData? = null
    private var userName: String = "Anonymous"
    private var userEmail: String = ""

    // for upload requests recycler view
    private val list = ArrayList<BooksModel>()
    private val adapter = UploadAdapter(this, list)
    private val repo = UploadRepo(this)
    private val viewModel by lazy {
        ViewModelProvider(this, UploadViewModelFactory(repo))[UploadViewModel::class.java]
    }

    @SuppressLint("DefaultLocale")
    private val pickPdfLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                selectedPdfUri = it
                val fileInfo = getFileInfo(it)
                val fileName = fileInfo.first
                val fileSize = fileInfo.second.toFloat()
                val fileSizeInMB = format("%.3f", fileSize / 1000000.0)
                binding.fileNameTextView.text =
                    getString(R.string.filename_after_pickup, fileName, fileSizeInMB)
                binding.fileNameTextView.showWithAnim()

                if (fileSize > 10000000) {
                    showToast(this, "File size should be less than 10MB")
                    binding.progressBar.removeWithAnim()
                    binding.chooseFileButton.text = getString(R.string.choose_file)
                    binding.submitButton.isEnabled = false
                } else {
                    binding.submitButton.isEnabled = true
                }
            } ?: run {
                showToast(this, "No file selected")
            }
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

        // Initialize GoogleAuthUiClient
        googleAuthUiClient = GoogleAuthUiClient(this, Identity.getSignInClient(this))

        // get user name
        userData = googleAuthUiClient.getSignedInUser()
        if (userData != null) {
            userName = userData!!.username ?: "Anonymous"
            userEmail = userData!!.userEmail ?: ""
        }

        val branchList = arrayOf(
            "Computer Science & Engineering",
            "Information Technology",
            "Electronics & Communication",
            "Mechanical Engineering",
            "Civil Engineering",
            "Electrical Engineering"
        )
        val branchAdapter = ArrayAdapter(this, R.layout.item_dropdown_list, branchList)
        val branchDropdown = binding.listOfBranches
        branchDropdown.setAdapter(branchAdapter)

        val semList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
        val semAdapter = ArrayAdapter(this, R.layout.item_dropdown_list, semList)
        val semDropDown = binding.listOfSemesters
        semDropDown.setAdapter(semAdapter)

        setupUploadRequests()

        binding.apply {
            backButton.setOnClickListener {
                finish()
            }

            pullToRefresh.setOnRefreshListener {
                viewModel.getRequestsData()
                pullToRefresh.isRefreshing = false
            }

            chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                val selectedChipId = checkedIds.firstOrNull()
                val selectedChip = selectedChipId?.let { group.findViewById<Chip>(it) }
                val type = selectedChip?.text.toString()

                when (type) {
                    NOTES -> {
                        topicInputLayout.showWithAnim()
                        authorInputLayout.visibility = View.GONE
                        subjectInputLayout.hint = getString(R.string.subject)
                    }
                    BOOKS -> {
                        authorInputLayout.showWithAnim()
                        topicInputLayout.visibility = View.GONE
                        subjectInputLayout.hint = "Book Name"
                    }
                    else -> {
                        topicInputLayout.visibility = View.GONE
                        authorInputLayout.visibility = View.GONE
                        subjectInputLayout.hint = getString(R.string.subject)
                    }
                }
            }

            binding.fileNameTextView.removeWithAnim()
            binding.progressBar.removeWithAnim()
            binding.chooseFileButton.text = getString(R.string.choose_file)

            contributorCheckBox.text = getString(R.string.contributed_by_who, userName)

            chooseFileButton.setOnClickListener {
                pickPdfLauncher.launch(arrayOf("application/pdf"))
            }

            submitButton.setOnClickListener {
                val type =
                    binding.chipGroup.findViewById<Chip>(binding.chipGroup.checkedChipId)?.text.toString()
                val subject = binding.editTextBookName.text.toString().trim()
                val topic = if (type == NOTES) binding.editTextTopicName.text.toString().trim() else null
                val author = if (type == BOOKS) binding.editTextAuthorName.text.toString().trim() else null
                val branch = binding.listOfBranches.text.toString()
                val semester = binding.listOfSemesters.text.toString()
                val isContributorEnabled = binding.contributorCheckBox.isChecked
                val contributor = if (isContributorEnabled) {
                    userName
                } else "Anonymous"

                if (type.isEmpty() || subject.isEmpty() || branch.isEmpty() || semester.isEmpty()) {
                    showToast(this@UploadActivity, "Please fill all the fields")
                    return@setOnClickListener
                }

                if (selectedPdfUri == null) {
                    showToast(this@UploadActivity, "Please select a PDF file")
                    return@setOnClickListener
                }

                if (type == NOTES && topic.isNullOrEmpty()) {
                    showToast(this@UploadActivity, "Please enter a topic name")
                    return@setOnClickListener
                }

                if (type == BOOKS && author.isNullOrEmpty()) {
                    showToast(this@UploadActivity, "Please enter an author name")
                    return@setOnClickListener
                }

                uploadPdf(
                    type = type,
                    subject = subject,
                    topic = topic,
                    author = author,
                    branch = branch,
                    branchCode = getBranchCode(branch),
                    semester = semester,
                    contributor = contributor
                )
            }
        }
    }

    // Function to get the file name from URI
    private fun getFileInfo(uri: Uri): Pair<String, Long> {
        var fileName = "Unknown"
        var fileSize = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }
        return Pair(fileName, fileSize)
    }

    private fun uploadPdf(
        type: String,
        subject: String,
        topic: String?,
        author: String?,
        branch: String,
        branchCode: String,
        semester: String,
        contributor: String
    ) {
        database = firebaseDatabase.getReference(UPLOAD_REQUESTS).child(getTypeCode(type))
        storage = FirebaseStorage.getInstance().reference

        selectedPdfUri?.let { url ->
            val fileName = when (type) {
                NOTES -> {
                    "$subject-$topic.pdf"
                }
                BOOKS -> {
                    "$subject-$author.pdf"
                }
                else -> {
                    "$subject.pdf"
                }
            }
            val fileRef = storage.child(fileName)

            binding.progressBar.showWithAnim()
            binding.chooseFileButton.text = ""
            binding.submitButton.text = getString(R.string.uploading)

            fileRef.putFile(url)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val pdfUrl = uri.toString()
                        val bookId = database.child(branchCode).child(BOOK_LIST).push().key
                        if (bookId == null) {
                            showToast(this, "Failed to generate book ID")
                            return@addOnSuccessListener
                        }

                        val newBook = BooksModel(
                            id = bookId,
                            bookName = subject,
                            topicName = topic,
                            authorName = author,
                            bookPDF = pdfUrl,
                            semester = semester,
                            contributor = contributor,
                            contributorEmail = userEmail,
                            type = type,
                            branch = branch,
                            status = PENDING
                        )

                        database.child(bookId)
                            .setValue(newBook)
                            .addOnSuccessListener {
                                clearFields()
                                showToast(this, "Upload successful")

                            }.addOnFailureListener {
                                binding.progressBar.removeWithAnim()
                                binding.chooseFileButton.text = getString(R.string.choose_file)
                                showToast(this, "Failed to upload PDF")
                            }
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.removeWithAnim()
                    binding.chooseFileButton.text = getString(R.string.choose_file)
                    showToast(this, "No file selected")
                }
        }
    }

    private fun clearFields() {
        binding.progressBar.removeWithAnim()
        binding.chooseFileButton.text = getString(R.string.choose_file)
        binding.submitButton.text = getString(R.string.submit_request)
        binding.fileNameTextView.removeWithAnim()
        binding.editTextBookName.text.clear()
        binding.editTextAuthorName.text.clear()

        viewModel.getRequestsData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupUploadRequests() {
        binding.apply {
            rvUploadRequests.layoutManager = LinearLayoutManager(this@UploadActivity)
            rvUploadRequests.adapter = adapter
            viewModel.getRequestsData()

            viewModel.uploadRequestsLiveData.observe(this@UploadActivity) {
                when (it) {
                    is MyResponses.Error -> {
                        binding.errorLayout.showWithAnim()
                        binding.rvUploadRequests.removeWithAnim()
                    }

                    is MyResponses.Loading -> {}

                    is MyResponses.Success -> {
                        binding.errorLayout.removeWithAnim()
                        binding.rvUploadRequests.showWithAnim()
                        val tempList = it.data
                        list.clear()
                        tempList?.forEach { request ->
                            if (userData?.userEmail == request.contributorEmail) {
                                list.add(request)
                            }
                        }
                        log("list: $list")

                        if (list.isEmpty()) {
                            binding.errorLayout.showWithAnim()
                            binding.rvUploadRequests.removeWithAnim()
                        } else {
                            binding.errorLayout.removeWithAnim()
                            binding.rvUploadRequests.showWithAnim()
                        }

                        adapter.notifyItemRangeChanged(0, list.size)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearListeners()
    }
}
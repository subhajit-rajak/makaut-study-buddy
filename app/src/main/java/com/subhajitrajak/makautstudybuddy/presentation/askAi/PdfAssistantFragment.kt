package com.subhajitrajak.makautstudybuddy.presentation.askAi

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModel
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModelFactory
import com.subhajitrajak.makautstudybuddy.data.repository.AiModelsRepository
import com.subhajitrajak.makautstudybuddy.databinding.FragmentPdfAssistantBinding
import com.subhajitrajak.makautstudybuddy.utils.log
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import java.util.Locale

class PdfAssistantFragment : Fragment() {
    private var _binding: FragmentPdfAssistantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfAssistantViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels {
        SubscriptionViewModelFactory(requireContext().applicationContext)
    }
    private val aiModelsViewModel: AiModelsViewModel by viewModels {
        AiModelsViewModelFactory(AiModelsRepository())
    }

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    private val requestMicPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(requireContext(), "Microphone permission is required to use speech-to-text", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfAssistantBinding.inflate(inflater, container, false)

        setupSpeechRecognition()
        setupModelSpinner()
        setupAiAssistant()
        setupQuickPrompts()
        setupSnapShotCard()

        return binding.root
    }

    private fun hideListeningOverlay() {
        binding.listeningOverlay.visibility = View.GONE
        binding.listeningText.visibility = View.GONE
    }

    private fun startListening() {
        binding.listeningOverlay.showWithAnim()
        binding.listeningText.showWithAnim()

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                hideListeningOverlay()
            }
            override fun onError(error: Int) {
                hideListeningOverlay()
                val message = when (error) {
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission error"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Try again!"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Speech error: $error"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }


            override fun onResults(results: Bundle?) {
                hideListeningOverlay()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    binding.messageEditText.setText(spokenText)
                    binding.messageEditText.setSelection(spokenText.length)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechIntent)
    }

    private fun setupSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
            binding.talkButton.isEnabled = false
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
        }

        binding.talkButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                    startListening()
                }
                else -> {
                    requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    private fun setupAiAssistant() {
        val initialPrompt = arguments?.getString("initial_prompt")
        val markwon = Markwon.create(requireContext())

        binding.sendButton.setOnClickListener {
            val question = binding.messageEditText.text.toString()
            if (question.isNotBlank()) {
                initialPrompt?.let {
                    viewModel.askDeepSeek("$question\nwith respect to the following page\n$initialPrompt")
                } ?: run {
                    viewModel.askDeepSeek(question)
                }

                binding.messageEditText.setText("")
            }
        }

        viewModel.response.observe(viewLifecycleOwner) {
            binding.tvResponse.visibility = View.VISIBLE
            binding.emptyResponse.visibility = View.GONE
            binding.snapshotCard.removeWithAnim()
            binding.lottieAnimationView.visibility = View.GONE

            val markdown = markwon.toMarkdown(it)
            markwon.setParsedMarkdown(binding.tvResponse, markdown)
        }
    }

    private fun setupModelSpinner() {
        lifecycleScope.launch {
            try {
                // Fetch once and cache in memory
                val models = aiModelsViewModel.getAllModels()

                // Observe premium status
                subscriptionViewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
                    log(models.toString())
                    val adapter = ModelSpinnerAdapter(requireContext(), models, isPremium)
                    adapter.setDropDownViewResource(R.layout.spinner_item_with_icon)
                    binding.modelSpinner.adapter = adapter

                    // Default selection to first model
                    if (binding.modelSpinner.selectedItemPosition == AdapterView.INVALID_POSITION) {
                        binding.modelSpinner.setSelection(0)
                        viewModel.setModel(models[0].identifier)
                    }
                }

                var lastValidPosition = 0 // To store previous valid selection

                binding.modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedModel = models[position]
                        val isPremium = subscriptionViewModel.isPremium.value ?: false

                        if (selectedModel.isPremium && !isPremium) {
                            Toast.makeText(
                                requireContext(),
                                "This model is premium only",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.modelSpinner.setSelection(lastValidPosition)
                            return
                        }

                        lastValidPosition = position
                        viewModel.setModel(selectedModel.identifier)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            } catch (e: Exception) {
                log("Failed to setup model spinner: ${e.message}")
            }
        }
    }

    private fun setupQuickPrompts() {
        binding.apply {
            summarize.setOnClickListener {
                messageEditText.setText(summarize.text.toString())
            }

            explain.setOnClickListener {
                messageEditText.setText(explain.text.toString())
            }

            generateMCQs.setOnClickListener {
                messageEditText.setText(generateMCQs.text.toString())
            }
        }
    }

    private fun setupSnapShotCard() {
        val snapshotBytes = arguments?.getByteArray("snapshot")
        snapshotBytes?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.pdfSnapshot.setImageBitmap(bitmap)
        } ?: run {
            binding.snapshotCard.visibility = View.GONE
        }

        binding.pdfSnapshot.setOnClickListener {
            snapshotBytes?.let { bytes ->
                val dialog =
                    Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                val imageView = ImageView(requireContext()).apply {
                    setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setBackgroundColor(Color.BLACK)
                    setOnClickListener {
                        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
                        startAnimation(animation)

                        animation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                dialog.dismiss()
                            }
                            override fun onAnimationRepeat(animation: Animation?) {}
                        })
                    }
                }
                dialog.setContentView(imageView)
                dialog.show()

                val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
                imageView.startAnimation(animation)
            }
        }
    }

    companion object {
        fun newInstance(initialPrompt: String, snapshot: ByteArray): PdfAssistantFragment {
            val fragment = PdfAssistantFragment()
            val args = Bundle().apply {
                putString("initial_prompt", initialPrompt)
                putByteArray("snapshot", snapshot)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer.destroy()
        _binding = null
    }
}
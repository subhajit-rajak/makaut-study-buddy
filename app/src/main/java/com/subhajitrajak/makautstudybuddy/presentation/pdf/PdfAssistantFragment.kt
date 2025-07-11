package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.app.Dialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.FragmentPdfAssistantBinding
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import io.noties.markwon.Markwon

class PdfAssistantFragment : Fragment() {
    private var _binding: FragmentPdfAssistantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfAssistantViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfAssistantBinding.inflate(inflater, container, false)

        val models = resources.getStringArray(R.array.ai_models)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, models)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        binding.modelSpinner.adapter = adapter

        binding.modelSpinner.setSelection(0)
        binding.modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setModel(models[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val initialPrompt = arguments?.getString("initial_prompt")
        val markwon = Markwon.create(requireContext())

        binding.sendButton.setOnClickListener {
            binding.tvResponse.visibility = View.VISIBLE
            binding.emptyResponse.visibility = View.GONE
            binding.snapshotCard.removeWithAnim()

            val question = binding.messageEditText.text.toString()
            if (question.isNotBlank()) {
                viewModel.askDeepSeek("$question\nwith respect to the following page\n$initialPrompt")
                binding.messageEditText.setText("")
            }
        }

        viewModel.response.observe(viewLifecycleOwner) {
            val markdown = markwon.toMarkdown(it)
            markwon.setParsedMarkdown(binding.tvResponse, markdown)
        }

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

        val snapshotBytes = arguments?.getByteArray("snapshot")
        snapshotBytes?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.pdfSnapshot.setImageBitmap(bitmap)
        }

        binding.pdfSnapshot.setOnClickListener {
            val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            val imageView = ImageView(requireContext()).apply {
                setImageBitmap(BitmapFactory.decodeByteArray(snapshotBytes, 0, snapshotBytes!!.size))
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.BLACK)
                setOnClickListener { dialog.dismiss() }
            }
            dialog.setContentView(imageView)
            dialog.show()
        }

        return binding.root
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
        _binding = null
    }
}
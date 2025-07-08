package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.FragmentPdfAssistantBinding

class PdfAssistantFragment : Fragment() {
    private var _binding: FragmentPdfAssistantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfAssistantViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfAssistantBinding.inflate(inflater, container, false)

        val initialPrompt = arguments?.getString("initial_prompt")

        binding.sendButton.setOnClickListener {
            binding.tvResponse.visibility = View.VISIBLE
            binding.emptyResponse.visibility = View.GONE

            val question = binding.messageEditText.text.toString()
            if (question.isNotBlank()) {
                viewModel.askDeepSeek("$question\nwith respect to the following page\n$initialPrompt")
                binding.messageEditText.setText("")
            }
        }

        viewModel.response.observe(viewLifecycleOwner) {
            binding.tvResponse.text = it
        }

        return binding.root
    }

    companion object {
        fun newInstance(initialPrompt: String): PdfAssistantFragment {
            val fragment = PdfAssistantFragment()
            val args = Bundle().apply {
                putString("initial_prompt", initialPrompt)
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
package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.FragmentPdfAssistantBinding
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
            val question = binding.messageEditText.text.toString()
            if (question.isNotBlank()) {
                viewModel.askDeepSeek("$question\nwith respect to the following page\n$initialPrompt")
                binding.messageEditText.setText("")
            }
        }

        viewModel.response.observe(viewLifecycleOwner) {
            binding.tvResponse.visibility = View.VISIBLE
            binding.emptyResponse.visibility = View.GONE

            val markdown = markwon.toMarkdown(it)
            markwon.setParsedMarkdown(binding.tvResponse, markdown)
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
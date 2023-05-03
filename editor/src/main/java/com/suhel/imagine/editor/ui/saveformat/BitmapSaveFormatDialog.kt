package com.suhel.imagine.editor.ui.saveformat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.suhel.imagine.editor.databinding.DialogSaveFormatBinding
import com.suhel.imagine.editor.model.BitmapSaveFormat

class BitmapSaveFormatDialog : BottomSheetDialogFragment() {

    private var _binding: DialogSaveFormatBinding? = null
    private val binding: DialogSaveFormatBinding
        get() = _binding!!

    var onChoose: ((BitmapSaveFormat) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSaveFormatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPng.setOnClickListener {
            onChoose?.invoke(BitmapSaveFormat.PNG)
            dismiss()
        }

        binding.btnJpeg.setOnClickListener {
            onChoose?.invoke(BitmapSaveFormat.JPEG)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
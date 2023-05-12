package com.suhel.imagine.editor.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.suhel.imagine.editor.databinding.DialogChooseBlendModeBinding
import com.suhel.imagine.editor.model.NamedBlendMode

class ChooseBlendModeDialog : BottomSheetDialogFragment() {

    var onSelected: ((NamedBlendMode) -> Unit)? = null

    private var _binding: DialogChooseBlendModeBinding? = null
    private val binding: DialogChooseBlendModeBinding
        get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        (dialog as BottomSheetDialog).behavior.apply {
            isDraggable = true
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChooseBlendModeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lstBlendModes.layoutManager = LinearLayoutManager(view.context)
        binding.lstBlendModes.setHasFixedSize(true)
        binding.lstBlendModes.adapter = BlendModesAdapter {
            onSelected?.invoke(it)
            dismiss()
        }
    }

}
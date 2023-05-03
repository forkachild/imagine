package com.suhel.imagine.editor.ui.addlayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.suhel.imagine.editor.databinding.DialogAddLayerBinding
import com.suhel.imagine.editor.layers.EffectLayer

class AddLayerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddLayerBinding? = null
    private val binding: DialogAddLayerBinding
        get() = _binding!!

    private lateinit var adapter: AddLayerAdapter

    var onAddLayer: ((EffectLayer) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddLayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AddLayerAdapter()
        adapter.onAddLayer = {
            onAddLayer?.invoke(it)
            dismiss()
        }

        binding.lstLayers.setHasFixedSize(true)
        binding.lstLayers.layoutManager = LinearLayoutManager(requireContext())
        binding.lstLayers.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
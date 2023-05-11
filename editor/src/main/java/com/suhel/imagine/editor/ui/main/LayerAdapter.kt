package com.suhel.imagine.editor.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.slider.Slider.OnChangeListener
import com.suhel.imagine.core.types.ImagineBlendMode
import com.suhel.imagine.editor.R
import com.suhel.imagine.editor.databinding.ItemLayerBinding
import com.suhel.imagine.editor.extensions.displayText
import com.suhel.imagine.editor.model.layers.EffectLayer

class LayerAdapter : Adapter<LayerAdapter.LayerViewHolder>() {

    var data: List<EffectLayer> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onStartDrag: ((ViewHolder) -> Unit)? = null
    var onDelete: ((Int) -> Unit)? = null
    var onVisibilityToggle: ((Int) -> Unit)? = null
    var onLayerUpdated: ((Int, Float) -> Unit)? = null
    var onBlendModeUpdated: ((Int, Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LayerViewHolder = LayerViewHolder(parent)

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("ClickableViewAccessibility")
    inner class LayerViewHolder(parent: ViewGroup) : ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_layer, parent, false)
    ) {

        private val binding: ItemLayerBinding = ItemLayerBinding.bind(itemView)

        init {
            binding.sldIntensity.addOnChangeListener(
                OnChangeListener { _, value, fromUser ->
                    if (fromUser)
                        onLayerUpdated?.invoke(adapterPosition, value)
                }
            )
            binding.btnDrag.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    onStartDrag?.invoke(this)
                    return@setOnTouchListener true
                }

                false
            }
            binding.btnDelete.setOnClickListener {
                onDelete?.invoke(adapterPosition)
            }
            binding.btnVisibility.setOnClickListener {
                onVisibilityToggle?.invoke(adapterPosition)
            }
            binding.spnBlendMode.adapter = ArrayAdapter(
                parent.context,
                R.layout.item_blend_mode,
                R.id.tvName,
                ImagineBlendMode.all.map { it.displayText }
            )
            binding.spnBlendMode.onItemSelectedListener = object : OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onBlendModeUpdated?.invoke(adapterPosition, position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }

        fun bind(value: EffectLayer) {
            binding.tvName.text = value.name
            binding.sldIntensity.value = value.layerIntensity
            binding.spnBlendMode.setSelection(value.layerBlendModeIdx)
            binding.btnVisibility.setImageResource(
                if (value.layerVisible) R.drawable.ic_visible else R.drawable.ic_invisible
            )
        }

    }

}
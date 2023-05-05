package com.suhel.imagine.editor.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.slider.Slider.OnChangeListener
import com.suhel.imagine.editor.R
import com.suhel.imagine.editor.databinding.ItemLayerBinding
import com.suhel.imagine.editor.model.layers.EffectLayer

class LayerAdapter : Adapter<LayerAdapter.LayerViewHolder>() {

    var data: List<EffectLayer> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onStartDrag: ((ViewHolder) -> Unit)? = null
    var onLayerUpdated: ((Int, Float) -> Unit)? = null

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
        }

        fun bind(value: EffectLayer) {
            binding.tvName.text = value.name
            binding.sldIntensity.value = value.factor
        }

    }

}
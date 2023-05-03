package com.suhel.imagine.editor.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.slider.Slider.OnChangeListener
import com.suhel.imagine.editor.R
import com.suhel.imagine.editor.databinding.ItemLayerBinding
import com.suhel.imagine.editor.layers.EffectLayer

class LayerAdapter : Adapter<LayerAdapter.LayerViewHolder>() {

    var data: List<EffectLayer> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onLayerUpdated: ((Int, Float) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LayerViewHolder = LayerViewHolder(parent)

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

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
        }

        fun bind(value: EffectLayer) {
            binding.tvName.text = value.name.uppercase()
            binding.sldIntensity.value = value.factor
        }

    }

    class ListDiff<T>(
        private val oldList: List<T>,
        private val newList: List<T>,
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]

    }

}
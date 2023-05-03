package com.suhel.imagine.editor.ui.addlayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.suhel.imagine.editor.R
import com.suhel.imagine.editor.databinding.ItemAddLayerBinding
import com.suhel.imagine.editor.layers.EffectLayer
import com.suhel.imagine.editor.layers.examples.allLayers

class AddLayerAdapter : Adapter<AddLayerAdapter.AddLayerViewHolder>() {

    private val data: List<EffectLayer>
        get() = allLayers

    var onAddLayer: ((EffectLayer) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddLayerViewHolder = AddLayerViewHolder(parent)

    override fun onBindViewHolder(holder: AddLayerViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class AddLayerViewHolder(parent: ViewGroup) : ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_add_layer, parent, false)
    ) {

        private val binding: ItemAddLayerBinding = ItemAddLayerBinding.bind(itemView)

        init {
            binding.root.setOnClickListener {
                onAddLayer?.invoke(data[adapterPosition])
            }
        }

        fun bind(value: EffectLayer) {
            binding.tvName.text = value.name
        }

    }

}
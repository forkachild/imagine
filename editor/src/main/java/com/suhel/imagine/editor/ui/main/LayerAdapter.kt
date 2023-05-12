package com.suhel.imagine.editor.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.slider.Slider.OnChangeListener
import com.suhel.imagine.editor.R
import com.suhel.imagine.editor.databinding.ItemLayerBinding
import com.suhel.imagine.editor.model.NamedBlendMode
import com.suhel.imagine.editor.model.layers.EffectLayer

class LayerAdapter(
    // TODO: Remove this
    private val fragmentManager: FragmentManager
) : Adapter<LayerAdapter.LayerViewHolder>() {

    var data: List<EffectLayer> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onStartDrag: ((ViewHolder) -> Unit)? = null
    var onDelete: ((Int) -> Unit)? = null
    var onVisibilityToggle: ((Int) -> Unit)? = null
    var onIntensityUpdated: ((Int, Float) -> Unit)? = null
    var onBlendModeUpdated: ((Int, NamedBlendMode) -> Unit)? = null

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
                        onIntensityUpdated?.invoke(adapterPosition, value)
                }
            )
            binding.btnChooseBlendMode.setOnClickListener {
                val dialog = ChooseBlendModeDialog()
                dialog.onSelected = {
                    onBlendModeUpdated?.invoke(adapterPosition, it)
                }
                dialog.show(fragmentManager, "ChooseBlendModeDialog")
            }
            binding.btnVisibility.setOnClickListener {
                onVisibilityToggle?.invoke(adapterPosition)
            }
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
        }

        fun bind(value: EffectLayer) {
            binding.tvName.text = value.name
            binding.sldIntensity.value = value.layerIntensity
            binding.btnChooseBlendMode.text = value.layerBlendMode.name
            binding.btnVisibility.setIconResource(
                if (value.layerVisible) R.drawable.ic_visible else R.drawable.ic_invisible
            )
        }

    }

}
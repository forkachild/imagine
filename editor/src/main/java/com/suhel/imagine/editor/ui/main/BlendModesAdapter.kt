package com.suhel.imagine.editor.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.suhel.imagine.editor.R
import com.suhel.imagine.editor.databinding.ItemBlendModeBinding
import com.suhel.imagine.editor.databinding.ItemBlendModeSectionHeaderBinding
import com.suhel.imagine.editor.model.NamedBlendMode
import com.suhel.imagine.editor.model.sectionedBlendModes
import com.suhel.imagine.editor.ui.common.StaticSectionedAdapter

class BlendModesAdapter(
    private val onSelected: (NamedBlendMode) -> Unit,
) : StaticSectionedAdapter<
        NamedBlendMode,
        BlendModesAdapter.SectionHeaderViewHolder,
        BlendModesAdapter.BlendModeViewHolder,
        >(sectionedBlendModes) {

    override fun onCreateSectionViewHolder(
        parent: ViewGroup
    ): SectionHeaderViewHolder = SectionHeaderViewHolder(parent)

    override fun onBindSectionViewHolder(holder: SectionHeaderViewHolder, title: String) {
        holder.bind(title)
    }

    override fun onCreateDatumViewHolder(
        parent: ViewGroup
    ): BlendModeViewHolder = BlendModeViewHolder(parent)

    override fun onBindDataViewHolder(holder: BlendModeViewHolder, datum: NamedBlendMode) {
        holder.bind(datum)
    }

    inner class SectionHeaderViewHolder(parent: ViewGroup) : ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blend_mode_section_header, parent, false)
    ) {

        private val binding = ItemBlendModeSectionHeaderBinding.bind(itemView)

        fun bind(title: String) {
            binding.tvSectionTitle.text = title
        }

    }

    inner class BlendModeViewHolder(parent: ViewGroup) : ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blend_mode, parent, false)
    ) {

        private val binding = ItemBlendModeBinding.bind(itemView)

        init {
            binding.root.setOnClickListener {
                getDatumForAdapterPositionOrNull(adapterPosition)?.let { namedBlendMode ->
                    onSelected(namedBlendMode)
                }
            }
        }

        fun bind(blendMode: NamedBlendMode) {
            binding.tvBlendModeName.text = blendMode.name
        }

    }


}
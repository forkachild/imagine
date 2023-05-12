package com.suhel.imagine.editor.ui.common

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.suhel.imagine.editor.ui.common.model.SectionedData

abstract class StaticSectionedAdapter<T, SVH : ViewHolder, DVH : ViewHolder>(
    sections: List<SectionedData<T>>
) : Adapter<ViewHolder>() {

    private val items = flatMapToItems(sections)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SECTION -> onCreateSectionViewHolder(parent)
            VIEW_TYPE_DATUM -> onCreateDatumViewHolder(parent)
            else -> throw IllegalArgumentException("onCreateViewHolder: Illegal viewType")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_SECTION -> onBindSectionViewHolder(
                holder as SVH,
                (items[position] as Item.Section).title,
            )

            VIEW_TYPE_DATUM -> onBindDataViewHolder(
                holder as DVH,
                (items[position] as Item.Datum).data,
            )

            else -> throw IllegalArgumentException("onBindViewHolder: Illegal viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].viewType

    abstract fun onCreateSectionViewHolder(parent: ViewGroup): SVH

    abstract fun onBindSectionViewHolder(holder: SVH, title: String)

    abstract fun onCreateDatumViewHolder(parent: ViewGroup): DVH

    abstract fun onBindDataViewHolder(holder: DVH, datum: T)

    protected fun getDatumForAdapterPositionOrNull(position: Int): T? =
        (items.getOrNull(position) as? Item.Datum<T>)?.data

    private fun flatMapToItems(sections: List<SectionedData<T>>): List<Item<T>> {
        val sectionCount = sections.size
        val dataCount = sections.fold(0) { acc, section -> acc + section.items.size }
        val totalCount = sectionCount + dataCount

        return sections.fold(ArrayList(totalCount)) { acc, section ->
            acc.add(Item.Section(section.title))
            section.items.forEach { acc.add(Item.Datum(it)) }
            acc
        }
    }

    sealed class Item<out T>(val viewType: Int) {
        data class Section(val title: String) : Item<Nothing>(VIEW_TYPE_SECTION)
        data class Datum<T>(val data: T) : Item<T>(VIEW_TYPE_DATUM)
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 1
        private const val VIEW_TYPE_DATUM = 2
    }

}
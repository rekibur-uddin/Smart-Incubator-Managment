package com.app.smartincubatormanagment.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.smartincubatormanagment.data.model.Part
import com.app.smartincubatormanagment.databinding.ItemPartBinding
import com.bumptech.glide.Glide
import com.app.smartincubatormanagment.R
import com.app.smartincubatormanagment.data.model.BuiltIncubator
import com.app.smartincubatormanagment.databinding.ItemBuiltIncubatorBinding
import com.app.smartincubatormanagment.ui.activity.AddPartActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BuiltIncubatorAdapter(
    private val items: List<BuiltIncubator>,
    private val onSelect: (BuiltIncubator, Boolean) -> Unit,
    private val showCheckbox: Boolean
) : RecyclerView.Adapter<BuiltIncubatorAdapter.ViewHolder>() {

    private val selectedIds = mutableSetOf<String>()

    inner class ViewHolder(val binding: ItemBuiltIncubatorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BuiltIncubator) {
            binding.incubatorName.text = item.capacity
            binding.incubatorCost.text = "Total Built Cost: ₹${item.totalCost.toString()}"
            Glide.with(binding.image.context)
                .load(item.imageLink)
                .into(binding.image)

            binding.checkSelect.visibility =
                if (showCheckbox) View.VISIBLE else View.GONE

            binding.incubatorName.text = item.capacity
            binding.builtDate.text = "Built Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.timestamp))}"


            val partsList = item.parts
            val totalItems = partsList.size
            val totalQuantity = partsList.sumOf { part ->
                part["quantity"]?.toString()?.toIntOrNull() ?: 0
            }
            binding.usedTotalQuantity.text = "Used items: $totalItems & Qnty: $totalQuantity"


            Glide.with(binding.image.context).load(item.imageLink).into(binding.image)


            binding.checkSelect.setOnCheckedChangeListener(null)
            binding.checkSelect.isChecked = selectedIds.contains(item.id)

            binding.checkSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                onSelect(item, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBuiltIncubatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
}

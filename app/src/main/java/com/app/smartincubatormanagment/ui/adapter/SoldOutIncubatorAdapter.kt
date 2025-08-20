package com.app.smartincubatormanagment.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.smartincubatormanagment.data.model.SoldOutIncubator
import com.app.smartincubatormanagment.databinding.ItemSoldoutIncubatorBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SoldOutIncubatorAdapter(
    private val items: List<SoldOutIncubator>
) : RecyclerView.Adapter<SoldOutIncubatorAdapter.SoldOutViewHolder>() {
    private val selectedIds = mutableSetOf<String>()


    inner class SoldOutViewHolder(val binding: ItemSoldoutIncubatorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoldOutViewHolder {
        val binding = ItemSoldoutIncubatorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SoldOutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoldOutViewHolder, position: Int) {
        val item = items[position]


        with(holder.binding) {


            Glide.with(image.context)
                .load(item.imageLink)
                .into(image)

            incubatorName.text = item.capacityName
            incubatorBuiltCost.text = "Total Built Cost: ₹${item.incubatorBuildCost}"
            transportCost.text = "Transport Cost: ₹${item.transportCost}"
            sellingPrice.text = "Selling Price: ₹${item.sellPrice}"
            // Calculate profit
            val calculatedProfit = item.sellPrice - (item.incubatorBuildCost + item.transportCost)
            profit.text = "Profit = ${item.sellPrice} - (${item.incubatorBuildCost} + ${item.transportCost}) = ₹$calculatedProfit"

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val soldDateText = item.soldDate
                ?.toDate()
                ?.let { sdf.format(it) }
                ?: "N/A"
            sellingDate.text = "Selling Date: $soldDateText"

            customerDetails.text = "Customer Details: ${item.customerDetails}"
        }
    }

    override fun getItemCount(): Int = items.size
}

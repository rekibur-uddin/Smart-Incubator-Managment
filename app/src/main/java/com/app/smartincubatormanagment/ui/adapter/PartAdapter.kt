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
import com.app.smartincubatormanagment.ui.activity.AddPartActivity


class PartAdapter(
    private val items: List<Part>,
    private val hideEditIcon: Boolean = false, // default false so other activities work as before
    private val fixedCardColorRes: Int? = null // optional color resource to keep fixed
) : RecyclerView.Adapter<PartAdapter.PartViewHolder>() {

    inner class PartViewHolder(val binding: ItemPartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val binding = ItemPartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val part = items[position]
        holder.binding.partNmae.text = part.name
        holder.binding.partPrice.text = "Price: ₹${part.price}"
        holder.binding.partQuantity.text = "Qty: ${part.quantity}"
        holder.binding.partDescription.text = part.description

        Glide.with(holder.itemView.context)
            .load(part.image)
            .placeholder(R.drawable.logo)
            .into(holder.binding.imagePart)

        // Hide edit icon if requested
        if (hideEditIcon) {
            holder.binding.editItem.visibility = View.GONE
        } else {
            holder.binding.editItem.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, AddPartActivity::class.java).apply {
                    putExtra("isEdit", true)
                    putExtra("partId", part.id)
                    putExtra("name", part.name)
                    putExtra("image", part.image)
                    putExtra("price", part.price)
                    putExtra("quantity", part.quantity)
                    putExtra("description", part.description)
                }
                context.startActivity(intent)
            }
        }

        // Apply fixed card color or conditional color
        val context = holder.itemView.context
        if (fixedCardColorRes != null) {
            holder.binding.cardPart.setCardBackgroundColor(ContextCompat.getColor(context, fixedCardColorRes))
        } else {
            val qty = part.quantity.toIntOrNull() ?: 0
            when {
                qty < 5 -> holder.binding.cardPart.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_Indicator_Color))
                qty < 10 -> holder.binding.cardPart.setCardBackgroundColor(ContextCompat.getColor(context, R.color.yellow_Indicator_Color))
                else -> holder.binding.cardPart.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green_Indicator_Color))
            }
        }
    }


    override fun getItemCount(): Int = items.size
}

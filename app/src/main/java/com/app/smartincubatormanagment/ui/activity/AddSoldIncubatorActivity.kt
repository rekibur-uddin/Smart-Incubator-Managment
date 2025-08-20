package com.app.smartincubatormanagment.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smartincubatormanagment.data.model.BuiltIncubator
import com.app.smartincubatormanagment.databinding.ActivityAddSoldIncubatorBinding
import com.app.smartincubatormanagment.ui.adapter.BuiltIncubatorAdapter
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddSoldIncubatorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSoldIncubatorBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val incubators = mutableListOf<BuiltIncubator>()
    private lateinit var adapter: BuiltIncubatorAdapter
    private var selectedIncubator: BuiltIncubator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSoldIncubatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        binding.toolBar.setNavigationOnClickListener { finish() }

        // Setup Adapter
        adapter = BuiltIncubatorAdapter(incubators, { item, isChecked ->
            if (isChecked) {
                selectedIncubator = item
                updateCostAndProfit()
            } else selectedIncubator = null
        }, showCheckbox = true)

        binding.rvSold.layoutManager = LinearLayoutManager(this)
        binding.rvSold.adapter = adapter

        // Listen for user input to update cost/profit live
        binding.etTransportCost.addTextChangedListener(inputWatcher)
        binding.etSellPrice.addTextChangedListener(inputWatcher)

        loadBuiltIncubators()

        binding.btnUpload.setOnClickListener {
            if (selectedIncubator == null) {
                Toast.makeText(this, "Select an incubator first!", Toast.LENGTH_SHORT).show()
            } else {
                val incubator = selectedIncubator!!
                val transportCost = binding.etTransportCost.text.toString().toIntOrNull() ?: 0
                val sellPrice = binding.etSellPrice.text.toString().toIntOrNull() ?: 0
                val incubatorCost = incubator.totalCost.toInt()
                val profit = sellPrice - (incubatorCost + transportCost)

                // Confirmation Dialog
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirm Upload")
                    .setMessage(
                        "Are you sure you want to upload this sold incubator?\n\n" +
                                "Incubator: ${incubator.capacity}\n" +
                                "Cost: ₹$incubatorCost\n" +
                                "Transport: ₹$transportCost\n" +
                                "Sell Price: ₹$sellPrice\n" +
                                "Profit: ₹$profit"
                    )
                    .setPositiveButton("Yes") { _, _ ->
                        calculateAndUpload()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

    }

    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateCostAndProfit()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updateCostAndProfit() {
        val incubator = selectedIncubator ?: return
        val transportCost = binding.etTransportCost.text.toString().toIntOrNull() ?: 0
        val sellPrice = binding.etSellPrice.text.toString().toIntOrNull() ?: 0
        val incubatorCost = incubator.totalCost.toInt()
        val profit = sellPrice - (incubatorCost + transportCost)

        binding.incubatorCostText.text = "Incubator Cost: ₹$incubatorCost"
        binding.profitText.text = "Profit = $sellPrice - ($incubatorCost + $transportCost) = ₹$profit"
    }

    private fun loadBuiltIncubators() {
        firestore.collection("builtIncubators")
            .get()
            .addOnSuccessListener { snap ->
                incubators.clear()
                for (doc in snap) {
                    val incubator = doc.toObject(BuiltIncubator::class.java).copy(id = doc.id)
                    incubators.add(incubator)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun calculateAndUpload() {
        val progress = ProgressDialog(this)
        progress.setMessage("Uploading...")
        progress.setCancelable(false)
        progress.show()

        val transportCost = binding.etTransportCost.text.toString().toIntOrNull() ?: 0
        val sellPrice = binding.etSellPrice.text.toString().toIntOrNull() ?: 0
        val customerDetails = binding.etCustomer.text.toString()
        val incubator = selectedIncubator!!

        val incubatorCost = incubator.totalCost.toInt()
        val profit = sellPrice - (incubatorCost + transportCost)

        val data = hashMapOf(
            "incubatorId" to incubator.id,
            "capacityName" to incubator.capacity,
            "imageLink" to incubator.imageLink,
            "sellPrice" to sellPrice,
            "transportCost" to transportCost,
            "incubatorBuildCost" to incubatorCost,
            "profit" to profit,
            "customerDetails" to customerDetails,
            "soldDate" to FieldValue.serverTimestamp()
        )

        firestore.collection("soldIncubators")
            .add(data)
            .addOnSuccessListener {
                progress.dismiss()
                Toast.makeText(this, "Uploaded successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progress.dismiss()
                Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
            }
    }
}

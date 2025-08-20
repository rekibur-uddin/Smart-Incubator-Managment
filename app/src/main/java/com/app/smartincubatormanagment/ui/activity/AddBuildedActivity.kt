package com.app.smartincubatormanagment.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smartincubatormanagment.R
import com.app.smartincubatormanagment.data.model.Part
import com.app.smartincubatormanagment.databinding.ActivityAddBuildedBinding
import com.app.smartincubatormanagment.ui.adapter.PartAdapter
import com.google.firebase.firestore.FirebaseFirestore



class AddBuildedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBuildedBinding
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val partList = mutableListOf<Part>()
    private lateinit var adapter: PartAdapter


    private val capacityImages: Map<Int, String> = mapOf(
        600 to "https://image.made-in-china.com/202f0j00jPTqEUgtRQkO/Smart-Incubator-with-Temperature-Controller.webp",
        1200 to "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTZFl8H-l1y7g7DCa8OQQPQupL4D-4NAiR_4g&s",
        2400 to "https://5.imimg.com/data5/SELLER/Default/2025/1/482551950/XU/OH/HO/108054826/4000-hour-smart-eggs-incubator.jpg"
    )



    // Predefined part requirements for capacities
    private val capacityParts = mapOf(
        600 to listOf(
            "555 IC" to 2,
            "7805 IC" to 1,
            "PIC 16 F 877A-I/P" to 2,
            "PIC 16 F 73-I/SP" to 1
        ),
        1200 to listOf(
            "555 IC" to 2,
            "7805 IC" to 2,
            "PIC 16 F 877A-I/P" to 1,
            "PIC 16 F 73-I/SP" to 1
        ),
        2400 to listOf(
            "555 IC" to 1,
            "7805 IC" to 1,
            "PIC 16 F 877A-I/P" to 2,
            "PIC 16 F 73-I/SP" to 1
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBuildedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        binding.toolBar.setNavigationOnClickListener { finish() }

        setupSpinner()
        setupRecycler()

        binding.btnUpload.setOnClickListener {
            showConfirmDialog()
        }
    }

    private fun setupSpinner() {
        // Display names for the spinner
        val capacitiesDisplay = listOf(
            "600 Capacity Incubator",
            "1200 Capacity Incubator",
            "2400 Capacity Incubator"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            capacitiesDisplay
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCapacity.adapter = spinnerAdapter

        binding.spinnerCapacity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // Extract just the number before the first space
                val selectedText = capacitiesDisplay[position]
                val capacity = selectedText.substringBefore(" ").toIntOrNull() ?: 0
                fetchPartsForCapacity(capacity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun setupRecycler() {
        adapter = PartAdapter(
            partList,
            hideEditIcon = true,
            fixedCardColorRes = R.color.green_Indicator_Color // or any fixed color you want
        )
        binding.rvParts.layoutManager = LinearLayoutManager(this)
        binding.rvParts.adapter = adapter
    }


    private fun fetchPartsForCapacity(capacity: Int) {
        partList.clear()
        val requirements = capacityParts[capacity] ?: emptyList()

        requirements.forEach { (name, requiredQty) ->
            firestore.collection("incubatorparts")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        val part = doc.toObject(Part::class.java).copy(id = doc.id)
                        part.quantity = requiredQty.toString() // Override quantity for build
                        partList.add(part)
                    }
                    adapter.notifyDataSetChanged()
                    calculateTotals()
                }
        }
    }

    private fun calculateTotals() {
        val totalParts = partList.size
        val totalQuantity = partList.sumOf { it.quantity.toIntOrNull() ?: 0 }
        val totalPrice = partList.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 0)
        }

        binding.etLaborCost.addTextChangedListener(inputWatcher)
        val laborCost = binding.etLaborCost.text.toString().toIntOrNull() ?: 0
        val totalIncubatorCost = totalPrice + laborCost

        binding.tvTotalParts.text = "Total Parts: $totalParts"
        binding.tvTotalQuantity.text = "Total Quantity: $totalQuantity"
        binding.tvTotalPrice.text = "Total Parts Cost: ₹${totalPrice.toInt()}"
        binding.totalIncubatorCost.text = "Total Incubator Cost: ₹$totalPrice + ₹$laborCost = ₹$totalIncubatorCost"
    }

    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            calculateTotals() // update whenever input changes
        }

        override fun afterTextChanged(s: Editable?) {}
    }



    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Upload Built Incubator")
            .setMessage("Are you sure you want to upload this build?")
            .setPositiveButton("Yes") { _, _ ->
                uploadBuildToFirestore()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun uploadBuildToFirestore() {
        val capacity = binding.spinnerCapacity.selectedItem.toString()
        val laborCostStr = binding.etLaborCost.text.toString().trim()
        val capacityValue = capacity.substringBefore(" ").trim().toIntOrNull() ?: 0
        val imageLink: String = capacityImages[capacityValue] ?: ""

        val laborCost = laborCostStr.toDoubleOrNull() ?: 0.0
        val totalPartsCost = partList.sumOf {
            (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 0)
        }
        val totalCost = totalPartsCost + laborCost

        val progress = ProgressDialog(this)
        progress.setMessage("Updating...")
        progress.setCancelable(false)
        progress.show()

        val buildData = hashMapOf(
            "capacity" to capacity,
            "parts" to partList.map {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "quantity" to it.quantity,
                    "price" to it.price
                )
            },
            "imageLink" to imageLink,
            "laborCost" to laborCost,
            "totalParts" to partList.size,
            "totalQuantity" to partList.sumOf { it.quantity.toIntOrNull() ?: 0 },
            "totalPartsCost" to totalPartsCost,
            "totalCost" to totalCost,
            "timestamp" to System.currentTimeMillis()
        )

        // First add the built incubator
        firestore.collection("builtIncubators")
            .add(buildData)
            .addOnSuccessListener {

                // Now update part stock manually (since quantity is String)
                partList.forEach { part ->
                    val usedQty = part.quantity.toIntOrNull() ?: 0
                    val partDocRef = firestore.collection("incubatorparts").document(part.id)

                    partDocRef.get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val currentQtyStr = doc.getString("quantity") ?: "0"
                            val currentQty = currentQtyStr.toIntOrNull() ?: 0
                            val newQty = (currentQty - usedQty).coerceAtLeast(0) // avoid negative

                            partDocRef.update("quantity", newQty.toString())
                        }
                    }
                }

                progress.dismiss()
                Toast.makeText(this, "Build Uploaded & Stock Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progress.dismiss()
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}


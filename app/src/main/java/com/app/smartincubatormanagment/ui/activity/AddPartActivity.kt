package com.app.smartincubatormanagment.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.smartincubatormanagment.databinding.ActivityAddPartBinding
import com.google.firebase.firestore.FirebaseFirestore



class AddPartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPartBinding
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private var isEdit = false
    private var partId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolBar)
        binding.toolBar.setNavigationOnClickListener { finish() }

        // Check intent data
        isEdit = intent.getBooleanExtra("isEdit", false)
        if (isEdit) {
            partId = intent.getStringExtra("partId")
            binding.etPartName.setText(intent.getStringExtra("name"))
            binding.etPartImglink.setText(intent.getStringExtra("image"))
            binding.etPartPrice.setText(intent.getStringExtra("price"))
            binding.etQuantity.setText(intent.getStringExtra("quantity"))
            binding.etDescription.setText(intent.getStringExtra("description"))

            binding.btnUpload.text = "Update"
        }

        binding.btnUpload.setOnClickListener {
            if (!areFieldsValid()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEdit) {
                updatePartInFirestore()
            } else {
                showConfirmDialog()
            }
        }
    }

    private fun areFieldsValid(): Boolean {
        return binding.etPartName.text.toString().trim().isNotEmpty() &&
                binding.etPartImglink.text.toString().trim().isNotEmpty() &&
                binding.etPartPrice.text.toString().trim().isNotEmpty() &&
                binding.etQuantity.text.toString().trim().isNotEmpty()
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add Incubator Parts")
            .setMessage("Are you sure you want to upload this incubator part to the database?")
            .setPositiveButton("Yes") { _, _ -> uploadPartToFirestore() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun uploadPartToFirestore() {
        val progress = ProgressDialog(this)
        progress.setMessage("Uploading...")
        progress.setCancelable(false)
        progress.show()

        val partData = getPartData()

        firestore.collection("incubatorparts")
            .add(partData)
            .addOnSuccessListener {
                progress.dismiss()
                Toast.makeText(this, "Upload Success", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progress.dismiss()
                Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePartInFirestore() {
        val progress = ProgressDialog(this)
        progress.setMessage("Updating...")
        progress.setCancelable(false)
        progress.show()

        val partData = getPartData()

        partId?.let { id ->
            firestore.collection("incubatorparts").document(id)
                .update(partData as Map<String, Any>)
                .addOnSuccessListener {
                    progress.dismiss()
                    Toast.makeText(this, "Update Success", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progress.dismiss()
                    Toast.makeText(this, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getPartData(): HashMap<String, Any> {
        val partName = binding.etPartName.text.toString().trim()
        val partImgLink = binding.etPartImglink.text.toString().trim()
        val partPrice = binding.etPartPrice.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        return hashMapOf(
            "name" to partName,
            "image" to partImgLink,
            "price" to partPrice,
            "quantity" to quantity,
            "description" to description,
            "timestamp" to System.currentTimeMillis()
        )
    }
}


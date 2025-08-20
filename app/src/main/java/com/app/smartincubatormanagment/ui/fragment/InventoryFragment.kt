package com.app.smartincubatormanagment.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smartincubatormanagment.data.model.Part
import com.app.smartincubatormanagment.databinding.FragmentInventoryBinding
import com.app.smartincubatormanagment.ui.adapter.PartAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val partList = mutableListOf<Part>()
    private lateinit var adapter: PartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PartAdapter(partList)
        binding.recyclerView.adapter = adapter

        fetchParts()
    }

   /* private fun fetchParts() {
        firestore.collection("incubatorparts")
            .orderBy("quantity", Query.Direction.ASCENDING) // Lowest first
            .get()
            .addOnSuccessListener { result ->
                partList.clear()
                for (doc in result) {
                    val part = doc.toObject(Part::class.java).copy(id = doc.id)
                    partList.add(part)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load parts", Toast.LENGTH_SHORT).show()
            }
    }*/

    private fun fetchParts() {
        firestore.collection("incubatorparts")
            .get()
            .addOnSuccessListener { result ->
                partList.clear()
                for (doc in result) {
                    val part = doc.toObject(Part::class.java).copy(id = doc.id)
                    partList.add(part)
                }

                // Sort by quantity as Int (lowest first)
                partList.sortBy { it.quantity.toIntOrNull() ?: 0 }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load parts", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

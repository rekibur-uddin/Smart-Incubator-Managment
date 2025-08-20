package com.app.smartincubatormanagment.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.smartincubatormanagment.R
import com.app.smartincubatormanagment.databinding.FragmentAddItemBinding
import com.app.smartincubatormanagment.ui.activity.AddBuildedActivity
import com.app.smartincubatormanagment.ui.activity.AddPartActivity
import com.app.smartincubatormanagment.ui.activity.AddSoldIncubatorActivity

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button Add Incubator
        binding.btnAddIncubatorpart.setOnClickListener {
            val intent = Intent(context, AddPartActivity::class.java)
            intent.putExtra("isEdit", false)
            startActivity(intent)
        }

        // Button Add Built Incubator
        binding.btnAddIncubator.setOnClickListener {
            val intent = Intent(context, AddBuildedActivity::class.java)
            startActivity(intent)
        }

        // Button Add Sold Incubator
        binding.btnSoldIncubator.setOnClickListener {
            val intent = Intent(context, AddSoldIncubatorActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
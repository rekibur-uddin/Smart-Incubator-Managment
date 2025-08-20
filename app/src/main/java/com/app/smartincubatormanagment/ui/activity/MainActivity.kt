package com.app.smartincubatormanagment.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.smartincubatormanagment.R
import com.app.smartincubatormanagment.databinding.ActivityMainBinding
import com.app.smartincubatormanagment.ui.fragment.AddItemFragment
import com.app.smartincubatormanagment.ui.fragment.AnalyticsFragment
import com.app.smartincubatormanagment.ui.fragment.InventoryFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadFragment(InventoryFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { index ->
            when (index) {
                0 -> loadFragment(InventoryFragment())
                1 -> loadFragment(AddItemFragment())
                2 -> loadFragment(AnalyticsFragment())
            }
        }



    }



    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}
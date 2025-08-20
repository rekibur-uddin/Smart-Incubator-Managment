/*
package com.app.smartincubatormanagment.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smartincubatormanagment.data.model.BuiltIncubator
import com.app.smartincubatormanagment.data.model.SoldOutIncubator
import com.app.smartincubatormanagment.databinding.FragmentAnalyticsBinding
import com.app.smartincubatormanagment.ui.adapter.BuiltIncubatorAdapter
import com.app.smartincubatormanagment.ui.adapter.SoldOutIncubatorAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val incubators = mutableListOf<BuiltIncubator>()
    private lateinit var adapter: BuiltIncubatorAdapter
    private val soldIncubators = mutableListOf<SoldOutIncubator>()
    private lateinit var soldOutAdapter: SoldOutIncubatorAdapter

    private var startDate: Date? = null
    private var endDate: Date? = null
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)

        // Init adapters
        adapter = BuiltIncubatorAdapter(incubators, { _, _ -> }, showCheckbox = false)
        soldOutAdapter = SoldOutIncubatorAdapter(soldIncubators)

        // Setup RecyclerViews
        binding.rcAvailableIncubators.layoutManager = LinearLayoutManager(requireContext())
        binding.rcAvailableIncubators.adapter = adapter
        binding.rcSoldOutIncubators.layoutManager = LinearLayoutManager(requireContext())
        binding.rcSoldOutIncubators.adapter = soldOutAdapter

        // Load data
        loadBuiltIncubators()
        setupBusinessReport()
        setupBarChart()

        return binding.root
    }


    private fun setupBarChart() {
        binding.sellsBarChart.apply {
            description.isEnabled = false
            setFitBars(true)
            animateY(1000)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun loadBarChartData(year: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1)
        val startYear = calendar.time
        calendar.set(year, Calendar.DECEMBER, 31)
        val endYear = calendar.time

        firestore.collection("soldIncubators")
            .whereGreaterThanOrEqualTo("soldDate", startYear)
            .whereLessThanOrEqualTo("soldDate", endYear)
            .get()
            .addOnSuccessListener { snap ->
                val monthlySales = FloatArray(12) { 0f }
                val monthlyCounts = IntArray(12) { 0 }

                for (doc in snap) {
                    val soldIncubator = doc.toObject(SoldOutIncubator::class.java)
                    val soldDate = soldIncubator.soldDate?.toDate()
                    if (soldDate != null) {
                        calendar.time = soldDate
                        val month = calendar.get(Calendar.MONTH)
                        monthlySales[month] += soldIncubator.sellPrice.toFloat()
                        monthlyCounts[month]++
                    }
                }

                val entries = mutableListOf<BarEntry>()
                for (i in 0 until 12) {
                    entries.add(BarEntry(i.toFloat(), monthlySales[i]))
                }

                val dataSet = BarDataSet(entries, "Monthly Sales")
                dataSet.colors = listOf(
                    ColorTemplate.rgb("#FF6F61"), // Jan
                    ColorTemplate.rgb("#6B7280"), // Feb
                    ColorTemplate.rgb("#4CAF50"), // Mar
                    ColorTemplate.rgb("#FFCA28"), // Apr
                    ColorTemplate.rgb("#2196F3"), // May
                    ColorTemplate.rgb("#F44336"), // Jun
                    ColorTemplate.rgb("#9C27B0"), // Jul
                    ColorTemplate.rgb("#FFEB3B"), // Aug
                    ColorTemplate.rgb("#3F51B5"), // Sep
                    ColorTemplate.rgb("#E91E63"), // Oct
                    ColorTemplate.rgb("#00BCD4"), // Nov
                    ColorTemplate.rgb("#8BC34A")  // Dec
                )
                dataSet.setDrawValues(true)

                val barData = BarData(dataSet)
                barData.barWidth = 0.4f
                binding.sellsBarChart.data = barData
                binding.sellsBarChart.invalidate()
            }
    }


    //
    private fun setupBusinessReport() {
        // Setup date filter dropdown
        val filterOptions = arrayOf("All Time", "Select Month", "Select Year")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateFilterDropdown.adapter = adapter

        binding.dateFilterDropdown.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                binding.selectedDateText.visibility = View.GONE
                when (position) {
                    0 -> { // All Time
                        startDate = null
                        endDate = null
                        loadSoldOutIncubators()
                    }
                    1 -> { // Select Month
                        showMonthPicker { calendar ->
                            binding.selectedDateText.visibility = View.VISIBLE
                            binding.selectedDateText.text = "Report On: ${monthYearFormat.format(calendar.time)}"
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            startDate = calendar.time
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                            endDate = calendar.time
                            loadSoldOutIncubators()
                        }
                    }
                    2 -> { // Select Year
                        showYearPicker { calendar ->
                            binding.selectedDateText.visibility = View.VISIBLE
                            binding.selectedDateText.text = "Report On: ${yearFormat.format(calendar.time)}"
                            calendar.set(Calendar.MONTH, Calendar.JANUARY)
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            startDate = calendar.time
                            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                            calendar.set(Calendar.DAY_OF_MONTH, 31)
                            endDate = calendar.time
                            loadSoldOutIncubators()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // Initial load
        loadSoldOutIncubators()
    }

    private fun showMonthPicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, _ ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, 1)
                onDateSelected(selectedCalendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        )
        datePicker.datePicker.findViewById<View>(
            datePicker.context.resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE // Hide day picker
        datePicker.show()
    }

    private fun showYearPicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, _, _ ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, Calendar.JANUARY, 1)
                onDateSelected(selectedCalendar)
            },
            calendar.get(Calendar.YEAR),
            Calendar.JANUARY,
            1
        )
        datePicker.datePicker.findViewById<View>(
            datePicker.context.resources.getIdentifier("month", "id", "android")
        )?.visibility = View.GONE // Hide month picker
        datePicker.datePicker.findViewById<View>(
            datePicker.context.resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE // Hide day picker
        datePicker.show()
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

    private fun loadSoldOutIncubators() {
        var query = firestore.collection("soldIncubators")
            .orderBy("soldDate", Query.Direction.DESCENDING)

        if (startDate != null && endDate != null) {
            query = query.whereGreaterThanOrEqualTo("soldDate", startDate!!)
                .whereLessThanOrEqualTo("soldDate", endDate!!)
        }

        query.get()
            .addOnSuccessListener { snap ->
                soldIncubators.clear()
                var totalSold = 0
                var totalSellAmount = 0.0
                var totalCost = 0.0
                var totalProfit = 0.0

                for (doc in snap) {
                    val soldIncubator = doc.toObject(SoldOutIncubator::class.java).copy(id = doc.id)
                    soldIncubators.add(soldIncubator)
                    totalSold++
                    totalSellAmount += soldIncubator.sellPrice
                    totalCost += soldIncubator.incubatorBuildCost + soldIncubator.transportCost
                    totalProfit += soldIncubator.profit
                }

                // Update UI
                binding.totalSoldIncubators.text = "Total Incubators Sold: $totalSold"
                binding.totalSellAmount.text = "Total Sell: ₹${String.format("%.2f", totalSellAmount)}"
                binding.totalCostAmount.text = "Total Cost: ₹${String.format("%.2f", totalCost)}"
                binding.totalProfitAmount.text = "Total Profit: ₹${String.format("%.2f", totalProfit)}"

                soldOutAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}*/


package com.app.smartincubatormanagment.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smartincubatormanagment.data.model.BuiltIncubator
import com.app.smartincubatormanagment.data.model.SoldOutIncubator
import com.app.smartincubatormanagment.databinding.FragmentAnalyticsBinding
import com.app.smartincubatormanagment.ui.adapter.BuiltIncubatorAdapter
import com.app.smartincubatormanagment.ui.adapter.SoldOutIncubatorAdapter
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val incubators = mutableListOf<BuiltIncubator>()
    private lateinit var adapter: BuiltIncubatorAdapter
    private val soldIncubators = mutableListOf<SoldOutIncubator>()
    private lateinit var soldOutAdapter: SoldOutIncubatorAdapter

    private var startDate: Date? = null
    private var endDate: Date? = null
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)

        // Init adapters
        adapter = BuiltIncubatorAdapter(incubators, { _, _ -> }, showCheckbox = false)
        soldOutAdapter = SoldOutIncubatorAdapter(soldIncubators)

        // Setup RecyclerViews
        binding.rcAvailableIncubators.layoutManager = LinearLayoutManager(requireContext())
        binding.rcAvailableIncubators.adapter = adapter
        binding.rcSoldOutIncubators.layoutManager = LinearLayoutManager(requireContext())
        binding.rcSoldOutIncubators.adapter = soldOutAdapter

        // Load data
        loadBuiltIncubators()
        setupBusinessReport()
        setupBarChart()

        return binding.root
    }

    private fun setupBusinessReport() {
        // Setup date filter dropdown
        val filterOptions = arrayOf("All Time", "Select Month", "Select Year")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateFilterDropdown.adapter = adapter

        binding.dateFilterDropdown.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                binding.selectedDateText.visibility = View.GONE
                when (position) {
                    0 -> { // All Time
                        startDate = null
                        endDate = null
                        loadSoldOutIncubators()
                    }
                    1 -> { // Select Month
                        showMonthPicker { calendar ->
                            binding.selectedDateText.visibility = View.VISIBLE
                            binding.selectedDateText.text = "Report On: ${monthYearFormat.format(calendar.time)}"
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            startDate = calendar.time
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                            endDate = calendar.time
                            loadSoldOutIncubators()
                        }
                    }
                    2 -> { // Select Year
                        showYearPicker { calendar ->
                            binding.selectedDateText.visibility = View.VISIBLE
                            binding.selectedDateText.text = "Report On: ${yearFormat.format(calendar.time)}"
                            calendar.set(Calendar.MONTH, Calendar.JANUARY)
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            startDate = calendar.time
                            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                            calendar.set(Calendar.DAY_OF_MONTH, 31)
                            endDate = calendar.time
                            loadSoldOutIncubators()
                            loadBarChartData(calendar.get(Calendar.YEAR))
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // Initial load for current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.titleSellsChart.text = "Sells of $currentYear"
        loadBarChartData(currentYear)
        loadSoldOutIncubators()
    }

    private fun showMonthPicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, _ ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, 1)
                onDateSelected(selectedCalendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        )
        datePicker.datePicker.findViewById<View>(
            datePicker.context.resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE // Hide day picker
        datePicker.show()
    }

    private fun showYearPicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, _, _ ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, Calendar.JANUARY, 1)
                onDateSelected(selectedCalendar)
                binding.titleSellsChart.text = "Sells of $year"
                loadBarChartData(year)
            },
            calendar.get(Calendar.YEAR),
            Calendar.JANUARY,
            1
        )
        datePicker.datePicker.findViewById<View>(
            datePicker.context.resources.getIdentifier("month", "id", "android")
        )?.visibility = View.GONE // Hide month picker
        datePicker.datePicker.findViewById<View>(
            datePicker.context.resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE // Hide day picker
        datePicker.show()
    }

    private fun setupBarChart() {
        binding.sellsBarChart.apply {
            description.isEnabled = false
            setFitBars(true)
            animateY(1000)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun loadBarChartData(year: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1)
        val startYear = calendar.time
        calendar.set(year, Calendar.DECEMBER, 31)
        val endYear = calendar.time

        firestore.collection("soldIncubators")
            .whereGreaterThanOrEqualTo("soldDate", startYear)
            .whereLessThanOrEqualTo("soldDate", endYear)
            .get()
            .addOnSuccessListener { snap ->
                val monthlySales = FloatArray(12) { 0f }
                val monthlyCounts = IntArray(12) { 0 }

                for (doc in snap) {
                    val soldIncubator = doc.toObject(SoldOutIncubator::class.java)
                    val soldDate = soldIncubator.soldDate?.toDate()
                    if (soldDate != null) {
                        calendar.time = soldDate
                        val month = calendar.get(Calendar.MONTH)
                        monthlySales[month] += soldIncubator.sellPrice.toFloat()
                        monthlyCounts[month]++
                    }
                }

                val entries = mutableListOf<BarEntry>()
                for (i in 0 until 12) {
                    entries.add(BarEntry(i.toFloat(), monthlySales[i]))
                }

                val dataSet = BarDataSet(entries, "Monthly Sales")
                dataSet.colors = listOf(
                    ColorTemplate.rgb("#FF6F61"), // Jan
                    ColorTemplate.rgb("#6B7280"), // Feb
                    ColorTemplate.rgb("#4CAF50"), // Mar
                    ColorTemplate.rgb("#FFCA28"), // Apr
                    ColorTemplate.rgb("#2196F3"), // May
                    ColorTemplate.rgb("#F44336"), // Jun
                    ColorTemplate.rgb("#9C27B0"), // Jul
                    ColorTemplate.rgb("#FFEB3B"), // Aug
                    ColorTemplate.rgb("#3F51B5"), // Sep
                    ColorTemplate.rgb("#E91E63"), // Oct
                    ColorTemplate.rgb("#00BCD4"), // Nov
                    ColorTemplate.rgb("#8BC34A")  // Dec
                )
                dataSet.setDrawValues(true)

                val barData = BarData(dataSet)
                barData.barWidth = 0.4f
                binding.sellsBarChart.data = barData
                binding.sellsBarChart.invalidate()
            }
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

    private fun loadSoldOutIncubators() {
        var query = firestore.collection("soldIncubators")
            .orderBy("soldDate", Query.Direction.DESCENDING)

        if (startDate != null && endDate != null) {
            query = query.whereGreaterThanOrEqualTo("soldDate", startDate!!)
                .whereLessThanOrEqualTo("soldDate", endDate!!)
        }

        query.get()
            .addOnSuccessListener { snap ->
                soldIncubators.clear()
                var totalSold = 0
                var totalSellAmount = 0.0
                var totalCost = 0.0
                var totalProfit = 0.0

                for (doc in snap) {
                    val soldIncubator = doc.toObject(SoldOutIncubator::class.java).copy(id = doc.id)
                    soldIncubators.add(soldIncubator)
                    totalSold++
                    totalSellAmount += soldIncubator.sellPrice
                    totalCost += soldIncubator.incubatorBuildCost + soldIncubator.transportCost
                    totalProfit += soldIncubator.profit
                }

                // Update UI
                binding.totalSoldIncubators.text = "Total Incubators Sold: $totalSold"
                binding.totalSellAmount.text = "Total Sell: ₹${String.format("%.2f", totalSellAmount)}"
                binding.totalCostAmount.text = "Total Cost: ₹${String.format("%.2f", totalCost)}"
                binding.totalProfitAmount.text = "Total Profit: ₹${String.format("%.2f", totalProfit)}"

                soldOutAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
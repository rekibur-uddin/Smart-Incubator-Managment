package com.app.smartincubatormanagment.data.model

data class Part(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val price: String = "",
    var quantity: String = "",
    val description: String = ""
)

data class BuiltIncubator(
    val id: String = "",
    val capacity: String = "",
    val imageLink: String = "",
    val parts: List<Map<String, Any>> = emptyList(),
    val laborCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val timestamp: Long = 0
)


data class SoldOutIncubator(
    val id: String = "",
    val incubatorId: String = "",
    val capacityName: String = "",
    val imageLink: String = "",
    val sellPrice: Double = 0.0,
    val transportCost: Double = 0.0,
    val incubatorBuildCost: Double = 0.0,
    val profit: Double = 0.0,
    val customerDetails: String = "",
    val soldDate: com.google.firebase.Timestamp? = null
)


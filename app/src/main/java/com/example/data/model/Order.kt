package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val productId: Long? = null,
    val productName: String,
    val sellingPrice: Double,
    val discount: Double = 0.0,
    val productCost: Double,
    val packagingCost: Double = 0.0,
    val shippingCost: Double = 0.0,
    val paymentFee: Double = 0.0,
    val adCost: Double = 0.0,
    val isRto: Boolean = false,
    val rtoLoss: Double = 0.0,
    val productReturnedToStock: Boolean = true,
    val revenue: Double,
    val totalCost: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sellingPrice: Double,
    val productCost: Double,
    val packagingCost: Double = 0.0,
    val shippingCost: Double = 0.0,
    val paymentFee: Double = 0.0,
    val adCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Total default costs for this product
     */
    val totalDefaultCost: Double
        get() = productCost + packagingCost + shippingCost + paymentFee + adCost

    /**
     * Estimated profit when all default costs are deducted from selling price
     */
    val estimatedProfit: Double
        get() = sellingPrice - totalDefaultCost

    /**
     * Estimated profit margin percentage
     */
    val estimatedMargin: Double
        get() = if (sellingPrice > 0) (estimatedProfit / sellingPrice) * 100.0 else 0.0
}

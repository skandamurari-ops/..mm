package com.example.domain

import java.util.Locale
import kotlin.math.abs

data class CalculationBreakdown(
    val sellingPrice: Double,
    val discount: Double,
    val revenue: Double,
    val productCost: Double,
    val packagingCost: Double,
    val shippingCost: Double,
    val paymentFee: Double,
    val adCost: Double,
    val isRto: Boolean,
    val rtoLoss: Double,
    val productReturnedToStock: Boolean,
    val effectiveProductCost: Double,
    val totalCost: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val isLoss: Boolean
)

object ProfitCalculator {

    /**
     * Consistent profit formula:
     * Revenue = Selling Price - Discount
     * Total Cost = Product Cost + Packaging + Shipping + Payment/COD Fee + Advertising Cost + Additional RTO Loss
     * Net Profit = Revenue - Total Cost
     * Profit Margin = Net Profit / Revenue * 100
     */
    fun calculate(
        sellingPrice: Double,
        discount: Double = 0.0,
        productCost: Double,
        packagingCost: Double = 0.0,
        shippingCost: Double = 0.0,
        paymentFee: Double = 0.0,
        adCost: Double = 0.0,
        isRto: Boolean = false,
        rtoLoss: Double = 0.0,
        productReturnedToStock: Boolean = true
    ): CalculationBreakdown {
        val safeSellingPrice = sellingPrice.coerceAtLeast(0.0)
        val safeDiscount = discount.coerceAtLeast(0.0)
        val safeProductCost = productCost.coerceAtLeast(0.0)
        val safePackaging = packagingCost.coerceAtLeast(0.0)
        val safeShipping = shippingCost.coerceAtLeast(0.0)
        val safePayment = paymentFee.coerceAtLeast(0.0)
        val safeAdCost = adCost.coerceAtLeast(0.0)
        val safeRtoLoss = rtoLoss.coerceAtLeast(0.0)

        return if (!isRto) {
            val revenue = (safeSellingPrice - safeDiscount).coerceAtLeast(0.0)
            val totalCost = safeProductCost + safePackaging + safeShipping + safePayment + safeAdCost
            val netProfit = revenue - totalCost
            val margin = if (revenue > 0) (netProfit / revenue) * 100.0 else 0.0
            CalculationBreakdown(
                sellingPrice = safeSellingPrice,
                discount = safeDiscount,
                revenue = revenue,
                productCost = safeProductCost,
                packagingCost = safePackaging,
                shippingCost = safeShipping,
                paymentFee = safePayment,
                adCost = safeAdCost,
                isRto = false,
                rtoLoss = 0.0,
                productReturnedToStock = true,
                effectiveProductCost = safeProductCost,
                totalCost = totalCost,
                netProfit = netProfit,
                profitMargin = margin,
                isLoss = netProfit < 0
            )
        } else {
            // In RTO / Returned: Customer did not pay or payment was reversed
            val effectiveProductCost = if (productReturnedToStock) 0.0 else safeProductCost
            val totalCost = effectiveProductCost + safePackaging + safeShipping + safePayment + safeAdCost + safeRtoLoss
            val netProfit = -totalCost
            val referenceBase = if (safeSellingPrice > 0) safeSellingPrice else (totalCost.coerceAtLeast(1.0))
            val margin = (netProfit / referenceBase) * 100.0
            CalculationBreakdown(
                sellingPrice = safeSellingPrice,
                discount = safeDiscount,
                revenue = 0.0,
                productCost = safeProductCost,
                packagingCost = safePackaging,
                shippingCost = safeShipping,
                paymentFee = safePayment,
                adCost = safeAdCost,
                isRto = true,
                rtoLoss = safeRtoLoss,
                productReturnedToStock = productReturnedToStock,
                effectiveProductCost = effectiveProductCost,
                totalCost = totalCost,
                netProfit = netProfit,
                profitMargin = margin,
                isLoss = true
            )
        }
    }

    fun formatRupees(amount: Double): String {
        val isNegative = amount < 0
        val absVal = abs(amount)
        val formatted = if (absVal % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", absVal)
        } else {
            String.format(Locale.US, "%,.2f", absVal)
        }
        return if (isNegative) "−₹$formatted" else "₹$formatted"
    }

    fun formatMargin(marginPercent: Double): String {
        return String.format(Locale.US, "%.1f%%", marginPercent)
    }
}

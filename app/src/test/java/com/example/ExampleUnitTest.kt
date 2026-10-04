package com.example

import com.example.domain.ProfitCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test user prompt profitable order example`() {
        // Selling Price: ₹999
        // Product Cost: ₹400
        // Packaging: ₹30
        // Shipping: ₹70
        // Payment Fee: ₹20
        // Ad Cost: ₹100
        // ACTUAL PROFIT = ₹379, 37.9% margin
        val result = ProfitCalculator.calculate(
            sellingPrice = 999.0,
            productCost = 400.0,
            packagingCost = 30.0,
            shippingCost = 70.0,
            paymentFee = 20.0,
            adCost = 100.0
        )

        assertEquals(999.0, result.revenue, 0.001)
        assertEquals(620.0, result.totalCost, 0.001)
        assertEquals(379.0, result.netProfit, 0.001)
        assertEquals(37.9379, result.profitMargin, 0.05)
        assertFalse(result.isLoss)
        assertEquals("₹379", ProfitCalculator.formatRupees(result.netProfit))
    }

    @Test
    fun `test user prompt loss making order`() {
        // Selling price ₹500, Product cost ₹400, Shipping ₹90, Ad cost ₹100 -> Cost = 590, Revenue = 500, Loss = -90
        val result = ProfitCalculator.calculate(
            sellingPrice = 500.0,
            productCost = 400.0,
            shippingCost = 90.0,
            adCost = 100.0
        )

        assertEquals(500.0, result.revenue, 0.001)
        assertEquals(590.0, result.totalCost, 0.001)
        assertEquals(-90.0, result.netProfit, 0.001)
        assertTrue(result.isLoss)
        assertEquals("−₹90", ProfitCalculator.formatRupees(result.netProfit))
    }

    @Test
    fun `test discount in profit calculation`() {
        // Revenue = Selling Price - Discount
        val result = ProfitCalculator.calculate(
            sellingPrice = 1000.0,
            discount = 100.0,
            productCost = 400.0
        )

        assertEquals(900.0, result.revenue, 0.001)
        assertEquals(400.0, result.totalCost, 0.001)
        assertEquals(500.0, result.netProfit, 0.001)
    }

    @Test
    fun `test RTO order when product safely returned to stock`() {
        // In RTO with product returned to stock:
        // Product cost is not lost (inventory preserved)
        // Seller loses shipping (₹70), packaging (₹30), ad cost (₹100), additional RTO loss (₹50)
        val result = ProfitCalculator.calculate(
            sellingPrice = 799.0,
            productCost = 400.0,
            packagingCost = 30.0,
            shippingCost = 70.0,
            adCost = 100.0,
            isRto = true,
            rtoLoss = 50.0,
            productReturnedToStock = true
        )

        assertEquals(0.0, result.revenue, 0.001)
        assertEquals(250.0, result.totalCost, 0.001) // 30 + 70 + 100 + 50
        assertEquals(-250.0, result.netProfit, 0.001)
        assertTrue(result.isLoss)
    }

    @Test
    fun `test RTO order when product damaged and not returned`() {
        // In RTO where product was damaged:
        // Product cost IS counted as loss
        val result = ProfitCalculator.calculate(
            sellingPrice = 799.0,
            productCost = 400.0,
            packagingCost = 30.0,
            shippingCost = 70.0,
            adCost = 100.0,
            isRto = true,
            rtoLoss = 50.0,
            productReturnedToStock = false
        )

        assertEquals(650.0, result.totalCost, 0.001) // 400 + 30 + 70 + 100 + 50
        assertEquals(-650.0, result.netProfit, 0.001)
        assertTrue(result.isLoss)
    }
}

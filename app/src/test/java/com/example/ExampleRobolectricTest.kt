package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.repository.ProfitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ProfitRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProfitRepository(database.productDao(), database.orderDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `read string from context matches RealProfit`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RealProfit", appName)
    }

    @Test
    fun `initial product count is zero`() = runBlocking {
        val products = repository.allProducts.first()
        assertEquals(0, products.size)
    }

    @Test
    fun `add product and verify calculation properties`() = runBlocking {
        val product = Product(
            name = "Gold Necklace",
            sellingPrice = 999.0,
            productCost = 400.0,
            packagingCost = 30.0,
            shippingCost = 70.0,
            paymentFee = 20.0,
            adCost = 100.0
        )
        val id = repository.insertProduct(product)
        val loaded = repository.getProductById(id)
        assertNotNull(loaded)
        assertEquals("Gold Necklace", loaded?.name)
        assertEquals(379.0, loaded?.estimatedProfit ?: 0.0, 0.001)

        val count = repository.productCount.first()
        assertEquals(1, count)
    }

    @Test
    fun `save order and read from repository`() = runBlocking {
        val orderNumber = repository.getNextOrderNumber()
        assertEquals("#001", orderNumber)

        val order = Order(
            orderNumber = orderNumber,
            productName = "Gold Necklace",
            sellingPrice = 999.0,
            discount = 0.0,
            productCost = 400.0,
            packagingCost = 30.0,
            shippingCost = 70.0,
            paymentFee = 20.0,
            adCost = 100.0,
            isRto = false,
            revenue = 999.0,
            totalCost = 620.0,
            netProfit = 379.0,
            profitMargin = 37.9
        )
        repository.insertOrder(order)

        val orders = repository.allOrders.first()
        assertEquals(1, orders.size)
        assertEquals("#001", orders[0].orderNumber)
        assertEquals(379.0, orders[0].netProfit, 0.001)

        val nextOrderNumber = repository.getNextOrderNumber()
        assertEquals("#002", nextOrderNumber)
    }
}

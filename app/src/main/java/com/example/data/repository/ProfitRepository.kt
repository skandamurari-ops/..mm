package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.ProductDao
import com.example.data.model.Order
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

class ProfitRepository(
    private val productDao: ProductDao,
    private val orderDao: OrderDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val productCount: Flow<Int> = productDao.getProductCount()

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)

    suspend fun insertOrder(order: Order): Long = orderDao.insertOrder(order)

    suspend fun deleteOrder(order: Order) = orderDao.deleteOrder(order)

    suspend fun deleteOrderById(id: Long) = orderDao.deleteOrderById(id)

    suspend fun getNextOrderNumber(): String {
        val count = orderDao.getOrderCount()
        return String.format(java.util.Locale.US, "#%03d", count + 1)
    }
}

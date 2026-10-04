package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.repository.ProfitRepository
import com.example.domain.CalculationBreakdown
import com.example.domain.ProfitCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab(val title: String) {
    HOME("Home"),
    PRODUCTS("Products"),
    CALCULATOR("Calculator"),
    ORDERS("Orders")
}

enum class OrderFilter(val label: String) {
    ALL("All"),
    DELIVERED("Delivered"),
    RTO("RTO / Returned")
}

data class DashboardMetrics(
    val todaySales: Double = 0.0,
    val todayProfit: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val todayMargin: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val avgProfitPerOrder: Double = 0.0,
    val deliveredCount: Int = 0,
    val rtoCount: Int = 0,
    val rtoLosses: Double = 0.0,
    val bestProfitProduct: String? = null,
    val worstProfitProduct: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProfitRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProfitRepository(database.productDao(), database.orderDao())
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun navigateToTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Products Flow
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders Flow
    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Order Filter
    private val _orderFilter = MutableStateFlow(OrderFilter.ALL)
    val orderFilter: StateFlow<OrderFilter> = _orderFilter.asStateFlow()

    fun setOrderFilter(filter: OrderFilter) {
        _orderFilter.value = filter
    }

    val filteredOrders: StateFlow<List<Order>> = combine(orders, _orderFilter) { list, filter ->
        when (filter) {
            OrderFilter.ALL -> list
            OrderFilter.DELIVERED -> list.filter { !it.isRto }
            OrderFilter.RTO -> list.filter { it.isRto }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = orders.combine(products) { ordersList, _ ->
        computeMetrics(ordersList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Add / Edit Product Dialog state
    var showProductDialog = MutableStateFlow(false)
        private set
    var editingProduct = MutableStateFlow<Product?>(null)
        private set

    fun openAddProduct() {
        editingProduct.value = null
        showProductDialog.value = true
    }

    fun openEditProduct(product: Product) {
        editingProduct.value = product
        showProductDialog.value = true
    }

    fun dismissProductDialog() {
        showProductDialog.value = false
        editingProduct.value = null
    }

    fun saveProduct(
        name: String,
        sellingPrice: Double,
        productCost: Double,
        packagingCost: Double,
        shippingCost: Double,
        paymentFee: Double,
        adCost: Double
    ) {
        viewModelScope.launch {
            val current = editingProduct.value
            if (current != null) {
                repository.updateProduct(
                    current.copy(
                        name = name.trim(),
                        sellingPrice = sellingPrice,
                        productCost = productCost,
                        packagingCost = packagingCost,
                        shippingCost = shippingCost,
                        paymentFee = paymentFee,
                        adCost = adCost
                    )
                )
            } else {
                repository.insertProduct(
                    Product(
                        name = name.trim(),
                        sellingPrice = sellingPrice,
                        productCost = productCost,
                        packagingCost = packagingCost,
                        shippingCost = shippingCost,
                        paymentFee = paymentFee,
                        adCost = adCost
                    )
                )
            }
            dismissProductDialog()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            if (selectedProduct.value?.id == product.id) {
                selectedProduct.value = null
            }
        }
    }

    // Calculator State
    val selectedProduct = MutableStateFlow<Product?>(null)
    val customProductName = MutableStateFlow("")

    val calcSellingPrice = MutableStateFlow("")
    val calcDiscount = MutableStateFlow("")
    val calcProductCost = MutableStateFlow("")
    val calcPackagingCost = MutableStateFlow("")
    val calcShippingCost = MutableStateFlow("")
    val calcPaymentFee = MutableStateFlow("")
    val calcAdCost = MutableStateFlow("")
    val calcIsRto = MutableStateFlow(false)
    val calcRtoLoss = MutableStateFlow("")
    val calcProductReturnedToStock = MutableStateFlow(true)

    // Snackbar / Feedback message
    val userMessage = MutableStateFlow<String?>(null)

    fun clearUserMessage() {
        userMessage.value = null
    }

    fun selectProductForCalculator(product: Product?) {
        selectedProduct.value = product
        if (product != null) {
            customProductName.value = product.name
            calcSellingPrice.value = if (product.sellingPrice > 0) formatPlain(product.sellingPrice) else ""
            calcDiscount.value = ""
            calcProductCost.value = if (product.productCost > 0) formatPlain(product.productCost) else ""
            calcPackagingCost.value = if (product.packagingCost > 0) formatPlain(product.packagingCost) else ""
            calcShippingCost.value = if (product.shippingCost > 0) formatPlain(product.shippingCost) else ""
            calcPaymentFee.value = if (product.paymentFee > 0) formatPlain(product.paymentFee) else ""
            calcAdCost.value = if (product.adCost > 0) formatPlain(product.adCost) else ""
            calcIsRto.value = false
            calcRtoLoss.value = ""
            calcProductReturnedToStock.value = true
        } else {
            customProductName.value = ""
        }
    }

    fun onCalculateProductClicked(product: Product) {
        selectProductForCalculator(product)
        _currentTab.value = AppTab.CALCULATOR
    }

    fun resetCalculator() {
        selectedProduct.value = null
        customProductName.value = ""
        calcSellingPrice.value = ""
        calcDiscount.value = ""
        calcProductCost.value = ""
        calcPackagingCost.value = ""
        calcShippingCost.value = ""
        calcPaymentFee.value = ""
        calcAdCost.value = ""
        calcIsRto.value = false
        calcRtoLoss.value = ""
        calcProductReturnedToStock.value = true
    }

    fun getCalculation(): CalculationBreakdown {
        val sp = calcSellingPrice.value.toDoubleOrNull() ?: 0.0
        val disc = calcDiscount.value.toDoubleOrNull() ?: 0.0
        val pc = calcProductCost.value.toDoubleOrNull() ?: 0.0
        val pkg = calcPackagingCost.value.toDoubleOrNull() ?: 0.0
        val ship = calcShippingCost.value.toDoubleOrNull() ?: 0.0
        val pay = calcPaymentFee.value.toDoubleOrNull() ?: 0.0
        val ad = calcAdCost.value.toDoubleOrNull() ?: 0.0
        val isRtoVal = calcIsRto.value
        val rtoL = calcRtoLoss.value.toDoubleOrNull() ?: 0.0
        val returned = calcProductReturnedToStock.value

        return ProfitCalculator.calculate(
            sellingPrice = sp,
            discount = disc,
            productCost = pc,
            packagingCost = pkg,
            shippingCost = ship,
            paymentFee = pay,
            adCost = ad,
            isRto = isRtoVal,
            rtoLoss = rtoL,
            productReturnedToStock = returned
        )
    }

    fun saveCurrentOrder() {
        val breakdown = getCalculation()
        val name = customProductName.value.ifBlank {
            selectedProduct.value?.name ?: "Custom Product"
        }

        viewModelScope.launch {
            val orderNumber = repository.getNextOrderNumber()
            val order = Order(
                orderNumber = orderNumber,
                productId = selectedProduct.value?.id,
                productName = name,
                sellingPrice = breakdown.sellingPrice,
                discount = breakdown.discount,
                productCost = breakdown.productCost,
                packagingCost = breakdown.packagingCost,
                shippingCost = breakdown.shippingCost,
                paymentFee = breakdown.paymentFee,
                adCost = breakdown.adCost,
                isRto = breakdown.isRto,
                rtoLoss = breakdown.rtoLoss,
                productReturnedToStock = breakdown.productReturnedToStock,
                revenue = breakdown.revenue,
                totalCost = breakdown.totalCost,
                netProfit = breakdown.netProfit,
                profitMargin = breakdown.profitMargin
            )
            repository.insertOrder(order)
            userMessage.value = "Order $orderNumber saved successfully!"
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            userMessage.value = "Order ${order.orderNumber} deleted"
        }
    }

    private fun computeMetrics(ordersList: List<Order>): DashboardMetrics {
        if (ordersList.isEmpty()) {
            return DashboardMetrics()
        }

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var todaySales = 0.0
        var todayProfit = 0.0
        var todayCount = 0

        var totalSales = 0.0
        var totalProfit = 0.0
        var deliveredCount = 0
        var rtoCount = 0
        var rtoLosses = 0.0

        val productProfits = mutableMapOf<String, Double>()

        for (order in ordersList) {
            totalSales += order.revenue
            totalProfit += order.netProfit
            if (order.isRto) {
                rtoCount++
                rtoLosses += kotlin.math.abs(order.netProfit)
            } else {
                deliveredCount++
            }

            // Track profit by product name
            val currentProdProfit = productProfits.getOrDefault(order.productName, 0.0)
            productProfits[order.productName] = currentProdProfit + order.netProfit

            if (order.createdAt >= startOfToday) {
                todaySales += order.revenue
                todayProfit += order.netProfit
                todayCount++
            }
        }

        val todayMargin = if (todaySales > 0) (todayProfit / todaySales) * 100.0 else 0.0
        val totalCount = ordersList.size
        val avgProfit = if (totalCount > 0) totalProfit / totalCount else 0.0

        val bestProduct = productProfits.maxByOrNull { it.value }?.key
        val worstProduct = productProfits.minByOrNull { it.value }?.key

        return DashboardMetrics(
            todaySales = todaySales,
            todayProfit = todayProfit,
            todayOrdersCount = todayCount,
            todayMargin = todayMargin,
            totalSales = totalSales,
            totalProfit = totalProfit,
            totalOrdersCount = totalCount,
            avgProfitPerOrder = avgProfit,
            deliveredCount = deliveredCount,
            rtoCount = rtoCount,
            rtoLosses = rtoLosses,
            bestProfitProduct = if (productProfits.size > 1 || totalProfit != 0.0) bestProduct else null,
            worstProfitProduct = if (productProfits.size > 1 && worstProduct != bestProduct) worstProduct else null
        )
    }

    private fun formatPlain(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
    }
}

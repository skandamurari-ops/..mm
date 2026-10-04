package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RealProfitApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RealProfitApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val orderFilter by viewModel.orderFilter.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val showProductDialog by viewModel.showProductDialog.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()

    // Calculator state
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val customProductName by viewModel.customProductName.collectAsStateWithLifecycle()
    val calcSellingPrice by viewModel.calcSellingPrice.collectAsStateWithLifecycle()
    val calcDiscount by viewModel.calcDiscount.collectAsStateWithLifecycle()
    val calcProductCost by viewModel.calcProductCost.collectAsStateWithLifecycle()
    val calcPackagingCost by viewModel.calcPackagingCost.collectAsStateWithLifecycle()
    val calcShippingCost by viewModel.calcShippingCost.collectAsStateWithLifecycle()
    val calcPaymentFee by viewModel.calcPaymentFee.collectAsStateWithLifecycle()
    val calcAdCost by viewModel.calcAdCost.collectAsStateWithLifecycle()
    val calcIsRto by viewModel.calcIsRto.collectAsStateWithLifecycle()
    val calcRtoLoss by viewModel.calcRtoLoss.collectAsStateWithLifecycle()
    val calcProductReturnedToStock by viewModel.calcProductReturnedToStock.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Back handling: If on secondary tab, return to Home tab
    if (currentTab != AppTab.HOME) {
        BackHandler {
            viewModel.navigateToTab(AppTab.HOME)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { viewModel.navigateToTab(AppTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PRODUCTS,
                    onClick = { viewModel.navigateToTab(AppTab.PRODUCTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.PRODUCTS) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                            contentDescription = "Products"
                        )
                    },
                    label = { Text("Products") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_products")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CALCULATOR,
                    onClick = { viewModel.navigateToTab(AppTab.CALCULATOR) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CALCULATOR) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                            contentDescription = "Calculator"
                        )
                    },
                    label = { Text("Calculator") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_calculator")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.ORDERS,
                    onClick = { viewModel.navigateToTab(AppTab.ORDERS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Orders"
                        )
                    },
                    label = { Text("Orders") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_orders")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        metrics = metrics,
                        products = products,
                        onAddProductClick = { viewModel.openAddProduct() },
                        onCalculateProductClick = { product ->
                            viewModel.onCalculateProductClicked(product)
                        },
                        onNavigateToProducts = { viewModel.navigateToTab(AppTab.PRODUCTS) },
                        onNavigateToCalculator = { viewModel.navigateToTab(AppTab.CALCULATOR) }
                    )
                }

                AppTab.PRODUCTS -> {
                    ProductsScreen(
                        products = products,
                        showAddEditDialog = showProductDialog,
                        editingProduct = editingProduct,
                        onAddProductClick = { viewModel.openAddProduct() },
                        onEditProductClick = { product -> viewModel.openEditProduct(product) },
                        onDeleteProductClick = { product -> viewModel.deleteProduct(product) },
                        onDismissDialog = { viewModel.dismissProductDialog() },
                        onSaveProduct = { name, sp, pc, pkg, ship, pay, ad ->
                            viewModel.saveProduct(name, sp, pc, pkg, ship, pay, ad)
                        },
                        onCalculateProductClick = { product ->
                            viewModel.onCalculateProductClicked(product)
                        }
                    )
                }

                AppTab.CALCULATOR -> {
                    CalculatorScreen(
                        products = products,
                        selectedProduct = selectedProduct,
                        customProductName = customProductName,
                        onCustomProductNameChange = { viewModel.customProductName.value = it },
                        sellingPrice = calcSellingPrice,
                        onSellingPriceChange = { viewModel.calcSellingPrice.value = it },
                        discount = calcDiscount,
                        onDiscountChange = { viewModel.calcDiscount.value = it },
                        productCost = calcProductCost,
                        onProductCostChange = { viewModel.calcProductCost.value = it },
                        packagingCost = calcPackagingCost,
                        onPackagingCostChange = { viewModel.calcPackagingCost.value = it },
                        shippingCost = calcShippingCost,
                        onShippingCostChange = { viewModel.calcShippingCost.value = it },
                        paymentFee = calcPaymentFee,
                        onPaymentFeeChange = { viewModel.calcPaymentFee.value = it },
                        adCost = calcAdCost,
                        onAdCostChange = { viewModel.calcAdCost.value = it },
                        isRto = calcIsRto,
                        onIsRtoChange = { viewModel.calcIsRto.value = it },
                        rtoLoss = calcRtoLoss,
                        onRtoLossChange = { viewModel.calcRtoLoss.value = it },
                        productReturnedToStock = calcProductReturnedToStock,
                        onProductReturnedToStockChange = { viewModel.calcProductReturnedToStock.value = it },
                        breakdown = viewModel.getCalculation(),
                        onSelectProduct = { product -> viewModel.selectProductForCalculator(product) },
                        onSaveOrder = { viewModel.saveCurrentOrder() },
                        onReset = { viewModel.resetCalculator() }
                    )
                }

                AppTab.ORDERS -> {
                    OrdersScreen(
                        orders = filteredOrders,
                        currentFilter = orderFilter,
                        onFilterChange = { viewModel.setOrderFilter(it) },
                        onDeleteOrder = { viewModel.deleteOrder(it) },
                        onGoToCalculator = { viewModel.navigateToTab(AppTab.CALCULATOR) }
                    )
                }
            }
        }
    }

    // Floating Add Product dialog if triggered from Home or elsewhere
    if (showProductDialog && currentTab != AppTab.PRODUCTS) {
        com.example.ui.screens.AddEditProductDialog(
            initialProduct = editingProduct,
            onDismiss = { viewModel.dismissProductDialog() },
            onSave = { name, sp, pc, pkg, ship, pay, ad ->
                viewModel.saveProduct(name, sp, pc, pkg, ship, pay, ad)
            }
        )
    }
}

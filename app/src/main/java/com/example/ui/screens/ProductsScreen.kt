package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.domain.ProfitCalculator
import com.example.ui.components.CurrencyInputField
import com.example.ui.components.EmptyStateView

@Composable
fun ProductsScreen(
    products: List<Product>,
    showAddEditDialog: Boolean,
    editingProduct: Product?,
    onAddProductClick: () -> Unit,
    onEditProductClick: (Product) -> Unit,
    onDeleteProductClick: (Product) -> Unit,
    onDismissDialog: () -> Unit,
    onSaveProduct: (String, Double, Double, Double, Double, Double, Double) -> Unit,
    onCalculateProductClick: (Product) -> Unit
) {
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            if (products.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddProductClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_product")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Product"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("products_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (products.isEmpty()) {
                EmptyStateView(
                    title = "No products yet",
                    message = "Add your first product to start calculating your profit.",
                    buttonText = "+ Add Product",
                    onButtonClick = onAddProductClick,
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.align(Alignment.Center),
                    testTag = "products_empty_state"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Products",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${products.size} product${if (products.size > 1) "s" else ""} added",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onAddProductClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("btn_add_product_top")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Product")
                            }
                        }
                    }

                    items(products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onEdit = { onEditProductClick(product) },
                            onDelete = { productToDelete = product },
                            onCalculate = { onCalculateProductClick(product) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddEditDialog) {
        AddEditProductDialog(
            initialProduct = editingProduct,
            onDismiss = onDismissDialog,
            onSave = onSaveProduct
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to delete \"${product.name}\"? Past saved orders will keep their records.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProductClick(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCalculate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selling Price & Product Cost
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Selling Price: ${ProfitCalculator.formatRupees(product.sellingPrice)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Product Cost: ${ProfitCalculator.formatRupees(product.productCost)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Estimated Profit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Estimated Profit: ${ProfitCalculator.formatRupees(product.estimatedProfit)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (product.estimatedProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.estimatedProfit >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = ProfitCalculator.formatMargin(product.estimatedMargin),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.estimatedProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Optional defaults info (if any costs set)
            val optionalCosts = listOfNotNull(
                if (product.packagingCost > 0) "Pack: ${ProfitCalculator.formatRupees(product.packagingCost)}" else null,
                if (product.shippingCost > 0) "Ship: ${ProfitCalculator.formatRupees(product.shippingCost)}" else null,
                if (product.paymentFee > 0) "COD/Pay: ${ProfitCalculator.formatRupees(product.paymentFee)}" else null,
                if (product.adCost > 0) "Ad: ${ProfitCalculator.formatRupees(product.adCost)}" else null
            )
            if (optionalCosts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Defaults: ${optionalCosts.joinToString(" • ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions: Edit | Delete | Calculate
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("btn_edit_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_delete_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onCalculate,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_calc_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculate",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calculate",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, Double, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sellingPrice by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.sellingPrice > 0) {
            if (initialProduct.sellingPrice % 1.0 == 0.0) initialProduct.sellingPrice.toLong().toString() else initialProduct.sellingPrice.toString()
        } else "")
    }
    var productCost by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.productCost > 0) {
            if (initialProduct.productCost % 1.0 == 0.0) initialProduct.productCost.toLong().toString() else initialProduct.productCost.toString()
        } else "")
    }
    var packagingCost by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.packagingCost > 0) {
            if (initialProduct.packagingCost % 1.0 == 0.0) initialProduct.packagingCost.toLong().toString() else initialProduct.packagingCost.toString()
        } else "")
    }
    var shippingCost by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.shippingCost > 0) {
            if (initialProduct.shippingCost % 1.0 == 0.0) initialProduct.shippingCost.toLong().toString() else initialProduct.shippingCost.toString()
        } else "")
    }
    var paymentFee by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.paymentFee > 0) {
            if (initialProduct.paymentFee % 1.0 == 0.0) initialProduct.paymentFee.toLong().toString() else initialProduct.paymentFee.toString()
        } else "")
    }
    var adCost by remember {
        mutableStateOf(if (initialProduct != null && initialProduct.adCost > 0) {
            if (initialProduct.adCost % 1.0 == 0.0) initialProduct.adCost.toLong().toString() else initialProduct.adCost.toString()
        } else "")
    }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var costError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialProduct == null) "Add Product" else "Edit Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Keep it simple. You can adjust costs per order anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Required: Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Gold Necklace, Black T-Shirt") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Product name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Required: Selling Price
                CurrencyInputField(
                    value = sellingPrice,
                    onValueChange = {
                        sellingPrice = it
                        if (priceError && it.isNotBlank()) priceError = false
                    },
                    label = "Selling Price *",
                    placeholder = "999",
                    testTag = "input_selling_price"
                )
                if (priceError) {
                    Text(
                        text = "Enter a valid selling price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Required: Product Cost
                CurrencyInputField(
                    value = productCost,
                    onValueChange = {
                        productCost = it
                        if (costError && it.isNotBlank()) costError = false
                    },
                    label = "Product Cost *",
                    placeholder = "400",
                    testTag = "input_product_cost"
                )
                if (costError) {
                    Text(
                        text = "Enter product cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Section Header
                Text(
                    text = "Optional Defaults (per order)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Optional: Packaging Cost
                CurrencyInputField(
                    value = packagingCost,
                    onValueChange = { packagingCost = it },
                    label = "Packaging Cost",
                    isOptional = true,
                    placeholder = "30",
                    testTag = "input_packaging_cost"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional: Shipping Cost
                CurrencyInputField(
                    value = shippingCost,
                    onValueChange = { shippingCost = it },
                    label = "Shipping Cost",
                    isOptional = true,
                    placeholder = "70",
                    testTag = "input_shipping_cost"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional: Payment/COD Fee
                CurrencyInputField(
                    value = paymentFee,
                    onValueChange = { paymentFee = it },
                    label = "Payment / COD Fee",
                    isOptional = true,
                    placeholder = "20",
                    testTag = "input_payment_fee"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional: Advertising Cost per Order
                CurrencyInputField(
                    value = adCost,
                    onValueChange = { adCost = it },
                    label = "Advertising Cost per Order",
                    isOptional = true,
                    placeholder = "100",
                    testTag = "input_ad_cost"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save / Cancel Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = true
                                hasError = true
                            }
                            val sp = sellingPrice.toDoubleOrNull()
                            if (sp == null || sp <= 0) {
                                priceError = true
                                hasError = true
                            }
                            val pc = productCost.toDoubleOrNull()
                            if (pc == null || pc < 0) {
                                costError = true
                                hasError = true
                            }

                            if (!hasError && sp != null && pc != null) {
                                onSave(
                                    name,
                                    sp,
                                    pc,
                                    packagingCost.toDoubleOrNull() ?: 0.0,
                                    shippingCost.toDoubleOrNull() ?: 0.0,
                                    paymentFee.toDoubleOrNull() ?: 0.0,
                                    adCost.toDoubleOrNull() ?: 0.0
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_save_product")
                    ) {
                        Text(if (initialProduct == null) "Save Product" else "Update Product")
                    }
                }
            }
        }
    }
}

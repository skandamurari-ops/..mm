package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.domain.CalculationBreakdown
import com.example.domain.ProfitCalculator
import com.example.ui.components.CurrencyInputField

@Composable
fun CalculatorScreen(
    products: List<Product>,
    selectedProduct: Product?,
    customProductName: String,
    onCustomProductNameChange: (String) -> Unit,
    sellingPrice: String,
    onSellingPriceChange: (String) -> Unit,
    discount: String,
    onDiscountChange: (String) -> Unit,
    productCost: String,
    onProductCostChange: (String) -> Unit,
    packagingCost: String,
    onPackagingCostChange: (String) -> Unit,
    shippingCost: String,
    onShippingCostChange: (String) -> Unit,
    paymentFee: String,
    onPaymentFeeChange: (String) -> Unit,
    adCost: String,
    onAdCostChange: (String) -> Unit,
    isRto: Boolean,
    onIsRtoChange: (Boolean) -> Unit,
    rtoLoss: String,
    onRtoLossChange: (String) -> Unit,
    productReturnedToStock: Boolean,
    onProductReturnedToStockChange: (Boolean) -> Unit,
    breakdown: CalculationBreakdown,
    onSelectProduct: (Product?) -> Unit,
    onSaveOrder: () -> Unit,
    onReset: () -> Unit
) {
    var productDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("calculator_screen")
    ) {
        // Title & description
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Profit Calculator",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calculate the real profit you make on this order",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButtonWithLabel(
                    text = "Reset",
                    onClick = onReset
                )
            }
        }

        // DOMINANT PROFIT RESULT CARD (Front & Center)
        item {
            DominantProfitCard(breakdown = breakdown)
        }

        // STEP 1: Select Product
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Select Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: if (customProductName.isNotBlank()) customProductName else "Choose from your products...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Product"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { productDropdownExpanded = true }
                                .testTag("dropdown_select_product")
                        )

                        DropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (products.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No saved products yet. Enter values below.") },
                                    onClick = { productDropdownExpanded = false }
                                )
                            } else {
                                products.forEach { product ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "Sale: ${ProfitCalculator.formatRupees(product.sellingPrice)} • Cost: ${ProfitCalculator.formatRupees(product.productCost)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectProduct(product)
                                            productDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedProduct == null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customProductName,
                            onValueChange = onCustomProductNameChange,
                            label = { Text("Product / Order Name (optional)") },
                            placeholder = { Text("e.g. Custom Silk Scarf") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // STEP 2: Order-specific Costs (Override freely)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Order Costs & Pricing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Loaded defaults can be adjusted for this particular order",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selling Price & Discount
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CurrencyInputField(
                            value = sellingPrice,
                            onValueChange = onSellingPriceChange,
                            label = "Selling Price",
                            placeholder = "999",
                            modifier = Modifier.weight(1f),
                            testTag = "calc_selling_price"
                        )

                        CurrencyInputField(
                            value = discount,
                            onValueChange = onDiscountChange,
                            label = "Discount",
                            placeholder = "0",
                            isOptional = true,
                            modifier = Modifier.weight(1f),
                            testTag = "calc_discount"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Product Cost & Packaging
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CurrencyInputField(
                            value = productCost,
                            onValueChange = onProductCostChange,
                            label = "Product Cost",
                            placeholder = "400",
                            modifier = Modifier.weight(1f),
                            testTag = "calc_product_cost"
                        )

                        CurrencyInputField(
                            value = packagingCost,
                            onValueChange = onPackagingCostChange,
                            label = "Packaging",
                            placeholder = "30",
                            isOptional = true,
                            modifier = Modifier.weight(1f),
                            testTag = "calc_packaging_cost"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shipping & Payment/COD Fee
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CurrencyInputField(
                            value = shippingCost,
                            onValueChange = onShippingCostChange,
                            label = "Shipping",
                            placeholder = "70",
                            isOptional = true,
                            modifier = Modifier.weight(1f),
                            testTag = "calc_shipping_cost"
                        )

                        CurrencyInputField(
                            value = paymentFee,
                            onValueChange = onPaymentFeeChange,
                            label = "Payment/COD Fee",
                            placeholder = "20",
                            isOptional = true,
                            modifier = Modifier.weight(1f),
                            testTag = "calc_payment_fee"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Advertising Cost
                    CurrencyInputField(
                        value = adCost,
                        onValueChange = onAdCostChange,
                        label = "Advertising Cost",
                        placeholder = "100",
                        isOptional = true,
                        testTag = "calc_ad_cost"
                    )
                }
            }
        }

        // STEP 3: Order Status (Delivered vs RTO / Returned)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Order Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIsRtoChange(false) }
                            .padding(vertical = 4.dp)
                            .testTag("status_delivered_option")
                    ) {
                        RadioButton(
                            selected = !isRto,
                            onClick = { onIsRtoChange(false) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Delivered",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (!isRto) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isRto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customer received order and paid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIsRtoChange(true) }
                            .padding(vertical = 4.dp)
                            .testTag("status_rto_option")
                    ) {
                        RadioButton(
                            selected = isRto,
                            onClick = { onIsRtoChange(true) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "RTO / Returned",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isRto) FontWeight.Bold else FontWeight.Normal,
                                color = if (isRto) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Order returned or cancelled by customer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // RTO Extra options
                    if (isRto) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        CurrencyInputField(
                            value = rtoLoss,
                            onValueChange = onRtoLossChange,
                            label = "Additional Loss",
                            placeholder = "50",
                            isOptional = true,
                            testTag = "input_rto_loss"
                        )
                        Text(
                            text = "e.g. Reverse courier fee or damaged packaging",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = productReturnedToStock,
                                onCheckedChange = onProductReturnedToStockChange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Product returned safely to stock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (productReturnedToStock) "Product cost is NOT lost (item is back in inventory)" else "Product was damaged/lost (Product cost will be counted as loss)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // STEP 4: Itemized Calculation Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calculation Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CalcRow("Selling Price", ProfitCalculator.formatRupees(breakdown.sellingPrice))
                    if (breakdown.discount > 0) {
                        CalcRow("− Discount", "−" + ProfitCalculator.formatRupees(breakdown.discount), isSub = true)
                    }
                    if (!breakdown.isRto) {
                        CalcRow("− Product Cost", "−" + ProfitCalculator.formatRupees(breakdown.productCost), isSub = true)
                    } else if (!breakdown.productReturnedToStock) {
                        CalcRow("− Damaged Product Cost", "−" + ProfitCalculator.formatRupees(breakdown.productCost), isSub = true)
                    } else {
                        CalcRow("Product Cost", "₹0 (Returned to stock)", isSub = false)
                    }
                    if (breakdown.packagingCost > 0) {
                        CalcRow("− Packaging", "−" + ProfitCalculator.formatRupees(breakdown.packagingCost), isSub = true)
                    }
                    if (breakdown.shippingCost > 0) {
                        CalcRow("− Shipping", "−" + ProfitCalculator.formatRupees(breakdown.shippingCost), isSub = true)
                    }
                    if (breakdown.paymentFee > 0) {
                        CalcRow("− Payment Fee", "−" + ProfitCalculator.formatRupees(breakdown.paymentFee), isSub = true)
                    }
                    if (breakdown.adCost > 0) {
                        CalcRow("− Advertising Cost", "−" + ProfitCalculator.formatRupees(breakdown.adCost), isSub = true)
                    }
                    if (breakdown.isRto && breakdown.rtoLoss > 0) {
                        CalcRow("− Additional RTO Loss", "−" + ProfitCalculator.formatRupees(breakdown.rtoLoss), isSub = true)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (breakdown.netProfit >= 0) "ACTUAL PROFIT" else "YOU ARE LOSING MONEY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (breakdown.netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = ProfitCalculator.formatRupees(breakdown.netProfit),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (breakdown.netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Save Order Button
        item {
            Button(
                onClick = onSaveOrder,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (breakdown.netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_save_order")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save as Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DominantProfitCard(breakdown: CalculationBreakdown) {
    val isLoss = breakdown.isLoss || breakdown.netProfit < 0
    val containerColor = if (isLoss) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (isLoss) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dominant_profit_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            if (isLoss) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Loss Warning",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YOU ARE LOSING MONEY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor,
                        letterSpacing = 1.2.sp
                    )
                }
            } else {
                Text(
                    text = "ACTUAL PROFIT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visually dominant final profit number
            Text(
                text = ProfitCalculator.formatRupees(breakdown.netProfit),
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("dominant_profit_number")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Margin Badge
            Surface(
                shape = CircleShape,
                color = if (isLoss) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (breakdown.isRto) {
                        "RTO / Returned Order"
                    } else {
                        "${ProfitCalculator.formatMargin(breakdown.profitMargin)} margin"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun CalcRow(
    label: String,
    value: String,
    isSub: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isSub) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IconButtonWithLabel(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text)
    }
}

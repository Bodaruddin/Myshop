package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.Category
import com.example.data.entity.Product
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

@Composable
fun AddEditProductDialog(
    initialProduct: Product? = null,
    categories: List<Category>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var modelNumber by remember { mutableStateOf(initialProduct?.modelNumber ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: categories.firstOrNull()?.name ?: "Mobile Phones") }
    var barcode by remember {
        mutableStateOf(
            initialProduct?.barcode ?: "890${Random.nextLong(100000000L, 999999999L)}"
        )
    }
    var purchasePriceStr by remember { mutableStateOf(initialProduct?.purchasePrice?.toString() ?: "") }
    var sellingPriceStr by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "") }
    var stockQuantityStr by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10") }
    var minStockStr by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "3") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }

    val purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val profitMargin = sellingPrice - purchasePrice
    val profitPercent = if (purchasePrice > 0) (profitMargin / purchasePrice) * 100 else 0.0

    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialProduct == null) "Add New Product" else "Edit Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Name
                GlassInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Product Name *",
                    placeholder = "e.g. Sony WH-1000XM5"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text(
                    text = "Category",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        LiquidGlassChip(
                            text = cat.name,
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            icon = getCategoryIcon(cat.iconName)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand & Model
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassInputField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = "Brand",
                        placeholder = "e.g. Sony",
                        modifier = Modifier.weight(1f)
                    )
                    GlassInputField(
                        value = modelNumber,
                        onValueChange = { modelNumber = it },
                        label = "Model #",
                        placeholder = "e.g. WH1000XM5",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barcode input with auto-generate button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassInputField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = "Barcode / Serial",
                        placeholder = "890...",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            barcode = "890${Random.nextLong(100000000L, 999999999L)}"
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Generate Barcode",
                            tint = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pricing Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassInputField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = "Purchase Price ($currencySymbol) *",
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    GlassInputField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = "Selling Price ($currencySymbol) *",
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Profit preview chip
                if (sellingPrice > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (profitMargin >= 0) EmeraldMint.copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estimated Profit Margin:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${formatter.format(profitMargin)} (${String.format(Locale.US, "%.1f", profitPercent)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profitMargin >= 0) EmeraldMint else Color(0xFFEF4444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stock & Alert
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassInputField(
                        value = stockQuantityStr,
                        onValueChange = { stockQuantityStr = it },
                        label = "Initial Stock *",
                        placeholder = "10",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    GlassInputField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = "Min Stock Alert",
                        placeholder = "3",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                GlassInputField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description / Specs",
                    placeholder = "Color, storage, warranty details...",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                LiquidGlassButton(
                    text = if (initialProduct == null) "Create Product" else "Update Product",
                    onClick = {
                        if (name.isNotBlank()) {
                            val prod = (initialProduct ?: Product(
                                name = name,
                                category = selectedCategory,
                                brand = brand,
                                modelNumber = modelNumber,
                                barcode = barcode,
                                purchasePrice = purchasePrice,
                                sellingPrice = sellingPrice,
                                stockQuantity = stockQuantityStr.toIntOrNull() ?: 0,
                                minStockAlert = minStockStr.toIntOrNull() ?: 3,
                                description = description,
                                iconName = categories.firstOrNull { it.name == selectedCategory }?.iconName ?: "devices"
                            )).copy(
                                name = name,
                                category = selectedCategory,
                                brand = brand,
                                modelNumber = modelNumber,
                                barcode = barcode,
                                purchasePrice = purchasePrice,
                                sellingPrice = sellingPrice,
                                stockQuantity = stockQuantityStr.toIntOrNull() ?: 0,
                                minStockAlert = minStockStr.toIntOrNull() ?: 3,
                                description = description,
                                iconName = categories.firstOrNull { it.name == selectedCategory }?.iconName ?: "devices",
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(prod)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && sellingPrice > 0
                )
            }
        }
    }
}

@Composable
fun StockActionDialog(
    product: Product,
    actionType: String, // "STOCK_IN", "STOCK_OUT", "ADJUSTMENT"
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, cost: Double, notes: String, supplierOrReason: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(if (actionType == "ADJUSTMENT") product.stockQuantity else 5) }
    var costStr by remember { mutableStateOf(product.purchasePrice.toString()) }
    var notes by remember { mutableStateOf("") }
    var sourceOrReason by remember {
        mutableStateOf(
            when (actionType) {
                "STOCK_IN" -> "Authorized Supplier"
                "STOCK_OUT" -> "Damaged / Demo"
                else -> "Physical Inventory Audit"
            }
        )
    }

    val title = when (actionType) {
        "STOCK_IN" -> "Add Stock (Restock)"
        "STOCK_OUT" -> "Reduce Stock"
        else -> "Stock Adjustment"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan,
                    fontSize = 14.sp
                )
                Text(
                    text = "Current Stock: ${product.stockQuantity} units",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Counter
                Text(
                    text = if (actionType == "ADJUSTMENT") "New Actual Physical Stock:" else "Quantity to ${if (actionType == "STOCK_IN") "Add" else "Deduct"}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { if (quantity > 0) quantity-- },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (actionType == "STOCK_IN") {
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassInputField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = "Cost Per Unit ($currencySymbol)",
                        placeholder = "Purchase price",
                        keyboardType = KeyboardType.Decimal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassInputField(
                        value = sourceOrReason,
                        onValueChange = { sourceOrReason = it },
                        label = "Supplier / Source",
                        placeholder = "e.g. Ingram Micro"
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassInputField(
                        value = sourceOrReason,
                        onValueChange = { sourceOrReason = it },
                        label = "Reason",
                        placeholder = "e.g. Discrepancy / Damaged / Display Unit"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                GlassInputField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    placeholder = "Invoice #, PO reference, batch..."
                )

                Spacer(modifier = Modifier.height(20.dp))
                LiquidGlassButton(
                    text = "Confirm & Update Stock",
                    onClick = {
                        onConfirm(quantity, costStr.toDoubleOrNull() ?: product.purchasePrice, notes, sourceOrReason)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (actionType == "ADJUSTMENT" && quantity >= 0) || (actionType != "ADJUSTMENT" && quantity > 0)
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("devices") }

    val iconOptions = listOf(
        "smartphone" to "Mobile",
        "battery_charging" to "Charger",
        "headphones" to "Audio",
        "speaker" to "Speaker",
        "watch" to "Watch",
        "cable" to "Cable",
        "devices" to "Gadgets"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Custom Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                GlassInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Category Name",
                    placeholder = "e.g. Smart Home, Drones"
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Choose Icon",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconOptions) { (iconKey, label) ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { selectedIcon = iconKey }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(iconKey),
                                contentDescription = label,
                                tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LiquidGlassButton(
                    text = "Add Category",
                    onClick = { if (name.isNotBlank()) onSave(name, selectedIcon, "#00F2FE") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank()
                )
            }
        }
    }
}

@Composable
fun GlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            cursorColor = NeonCyan
        ),
        modifier = modifier.fillMaxWidth()
    )
}

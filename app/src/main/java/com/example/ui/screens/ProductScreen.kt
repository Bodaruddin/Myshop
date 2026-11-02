package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.Category
import com.example.data.entity.Product
import com.example.data.entity.ShopProfile
import com.example.ui.components.BadgeType
import com.example.ui.components.BarcodeView
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassSearchBar
import com.example.ui.components.QrCodePreview
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductScreen(
    products: List<Product>,
    categories: List<Category>,
    profile: ShopProfile?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    stockFilter: String,
    onStockFilterChange: (String) -> Unit,
    onOpenAddProduct: () -> Unit,
    onOpenAddCategory: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onQuickStockAction: (Product) -> Unit
) {
    val currency = profile?.currencySymbol ?: "₹"
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))

    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productForBarcodeModal by remember { mutableStateOf<Product?>(null) }

    // Filter products
    val filteredProducts = products.filter { prod ->
        val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
        val matchesStock = when (stockFilter) {
            "Low Stock" -> prod.isLowStock
            "Out of Stock" -> prod.isOutOfStock
            else -> true
        }
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
                prod.name.lowercase().contains(q) ||
                prod.brand.lowercase().contains(q) ||
                prod.modelNumber.lowercase().contains(q) ||
                prod.barcode.lowercase().contains(q)

        matchesCategory && matchesStock && matchesQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Search Bar
                LiquidGlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchChange,
                    placeholder = "Search name, brand, model or barcode..."
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        LiquidGlassChip(
                            text = "All Categories",
                            selected = selectedCategory == "All",
                            onClick = { onCategoryChange("All") }
                        )
                    }
                    items(categories) { cat ->
                        LiquidGlassChip(
                            text = cat.name,
                            selected = selectedCategory.equals(cat.name, ignoreCase = true),
                            onClick = { onCategoryChange(cat.name) },
                            icon = getCategoryIcon(cat.iconName)
                        )
                    }
                    item {
                        LiquidGlassChip(
                            text = "+ New Category",
                            selected = false,
                            onClick = onOpenAddCategory,
                            icon = Icons.Default.Add,
                            accentColor = EmeraldMint
                        )
                    }
                }
            }

            // Quick Stock Status Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Low Stock", "Out of Stock").forEach { filter ->
                        val count = when (filter) {
                            "Low Stock" -> products.count { it.isLowStock }
                            "Out of Stock" -> products.count { it.isOutOfStock }
                            else -> products.size
                        }
                        LiquidGlassChip(
                            text = "$filter ($count)",
                            selected = stockFilter == filter,
                            onClick = { onStockFilterChange(filter) },
                            accentColor = if (filter == "Out of Stock") CoralFlame else if (filter == "Low Stock") AmberWarning else NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Products (${filteredProducts.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+ Add Product",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.clickable { onOpenAddProduct() }
                    )
                }
            }

            // Product List
            if (filteredProducts.isEmpty()) {
                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No products found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try modifying your search or filter criteria",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductItemCard(
                        product = product,
                        currency = currency,
                        formatter = formatter,
                        onEdit = { onEditProduct(product) },
                        onDelete = { productToDelete = product },
                        onStockAction = { onQuickStockAction(product) },
                        onShowBarcode = { productForBarcodeModal = product }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${prod.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(prod)
                        productToDelete = null
                    }
                ) {
                    Text("Delete", color = CoralFlame, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Barcode / QR Code Modal
    if (productForBarcodeModal != null) {
        val prod = productForBarcodeModal!!
        Dialog(onDismissRequest = { productForBarcodeModal = null }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(0.92f),
                accentGlow = NeonCyan
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = prod.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Model: ${prod.modelNumber.ifBlank { "N/A" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    QrCodePreview(
                        content = "PROD:${prod.id}|${prod.name}|${prod.barcode}|PRICE:${prod.sellingPrice}",
                        sizeDp = 140
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    BarcodeView(
                        code = prod.barcode,
                        modifier = Modifier.fillMaxWidth(),
                        lightBackground = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    LiquidGlassButton(
                        text = "Close",
                        onClick = { productForBarcodeModal = null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    currency: String,
    formatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockAction: () -> Unit,
    onShowBarcode: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = if (product.isOutOfStock) CoralFlame else if (product.isLowStock) AmberWarning else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon, Name, Category & Stock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(product.iconName),
                        contentDescription = product.name,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = listOfNotNull(
                            product.brand.ifBlank { null },
                            product.modelNumber.ifBlank { null },
                            product.category
                        ).joinToString(" • "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock Badge
                if (product.isOutOfStock) {
                    LiquidGlassBadge(text = "Out of Stock", type = BadgeType.ERROR)
                } else if (product.isLowStock) {
                    LiquidGlassBadge(text = "Low: ${product.stockQuantity}", type = BadgeType.WARNING)
                } else {
                    LiquidGlassBadge(text = "${product.stockQuantity} in stock", type = BadgeType.SUCCESS)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing & Profit breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Selling Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$currency${formatter.format(product.sellingPrice)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = NeonCyan
                    )
                }

                Column {
                    Text(text = "Cost Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$currency${formatter.format(product.purchasePrice)}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Profit Margin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "+$currency${formatter.format(product.profitMarginPerUnit)} (${String.format(Locale.US, "%.0f", product.profitMarginPercent)}%)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (product.profitMarginPerUnit >= 0) EmeraldMint else CoralFlame
                    )
                }
            }

            if (product.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barcode Quick Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onShowBarcode() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Barcode",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.barcode.takeLast(6),
                        fontSize = 11.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Stock In button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberWarning.copy(alpha = 0.15f))
                            .clickable { onStockAction() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ Stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Edit button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralFlame.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.entity.Product
import com.example.data.entity.ShopProfile
import com.example.data.entity.StockTransaction
import com.example.ui.components.BadgeType
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.StockActionDialog
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockScreen(
    products: List<Product>,
    transactions: List<StockTransaction>,
    profile: ShopProfile?,
    onAddStock: (productId: Long, qty: Int, cost: Double, notes: String, supplier: String) -> Unit,
    onReduceStock: (productId: Long, qty: Int, notes: String, reason: String) -> Unit,
    onAdjustStock: (productId: Long, newPhysicalQuantity: Int, reason: String) -> Unit
) {
    val currency = profile?.currencySymbol ?: "₹"
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    var selectedTransactionFilter by remember { mutableStateOf("ALL") }
    var activeStockActionProduct by remember { mutableStateOf<Product?>(null) }
    var activeStockActionType by remember { mutableStateOf("STOCK_IN") }

    val lowStockCount = products.count { it.isLowStock }
    val outOfStockCount = products.count { it.isOutOfStock }
    val totalStockUnits = products.sumOf { it.stockQuantity }
    val totalStockValuation = products.sumOf { it.purchasePrice * it.stockQuantity }

    val filteredTransactions = transactions.filter { tx ->
        when (selectedTransactionFilter) {
            "STOCK_IN" -> tx.type == "STOCK_IN"
            "STOCK_OUT" -> tx.type == "STOCK_OUT"
            "ADJUSTMENT" -> tx.type == "ADJUSTMENT"
            "SALE" -> tx.type == "SALE"
            else -> true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Stock Summary Banner
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = ElectricBlue,
                borderColor = ElectricBlue.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Warehouse & Inventory Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalStockUnits Units",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Valuation: $currency${formatter.format(totalStockValuation)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (lowStockCount > 0) {
                                LiquidGlassBadge(text = "$lowStockCount Low", type = BadgeType.WARNING)
                            }
                            if (outOfStockCount > 0) {
                                LiquidGlassBadge(text = "$outOfStockCount Out", type = BadgeType.ERROR)
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StockActionButton(
                    label = "Stock In (+)",
                    icon = Icons.Default.Add,
                    color = EmeraldMint,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeStockActionType = "STOCK_IN"
                        activeStockActionProduct = products.firstOrNull()
                    }
                )
                StockActionButton(
                    label = "Reduce (-)",
                    icon = Icons.Default.Remove,
                    color = CoralFlame,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeStockActionType = "STOCK_OUT"
                        activeStockActionProduct = products.firstOrNull()
                    }
                )
                StockActionButton(
                    label = "Adjust (=)",
                    icon = Icons.Default.Tune,
                    color = AmberWarning,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeStockActionType = "ADJUSTMENT"
                        activeStockActionProduct = products.firstOrNull()
                    }
                )
            }
        }

        // Section: Critical Reorder Alert Products
        val alertProducts = products.filter { it.isOutOfStock || it.isLowStock }
        if (alertProducts.isNotEmpty()) {
            item {
                Text(
                    text = "Requires Reorder (${alertProducts.size} Items)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning
                )
            }

            items(alertProducts) { product ->
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (product.isOutOfStock) CoralFlame.copy(alpha = 0.5f) else AmberWarning.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (product.isOutOfStock) CoralFlame.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (product.isOutOfStock) Icons.Default.Warning else Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = if (product.isOutOfStock) CoralFlame else AmberWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Current: ${product.stockQuantity} | Min Threshold: ${product.minStockAlert}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .clickable {
                                    activeStockActionType = "STOCK_IN"
                                    activeStockActionProduct = product
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Restock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Section: Audit Trail Filter & Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock Movement History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredTransactions.size} records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("ALL" to "All Logs", "STOCK_IN" to "Restock In", "STOCK_OUT" to "Stock Out", "ADJUSTMENT" to "Adjustments", "SALE" to "Sales")) { (key, label) ->
                    LiquidGlassChip(
                        text = label,
                        selected = selectedTransactionFilter == key,
                        onClick = { selectedTransactionFilter = key }
                    )
                }
            }
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No stock transactions found for this filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val (badgeType, icon) = when (tx.type) {
                                "STOCK_IN" -> BadgeType.SUCCESS to Icons.Default.ArrowUpward
                                "STOCK_OUT" -> BadgeType.ERROR to Icons.Default.ArrowDownward
                                "ADJUSTMENT" -> BadgeType.WARNING to Icons.Default.Tune
                                else -> BadgeType.CYAN to Icons.Default.History
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (tx.type) {
                                            "STOCK_IN" -> EmeraldMint.copy(alpha = 0.15f)
                                            "STOCK_OUT" -> CoralFlame.copy(alpha = 0.15f)
                                            "ADJUSTMENT" -> AmberWarning.copy(alpha = 0.15f)
                                            else -> NeonCyan.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tx.type,
                                    tint = when (tx.type) {
                                        "STOCK_IN" -> EmeraldMint
                                        "STOCK_OUT" -> CoralFlame
                                        "ADJUSTMENT" -> AmberWarning
                                        else -> NeonCyan
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = listOfNotNull(
                                        tx.supplierOrSource.ifBlank { null },
                                        tx.notes.ifBlank { null },
                                        dateFormat.format(Date(tx.timestamp))
                                    ).joinToString(" • "),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (tx.quantity > 0) "+${tx.quantity}" else "${tx.quantity}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (tx.quantity > 0) EmeraldMint else CoralFlame
                            )
                            if (tx.costPerUnit > 0) {
                                Text(
                                    text = "@$currency${formatter.format(tx.costPerUnit)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Stock Action Dialog
    if (activeStockActionProduct != null) {
        StockActionDialog(
            product = activeStockActionProduct!!,
            actionType = activeStockActionType,
            currencySymbol = currency,
            onDismiss = { activeStockActionProduct = null },
            onConfirm = { qty, cost, notes, supplierOrReason ->
                when (activeStockActionType) {
                    "STOCK_IN" -> onAddStock(activeStockActionProduct!!.id, qty, cost, notes, supplierOrReason)
                    "STOCK_OUT" -> onReduceStock(activeStockActionProduct!!.id, qty, notes, supplierOrReason)
                    "ADJUSTMENT" -> onAdjustStock(activeStockActionProduct!!.id, qty, supplierOrReason)
                }
                activeStockActionProduct = null
            }
        )
    }
}

@Composable
fun StockActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        accentGlow = color,
        borderColor = color.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

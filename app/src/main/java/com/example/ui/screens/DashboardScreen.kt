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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Product
import com.example.data.entity.Sale
import com.example.data.entity.ShopProfile
import com.example.data.entity.StockTransaction
import com.example.ui.components.BadgeType
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassStatCard
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    products: List<Product>,
    sales: List<Sale>,
    transactions: List<StockTransaction>,
    profile: ShopProfile?,
    onNavigateToSales: () -> Unit,
    onNavigateToProducts: (filter: String?) -> Unit,
    onNavigateToStock: () -> Unit,
    onOpenAddProduct: () -> Unit,
    onViewSaleInvoice: (Sale) -> Unit
) {
    val currency = profile?.currencySymbol ?: "₹"
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    // Calculations
    val totalProducts = products.size
    val totalStockValue = products.sumOf { it.purchasePrice * it.stockQuantity }
    val lowStockProducts = products.filter { it.isLowStock }
    val outOfStockProducts = products.filter { it.isOutOfStock }

    val todayCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = todayCalendar.timeInMillis

    val todaySalesList = sales.filter { it.timestamp >= startOfToday }
    val todaySalesAmount = todaySalesList.sumOf { it.totalAmount }
    val totalProfitAmount = sales.sumOf { it.totalProfit }

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Shop Welcome Header Banner
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = NeonCyan,
                borderColor = NeonCyan.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldMint)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% OFFLINE DATABASE ACTIVE",
                                color = EmeraldMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile?.shopName ?: "VoltTech Electronics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = profile?.tagline ?: "Premium Gadgets Hub",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.2f), ElectricBlue.copy(alpha = 0.3f)))
                            )
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Shop",
                            tint = NeonCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "New Sale",
                    subtitle = "POS Checkout",
                    icon = Icons.Default.PointOfSale,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSales
                )
                QuickActionButton(
                    title = "Add Product",
                    subtitle = "Inventory",
                    icon = Icons.Default.Add,
                    accentColor = EmeraldMint,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAddProduct
                )
                QuickActionButton(
                    title = "Stock Action",
                    subtitle = "In / Adjust",
                    icon = Icons.Default.Inventory2,
                    accentColor = AmberWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStock
                )
            }
        }

        // Alert Banner if low or out of stock
        if (lowStockProducts.isNotEmpty() || outOfStockProducts.isNotEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AmberWarning.copy(alpha = 0.1f),
                    borderColor = AmberWarning.copy(alpha = 0.5f),
                    accentGlow = AmberWarning,
                    onClick = { onNavigateToProducts("Low Stock") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Inventory Attention Needed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${lowStockProducts.size} low stock, ${outOfStockProducts.size} out of stock products.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View",
                            tint = AmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Key Metrics
        item {
            Text(
                text = "Financial & Inventory Overview",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 2x2 Grid of Stat Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassStatCard(
                        title = "Today's Sales",
                        value = "$currency${formatter.format(todaySalesAmount)}",
                        subtitle = "${todaySalesList.size} bills today",
                        icon = Icons.Default.CurrencyRupee,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassStatCard(
                        title = "Total Profit",
                        value = "$currency${formatter.format(totalProfitAmount)}",
                        subtitle = "Net margin",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldMint,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassStatCard(
                        title = "Stock Value",
                        value = "$currency${formatter.format(totalStockValue)}",
                        subtitle = "$totalProducts items in catalog",
                        icon = Icons.Default.Inventory2,
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToProducts(null) }
                    )
                    LiquidGlassStatCard(
                        title = "Stock Alerts",
                        value = "${lowStockProducts.size + outOfStockProducts.size}",
                        subtitle = "${outOfStockProducts.size} out of stock",
                        icon = Icons.Default.Warning,
                        accentColor = if (outOfStockProducts.isNotEmpty()) CoralFlame else AmberWarning,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToProducts("Low Stock") }
                    )
                }
            }
        }

        // Section Title: Recently Added Products
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Highlights",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All (${products.size})",
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToProducts(null) }
                )
            }
        }

        // Horizontal Carousel of Products
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(products.take(6)) { product ->
                    LiquidGlassCard(
                        modifier = Modifier.width(180.dp),
                        accentGlow = if (product.isOutOfStock) CoralFlame else NeonCyan
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NeonCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(product.iconName),
                                        contentDescription = product.name,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (product.isOutOfStock) {
                                    LiquidGlassBadge(text = "Out", type = BadgeType.ERROR)
                                } else if (product.isLowStock) {
                                    LiquidGlassBadge(text = "${product.stockQuantity} left", type = BadgeType.WARNING)
                                } else {
                                    LiquidGlassBadge(text = "${product.stockQuantity} in stock", type = BadgeType.SUCCESS)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${product.brand} • ${product.category}",
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$currency${formatter.format(product.sellingPrice)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Recent Sales Invoices
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sales Invoices",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Open POS",
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToSales() }
                )
            }
        }

        // Recent Invoices List
        if (sales.isEmpty()) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sales yet. Tap 'New Sale' to create your first invoice!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(sales.take(4)) { sale ->
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onViewSaleInvoice(sale) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldMint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Invoice",
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = sale.invoiceNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${sale.customerName} • ${dateFormat.format(Date(sale.timestamp))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currency${formatter.format(sale.totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NeonCyan
                            )
                            Text(
                                text = sale.paymentMethod,
                                fontSize = 11.sp,
                                color = EmeraldMint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Recent Stock Activity Log
        item {
            Text(
                text = "Recent Stock Audit Log",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(transactions.take(5)) { tx ->
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeType = when (tx.type) {
                                "STOCK_IN" -> BadgeType.SUCCESS
                                "STOCK_OUT" -> BadgeType.ERROR
                                "ADJUSTMENT" -> BadgeType.WARNING
                                else -> BadgeType.CYAN
                            }
                            LiquidGlassBadge(text = tx.type.replace("_", " "), type = badgeType)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tx.productName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (tx.notes.isNotBlank() || tx.supplierOrSource.isNotBlank()) {
                            Text(
                                text = listOfNotNull(tx.supplierOrSource.ifBlank { null }, tx.notes.ifBlank { null }).joinToString(" • "),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (tx.quantity > 0) "+${tx.quantity}" else "${tx.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (tx.quantity >= 0) EmeraldMint else CoralFlame
                        )
                        Text(
                            text = dateFormat.format(Date(tx.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        accentGlow = accentColor,
        borderColor = accentColor.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

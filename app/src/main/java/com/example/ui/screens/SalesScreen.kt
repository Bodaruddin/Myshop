package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.entity.Sale
import com.example.data.entity.ShopProfile
import com.example.data.repository.ShopRepository
import com.example.ui.components.BadgeType
import com.example.ui.components.GlassInputField
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassSearchBar
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(
    products: List<Product>,
    categories: List<Category>,
    sales: List<Sale>,
    cartItems: List<ShopRepository.CartItem>,
    profile: ShopProfile?,
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    customerPhone: String,
    onCustomerPhoneChange: (String) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    discountAmount: Double,
    onDiscountChange: (Double) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onUpdateCartQuantity: (Product, Int) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onViewInvoice: (Sale) -> Unit
) {
    val currency = profile?.currencySymbol ?: "₹"
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    var selectedTab by remember { mutableStateOf(0) } // 0: POS Checkout, 1: Sales History
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCheckoutModal by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }
    val totalCost = cartItems.sumOf { it.product.purchasePrice * it.quantity }
    val grandTotal = (subtotal - discountAmount).coerceAtLeast(0.0)
    val estimatedProfit = grandTotal - totalCost
    val totalCartQuantity = cartItems.sumOf { it.quantity }

    val filteredProducts = products.filter { prod ->
        val matchesCat = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
                prod.name.lowercase().contains(q) ||
                prod.brand.lowercase().contains(q) ||
                prod.modelNumber.lowercase().contains(q) ||
                prod.barcode.lowercase().contains(q)
        matchesCat && matchesQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mode Selector Tabs (New Sale POS vs Sales History)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassChip(
                    text = "New Sale / POS",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = Icons.Default.PointOfSale,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassChip(
                    text = "Sales Invoices (${sales.size})",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = Icons.Default.History,
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTab == 0) {
                // POS SCREEN
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        LiquidGlassSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Quick scan or type product name/model..."
                        )
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                LiquidGlassChip(
                                    text = "All",
                                    selected = selectedCategory == "All",
                                    onClick = { selectedCategory = "All" }
                                )
                            }
                            items(categories) { cat ->
                                LiquidGlassChip(
                                    text = cat.name,
                                    selected = selectedCategory.equals(cat.name, ignoreCase = true),
                                    onClick = { selectedCategory = cat.name },
                                    icon = getCategoryIcon(cat.iconName)
                                )
                            }
                        }
                    }

                    // Product Grid/List for POS
                    items(filteredProducts, key = { it.id }) { product ->
                        val cartItem = cartItems.find { it.product.id == product.id }
                        val inCartQty = cartItem?.quantity ?: 0

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (inCartQty > 0) NeonCyan.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            accentGlow = if (inCartQty > 0) NeonCyan else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NeonCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(product.iconName),
                                            contentDescription = product.name,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(22.dp)
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
                                            text = "${product.brand} • In Stock: ${product.stockQuantity}",
                                            fontSize = 11.sp,
                                            color = if (product.isOutOfStock) CoralFlame else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$currency${formatter.format(product.sellingPrice)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = NeonCyan
                                        )
                                    }
                                }

                                // Cart counter (+ / -)
                                if (product.isOutOfStock) {
                                    LiquidGlassBadge(text = "Out of Stock", type = BadgeType.ERROR)
                                } else if (inCartQty > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onRemoveFromCart(product) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = NeonCyan)
                                        }
                                        Text(
                                            text = "$inCartQty",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NeonCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { onAddToCart(product) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = NeonCyan)
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NeonCyan)
                                            .clickable { onAddToCart(product) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "+ Add",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            } else {
                // SALES HISTORY SCREEN
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (sales.isEmpty()) {
                        item {
                            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No sales recorded yet.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(sales, key = { it.id }) { sale ->
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onViewInvoice(sale) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                                    imageVector = Icons.Default.Receipt,
                                                    contentDescription = "Invoice",
                                                    tint = EmeraldMint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = sale.invoiceNumber,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = dateFormat.format(Date(sale.timestamp)),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$currency${formatter.format(sale.totalAmount)}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = NeonCyan
                                            )
                                            LiquidGlassBadge(
                                                text = sale.paymentMethod,
                                                type = BadgeType.SUCCESS
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Customer: ${sale.customerName} (${sale.itemCount} items)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Profit: +$currency${formatter.format(sale.totalProfit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldMint
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Bottom Cart Checkout Bar (only in POS tab when cart has items)
        if (selectedTab == 0 && cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 72.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF0F172A),
                    borderColor = NeonCyan,
                    accentGlow = NeonCyan,
                    onClick = { showCheckoutModal = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$totalCartQuantity Items in Cart",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to review & bill",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$currency${formatter.format(grandTotal)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Checkout",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Checkout Modal Dialog
    if (showCheckoutModal) {
        Dialog(
            onDismissRequest = { showCheckoutModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
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
                            text = "Checkout & Invoice",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showCheckoutModal = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Customer details
                    GlassInputField(
                        value = customerName,
                        onValueChange = onCustomerNameChange,
                        label = "Customer Name",
                        placeholder = "e.g. Rahul Sharma"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassInputField(
                        value = customerPhone,
                        onValueChange = onCustomerPhoneChange,
                        label = "Customer Phone",
                        placeholder = "+91 98765 43210",
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Method selector
                    Text(
                        text = "Payment Method",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Cash", "UPI / QR", "Card", "Credit / Due")) { method ->
                            LiquidGlassChip(
                                text = method,
                                selected = paymentMethod == method,
                                onClick = { onPaymentMethodChange(method) },
                                icon = when (method) {
                                    "Cash" -> Icons.Default.Money
                                    "UPI / QR" -> Icons.Default.QrCode
                                    "Card" -> Icons.Default.CreditCard
                                    else -> Icons.Default.Payment
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cart Items breakdown
                    Text(
                        text = "Itemized Products (${cartItems.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    for (item in cartItems) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@$currency${formatter.format(item.product.sellingPrice)} x ${item.quantity}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$currency${formatter.format(item.product.sellingPrice * item.quantity)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Discount row
                    var discountInputStr by remember { mutableStateOf(if (discountAmount > 0) discountAmount.toString() else "") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassInputField(
                            value = discountInputStr,
                            onValueChange = {
                                discountInputStr = it
                                onDiscountChange(it.toDoubleOrNull() ?: 0.0)
                            },
                            label = "Discount ($currency)",
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val disc = (subtotal * 0.05).toInt().toDouble()
                                    discountInputStr = disc.toString()
                                    onDiscountChange(disc)
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Text("5% Off", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Final Amount & Profit Summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency${formatter.format(subtotal)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount Applied:", fontSize = 12.sp, color = CoralFlame)
                                Text("-$currency${formatter.format(discountAmount)}", fontSize = 12.sp, color = CoralFlame, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Net Profit:", fontSize = 12.sp, color = EmeraldMint)
                            Text("+$currency${formatter.format(estimatedProfit)}", fontSize = 12.sp, color = EmeraldMint, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable:", fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text("$currency${formatter.format(grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassButton(
                        text = "Generate Invoice & Complete Sale",
                        icon = Icons.Default.Check,
                        onClick = {
                            showCheckoutModal = false
                            onCheckout()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

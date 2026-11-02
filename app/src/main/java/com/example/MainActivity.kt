package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.Product
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.InvoiceDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProductScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockScreen
import com.example.ui.theme.DarkGlassBackgroundGradient
import com.example.ui.theme.LightGlassBackgroundGradient
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ShopViewModel = viewModel()
            val profile by viewModel.shopProfile.collectAsStateWithLifecycle()

            var isDarkTheme by remember { mutableStateOf(true) }

            // Sync with profile setting if set
            LaunchedEffect(profile) {
                profile?.let {
                    if (it.themeMode == "LIGHT") {
                        isDarkTheme = false
                    } else if (it.themeMode == "DARK") {
                        isDarkTheme = true
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                ElectroGlassApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = it }
                )
            }
        }
    }
}

@Composable
fun ElectroGlassApp(
    viewModel: ShopViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }

    val products by viewModel.products.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleItems by viewModel.allSaleItems.collectAsStateWithLifecycle()
    val profile by viewModel.shopProfile.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val stockFilter by viewModel.stockFilter.collectAsStateWithLifecycle()
    val reportTimeframe by viewModel.reportTimeframe.collectAsStateWithLifecycle()

    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartCustomerName by viewModel.cartCustomerName.collectAsStateWithLifecycle()
    val cartCustomerPhone by viewModel.cartCustomerPhone.collectAsStateWithLifecycle()
    val cartPaymentMethod by viewModel.cartPaymentMethod.collectAsStateWithLifecycle()
    val cartDiscount by viewModel.cartDiscount.collectAsStateWithLifecycle()
    val cartNotes by viewModel.cartNotes.collectAsStateWithLifecycle()

    val activeInvoiceSale by viewModel.activeInvoiceSale.collectAsStateWithLifecycle()
    val activeInvoiceItems by viewModel.activeInvoiceItems.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showAddEditProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // BackHandler: If on non-dashboard tab, return to Dashboard
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    val backgroundBrush = if (isDarkTheme) DarkGlassBackgroundGradient else LightGlassBackgroundGradient

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Ambient neon liquid glass ambient lights
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Ambient cyan orb
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonCyan.copy(alpha = if (isDarkTheme) 0.12f else 0.08f), Color.Transparent),
                                center = Offset(size.width * 0.85f, size.height * 0.12f),
                                radius = size.width * 0.7f
                            )
                        )
                        // Ambient violet orb
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF8B5CF6).copy(alpha = if (isDarkTheme) 0.10f else 0.06f), Color.Transparent),
                                center = Offset(size.width * 0.1f, size.height * 0.65f),
                                radius = size.width * 0.75f
                            )
                        )
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // App Top Header Bar
                    ElectroGlassTopBar(
                        currentTab = currentTab,
                        cartCount = cartItems.sumOf { it.quantity },
                        onCartClick = { currentTab = 3 }
                    )

                    // Screen Content Container
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_transition"
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> DashboardScreen(
                                    products = products,
                                    sales = sales,
                                    transactions = transactions,
                                    profile = profile,
                                    onNavigateToSales = { currentTab = 3 },
                                    onNavigateToProducts = { filter ->
                                        if (filter != null) viewModel.stockFilter.value = filter
                                        currentTab = 1
                                    },
                                    onNavigateToStock = { currentTab = 2 },
                                    onOpenAddProduct = {
                                        productToEdit = null
                                        showAddEditProductDialog = true
                                    },
                                    onViewSaleInvoice = { sale -> viewModel.showInvoice(sale) }
                                )
                                1 -> ProductScreen(
                                    products = products,
                                    categories = categories,
                                    profile = profile,
                                    searchQuery = searchQuery,
                                    onSearchChange = { viewModel.searchQuery.value = it },
                                    selectedCategory = selectedCategory,
                                    onCategoryChange = { viewModel.selectedCategoryFilter.value = it },
                                    stockFilter = stockFilter,
                                    onStockFilterChange = { viewModel.stockFilter.value = it },
                                    onOpenAddProduct = {
                                        productToEdit = null
                                        showAddEditProductDialog = true
                                    },
                                    onOpenAddCategory = { showAddCategoryDialog = true },
                                    onEditProduct = { prod ->
                                        productToEdit = prod
                                        showAddEditProductDialog = true
                                    },
                                    onDeleteProduct = { prod -> viewModel.deleteProduct(prod) },
                                    onQuickStockAction = { prod ->
                                        currentTab = 2
                                    }
                                )
                                2 -> StockScreen(
                                    products = products,
                                    transactions = transactions,
                                    profile = profile,
                                    onAddStock = { id, qty, cost, notes, supp ->
                                        viewModel.addStock(id, qty, cost, notes, supp)
                                    },
                                    onReduceStock = { id, qty, notes, reason ->
                                        viewModel.reduceStock(id, qty, notes, reason)
                                    },
                                    onAdjustStock = { id, qty, reason ->
                                        viewModel.adjustStock(id, qty, reason)
                                    }
                                )
                                3 -> SalesScreen(
                                    products = products,
                                    categories = categories,
                                    sales = sales,
                                    cartItems = cartItems,
                                    profile = profile,
                                    customerName = cartCustomerName,
                                    onCustomerNameChange = { viewModel.cartCustomerName.value = it },
                                    customerPhone = cartCustomerPhone,
                                    onCustomerPhoneChange = { viewModel.cartCustomerPhone.value = it },
                                    paymentMethod = cartPaymentMethod,
                                    onPaymentMethodChange = { viewModel.cartPaymentMethod.value = it },
                                    discountAmount = cartDiscount,
                                    onDiscountChange = { viewModel.cartDiscount.value = it },
                                    notes = cartNotes,
                                    onNotesChange = { viewModel.cartNotes.value = it },
                                    onAddToCart = { prod -> viewModel.addToCart(prod) },
                                    onRemoveFromCart = { prod -> viewModel.removeFromCart(prod) },
                                    onUpdateCartQuantity = { prod, q -> viewModel.updateCartQuantity(prod, q) },
                                    onClearCart = { viewModel.clearCart() },
                                    onCheckout = { viewModel.completeSale() },
                                    onViewInvoice = { sale -> viewModel.showInvoice(sale) }
                                )
                                4 -> ReportsScreen(
                                    sales = sales,
                                    saleItems = saleItems,
                                    products = products,
                                    profile = profile,
                                    timeframe = reportTimeframe,
                                    onTimeframeChange = { viewModel.reportTimeframe.value = it }
                                )
                                5 -> SettingsScreen(
                                    profile = profile,
                                    currentDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = onToggleDarkTheme,
                                    onSaveProfile = { prof -> viewModel.updateProfile(prof) },
                                    onExportBackup = { callback -> viewModel.exportBackup(callback) },
                                    onImportBackup = { json -> viewModel.importBackup(json) },
                                    onResetData = { viewModel.resetData() }
                                )
                            }
                        }
                    }

                    // Liquid Glass Bottom Navigation Bar
                    LiquidGlassBottomNavBar(
                        selectedTab = currentTab,
                        onTabSelected = { currentTab = it },
                        cartCount = cartItems.sumOf { it.quantity }
                    )
                }
            }
        }
    }

    // Modal Invoices & Forms
    if (activeInvoiceSale != null) {
        InvoiceDialog(
            sale = activeInvoiceSale!!,
            items = activeInvoiceItems,
            profile = profile,
            onDismiss = { viewModel.dismissInvoice() }
        )
    }

    if (showAddEditProductDialog) {
        AddEditProductDialog(
            initialProduct = productToEdit,
            categories = categories,
            currencySymbol = profile?.currencySymbol ?: "₹",
            onDismiss = {
                showAddEditProductDialog = false
                productToEdit = null
            },
            onSave = { prod ->
                viewModel.saveProduct(prod)
                showAddEditProductDialog = false
                productToEdit = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, icon, color ->
                viewModel.addCategory(name, icon, color)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
fun ElectroGlassTopBar(
    currentTab: Int,
    cartCount: Int,
    onCartClick: () -> Unit
) {
    val title = when (currentTab) {
        0 -> "Dashboard"
        1 -> "Product Inventory"
        2 -> "Stock Management"
        3 -> "Point of Sale (POS)"
        4 -> "Analytics & Reports"
        else -> "Shop Settings"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ELECTROGLASS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 1.5.sp
            )
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Cart icon shortcut with badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onCartClick() },
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ) {
                            Text("$cartCount", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = "POS Cart",
                    tint = if (cartCount > 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LiquidGlassBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    cartCount: Int
) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Dashboard, 0),
        NavigationItem("Products", Icons.Default.Devices, 1),
        NavigationItem("Stock", Icons.Default.Inventory2, 2),
        NavigationItem("Sales", Icons.Default.PointOfSale, 3),
        NavigationItem("Reports", Icons.Default.BarChart, 4),
        NavigationItem("Settings", Icons.Default.Settings, 5)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.4f),
                            Color(0xFF8B5CF6).copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedTab == item.index

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTabSelected(item.index) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.index == 3 && cartCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = NeonCyan, contentColor = Color.Black) {
                                            Text("$cartCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.Category
import com.example.data.entity.Product
import com.example.data.entity.Sale
import com.example.data.entity.SaleItem
import com.example.data.entity.ShopProfile
import com.example.data.entity.StockTransaction
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ShopRepository(database)

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<StockTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSaleItems: StateFlow<List<SaleItem>> = repository.allSaleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shopProfile: StateFlow<ShopProfile?> = repository.shopProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")
    val stockFilter = MutableStateFlow("All") // "All", "Low Stock", "Out of Stock"
    val reportTimeframe = MutableStateFlow("All Time") // "Today", "This Week", "This Month", "All Time"

    // POS Cart state
    private val _cartItems = MutableStateFlow<List<ShopRepository.CartItem>>(emptyList())
    val cartItems: StateFlow<List<ShopRepository.CartItem>> = _cartItems.asStateFlow()

    val cartCustomerName = MutableStateFlow("Walk-in Customer")
    val cartCustomerPhone = MutableStateFlow("")
    val cartPaymentMethod = MutableStateFlow("Cash") // Cash, UPI / QR, Card, Credit / Due
    val cartDiscount = MutableStateFlow(0.0)
    val cartNotes = MutableStateFlow("")

    // Active Invoice Modal
    private val _activeInvoiceSale = MutableStateFlow<Sale?>(null)
    val activeInvoiceSale: StateFlow<Sale?> = _activeInvoiceSale.asStateFlow()

    private val _activeInvoiceItems = MutableStateFlow<List<SaleItem>>(emptyList())
    val activeInvoiceItems: StateFlow<List<SaleItem>> = _activeInvoiceItems.asStateFlow()

    // Notification toast/snack message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Cart POS Methods
    fun addToCart(product: Product) {
        if (product.stockQuantity <= 0) {
            showToast("${product.name} is out of stock!")
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            if (existing.quantity < product.stockQuantity) {
                current[index] = existing.copy(quantity = existing.quantity + 1)
                _cartItems.value = current
            } else {
                showToast("Maximum available stock reached (${product.stockQuantity})")
            }
        } else {
            current.add(ShopRepository.CartItem(product, 1))
            _cartItems.value = current
        }
    }

    fun removeFromCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            if (existing.quantity > 1) {
                current[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                current.removeAt(index)
            }
            _cartItems.value = current
        }
    }

    fun updateCartQuantity(product: Product, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (quantity <= 0) {
            if (index >= 0) {
                current.removeAt(index)
                _cartItems.value = current
            }
        } else {
            val validQty = quantity.coerceAtMost(product.stockQuantity)
            if (index >= 0) {
                current[index] = current[index].copy(quantity = validQty)
                _cartItems.value = current
            } else {
                current.add(ShopRepository.CartItem(product, validQty))
                _cartItems.value = current
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        cartDiscount.value = 0.0
        cartCustomerName.value = "Walk-in Customer"
        cartCustomerPhone.value = ""
        cartNotes.value = ""
    }

    fun completeSale() {
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val sale = repository.createSale(
                customerName = cartCustomerName.value,
                customerPhone = cartCustomerPhone.value,
                paymentMethod = cartPaymentMethod.value,
                cartItems = items,
                discountAmount = cartDiscount.value,
                notes = cartNotes.value
            )

            if (sale != null) {
                val saleItems = items.map {
                    SaleItem(
                        saleId = sale.id,
                        productId = it.product.id,
                        productName = it.product.name,
                        modelNumber = it.product.modelNumber,
                        unitPrice = it.product.sellingPrice,
                        costPrice = it.product.purchasePrice,
                        quantity = it.quantity,
                        totalPrice = it.product.sellingPrice * it.quantity,
                        profit = (it.product.sellingPrice - it.product.purchasePrice) * it.quantity
                    )
                }
                clearCart()
                _activeInvoiceSale.value = sale
                _activeInvoiceItems.value = saleItems
                showToast("Sale completed! Invoice #${sale.invoiceNumber} generated.")
            } else {
                showToast("Failed to process sale.")
            }
        }
    }

    fun showInvoice(sale: Sale) {
        viewModelScope.launch {
            _activeInvoiceSale.value = sale
            val allItems = allSaleItems.value.filter { it.saleId == sale.id }
            _activeInvoiceItems.value = allItems
        }
    }

    fun dismissInvoice() {
        _activeInvoiceSale.value = null
        _activeInvoiceItems.value = emptyList()
    }

    // Product CRUD
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
                showToast("Product '${product.name}' added successfully!")
            } else {
                repository.updateProduct(product)
                showToast("Product '${product.name}' updated!")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showToast("Product '${product.name}' deleted.")
        }
    }

    // Stock Actions
    fun addStock(productId: Long, quantity: Int, costPerUnit: Double, notes: String, supplier: String) {
        viewModelScope.launch {
            val success = repository.addStock(productId, quantity, costPerUnit, notes, supplier)
            if (success) {
                showToast("Restocked +$quantity units successfully!")
            }
        }
    }

    fun reduceStock(productId: Long, quantity: Int, notes: String, reason: String) {
        viewModelScope.launch {
            val success = repository.reduceStock(productId, quantity, notes, reason)
            if (success) {
                showToast("Stock reduced by -$quantity units.")
            }
        }
    }

    fun adjustStock(productId: Long, newPhysicalQuantity: Int, reason: String) {
        viewModelScope.launch {
            val success = repository.adjustStock(productId, newPhysicalQuantity, reason)
            if (success) {
                showToast("Stock adjusted to $newPhysicalQuantity units.")
            }
        }
    }

    // Category CRUD
    fun addCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, iconName = icon, colorHex = color, isCustom = true))
            showToast("Category '$name' added!")
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showToast("Category '${category.name}' removed.")
        }
    }

    // Shop Profile
    fun updateProfile(profile: ShopProfile) {
        viewModelScope.launch {
            repository.updateShopProfile(profile)
            showToast("Shop settings saved!")
        }
    }

    // Backup & Restore
    fun exportBackup(onExport: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataToJson()
            onExport(json)
            showToast("Full backup generated successfully!")
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            val result = repository.importDataFromJson(json)
            if (result.isSuccess) {
                showToast("Data restored successfully!")
            } else {
                showToast("Import error: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            clearCart()
            showToast("Sample data restored!")
        }
    }
}

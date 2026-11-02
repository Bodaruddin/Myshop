package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val brand: String,
    val modelNumber: String,
    val barcode: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val minStockAlert: Int = 5,
    val description: String = "",
    val iconName: String = "devices",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profitMarginPerUnit: Double get() = sellingPrice - purchasePrice
    val profitMarginPercent: Double get() = if (purchasePrice > 0) ((sellingPrice - purchasePrice) / purchasePrice) * 100 else 0.0
    val isLowStock: Boolean get() = stockQuantity in 1..minStockAlert
    val isOutOfStock: Boolean get() = stockQuantity <= 0
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "devices",
    val colorHex: String = "#00F2FE",
    val isCustom: Boolean = false
)

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // STOCK_IN, STOCK_OUT, ADJUSTMENT, SALE
    val quantity: Int, // Positive for in, negative for out/sale
    val costPerUnit: Double = 0.0,
    val notes: String = "",
    val supplierOrSource: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val paymentMethod: String = "Cash", // Cash, UPI, Card, Due
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val totalProfit: Double,
    val itemCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val modelNumber: String,
    val unitPrice: Double,
    val costPrice: Double,
    val quantity: Int,
    val totalPrice: Double,
    val profit: Double
)

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "VoltTech Electronics",
    val tagline: String = "Premium Gadgets & Electronics Hub",
    val address: String = "Shop 12, Techno Arcade, MG Road, Bengaluru",
    val phoneNumber: String = "+91 98765 43210",
    val email: String = "contact@volttech.in",
    val gstin: String = "29ABCDE1234F1Z5",
    val currencySymbol: String = "₹",
    val invoicePrefix: String = "INV-",
    val invoiceFooterNote: String = "Thank you for shopping with us! 7 Days warranty on manufacturing defects.",
    val themeMode: String = "DARK" // DARK, LIGHT, SYSTEM
)

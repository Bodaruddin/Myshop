package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.dao.ShopProfileDao
import com.example.data.dao.StockTransactionDao
import com.example.data.entity.Category
import com.example.data.entity.Product
import com.example.data.entity.Sale
import com.example.data.entity.SaleItem
import com.example.data.entity.ShopProfile
import com.example.data.entity.StockTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Category::class,
        StockTransaction::class,
        Sale::class,
        SaleItem::class,
        ShopProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun saleDao(): SaleDao
    abstract fun shopProfileDao(): ShopProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electro_glass_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    val categoryDao = database.categoryDao()
    val productDao = database.productDao()
    val shopProfileDao = database.shopProfileDao()
    val saleDao = database.saleDao()
    val stockTransactionDao = database.stockTransactionDao()

    // 1. Initial Shop Profile
    shopProfileDao.insertOrUpdate(
        ShopProfile(
            id = 1,
            shopName = "VoltTech Electronics",
            tagline = "Premium Gadgets & Electronics Hub",
            address = "Shop 12, Techno Arcade, MG Road, Bengaluru",
            phoneNumber = "+91 98765 43210",
            email = "contact@volttech.in",
            gstin = "29ABCDE1234F1Z5",
            currencySymbol = "₹",
            invoicePrefix = "INV-",
            invoiceFooterNote = "Thank you for shopping with us! 7 Days warranty on manufacturing defects.",
            themeMode = "DARK"
        )
    )

    // 2. Categories
    val defaultCategories = listOf(
        Category(1, "Mobile Phones", "smartphone", "#00F2FE", false),
        Category(2, "Chargers", "battery_charging", "#4FACFE", false),
        Category(3, "Headphones", "headphones", "#8B5CF6", false),
        Category(4, "Speakers", "speaker", "#EC4899", false),
        Category(5, "Electronics Accessories", "devices", "#10B981", false),
        Category(6, "Cables", "cable", "#F59E0B", false),
        Category(7, "Smart Watches", "watch", "#6366F1", false),
        Category(8, "Other Products", "inventory", "#64748B", false)
    )
    categoryDao.insertCategories(defaultCategories)

    // 3. Realistic Products
    val now = System.currentTimeMillis()
    val dayMs = 24 * 60 * 60 * 1000L

    val defaultProducts = listOf(
        Product(
            id = 1,
            name = "iPhone 15 Pro",
            category = "Mobile Phones",
            brand = "Apple",
            modelNumber = "A3102",
            barcode = "890123400101",
            purchasePrice = 118000.0,
            sellingPrice = 134900.0,
            stockQuantity = 8,
            minStockAlert = 3,
            description = "Natural Titanium 128GB with A17 Pro chip and Super Retina XDR.",
            iconName = "smartphone",
            createdAt = now - (15 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 2,
            name = "Galaxy S24 Ultra",
            category = "Mobile Phones",
            brand = "Samsung",
            modelNumber = "SM-S928B",
            barcode = "890123400102",
            purchasePrice = 105000.0,
            sellingPrice = 129999.0,
            stockQuantity = 6,
            minStockAlert = 2,
            description = "Titanium Gray 256GB with Galaxy AI, S-Pen & 200MP Quad Tele System.",
            iconName = "smartphone",
            createdAt = now - (12 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 3,
            name = "OnePlus 12 5G",
            category = "Mobile Phones",
            brand = "OnePlus",
            modelNumber = "CPH2573",
            barcode = "890123400103",
            purchasePrice = 52000.0,
            sellingPrice = 64999.0,
            stockQuantity = 11,
            minStockAlert = 4,
            description = "Flowy Emerald 16GB/512GB Snapdragon 8 Gen 3 with 100W SUPERVOOC.",
            iconName = "smartphone",
            createdAt = now - (10 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 4,
            name = "Anker 65W GaN Charger",
            category = "Chargers",
            brand = "Anker",
            modelNumber = "A2668",
            barcode = "890123400201",
            purchasePrice = 2400.0,
            sellingPrice = 3999.0,
            stockQuantity = 22,
            minStockAlert = 5,
            description = "3-Port 735 Fast Charger for laptops, phones and tablets.",
            iconName = "battery_charging",
            createdAt = now - (20 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 5,
            name = "Apple 20W USB-C Adapter",
            category = "Chargers",
            brand = "Apple",
            modelNumber = "MHJE3HN/A",
            barcode = "890123400202",
            purchasePrice = 1200.0,
            sellingPrice = 1900.0,
            stockQuantity = 18,
            minStockAlert = 5,
            description = "Original Apple fast wall charger for iPhone & iPad.",
            iconName = "battery_charging",
            createdAt = now - (25 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 6,
            name = "Sony WH-1000XM5",
            category = "Headphones",
            brand = "Sony",
            modelNumber = "WH1000XM5/B",
            barcode = "890123400301",
            purchasePrice = 23500.0,
            sellingPrice = 29990.0,
            stockQuantity = 4,
            minStockAlert = 2,
            description = "Industry Leading Active Noise Canceling Wireless Bluetooth Headphones.",
            iconName = "headphones",
            createdAt = now - (8 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 7,
            name = "boAt Rockerz 450",
            category = "Headphones",
            brand = "boAt",
            modelNumber = "RC450-BL",
            barcode = "890123400302",
            purchasePrice = 850.0,
            sellingPrice = 1499.0,
            stockQuantity = 28,
            minStockAlert = 8,
            description = "On-Ear Wireless Headset with 40mm drivers and 15hr playback.",
            iconName = "headphones",
            createdAt = now - (30 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 8,
            name = "JBL Flip 6",
            category = "Speakers",
            brand = "JBL",
            modelNumber = "JBLFLIP6BLU",
            barcode = "890123400401",
            purchasePrice = 7800.0,
            sellingPrice = 10999.0,
            stockQuantity = 7,
            minStockAlert = 3,
            description = "Portable Waterproof Bluetooth Speaker with 2-way speaker system.",
            iconName = "speaker",
            createdAt = now - (14 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 9,
            name = "Marshall Emberton II",
            category = "Speakers",
            brand = "Marshall",
            modelNumber = "EMBERTON-II",
            barcode = "890123400402",
            purchasePrice = 12500.0,
            sellingPrice = 16999.0,
            stockQuantity = 2, // LOW STOCK
            minStockAlert = 3,
            description = "Black and Brass 360 multi-directional sound portable speaker.",
            iconName = "speaker",
            createdAt = now - (5 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 10,
            name = "Apple Watch Series 9",
            category = "Smart Watches",
            brand = "Apple",
            modelNumber = "MR8X3HN/A",
            barcode = "890123400701",
            purchasePrice = 33000.0,
            sellingPrice = 41900.0,
            stockQuantity = 5,
            minStockAlert = 2,
            description = "Midnight Aluminum 45mm GPS with Double Tap gesture.",
            iconName = "watch",
            createdAt = now - (9 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 11,
            name = "Galaxy Watch 6 Classic",
            category = "Smart Watches",
            brand = "Samsung",
            modelNumber = "SM-R960N",
            barcode = "890123400702",
            purchasePrice = 24000.0,
            sellingPrice = 29999.0,
            stockQuantity = 0, // OUT OF STOCK
            minStockAlert = 2,
            description = "Rotating bezel 47mm Bluetooth with comprehensive health monitoring.",
            iconName = "watch",
            createdAt = now - (18 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 12,
            name = "Belkin Braided C-to-Lightning",
            category = "Cables",
            brand = "Belkin",
            modelNumber = "CAA004bt2MBK",
            barcode = "890123400601",
            purchasePrice = 900.0,
            sellingPrice = 1799.0,
            stockQuantity = 2, // LOW STOCK
            minStockAlert = 5,
            description = "2-meter ultra-durable braided fast charging cable.",
            iconName = "cable",
            createdAt = now - (22 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 13,
            name = "Baseus 100W PD Type-C Cable",
            category = "Cables",
            brand = "Baseus",
            modelNumber = "CATJK-D01",
            barcode = "890123400602",
            purchasePrice = 450.0,
            sellingPrice = 899.0,
            stockQuantity = 34,
            minStockAlert = 6,
            description = "100W Power Delivery with digital wattage LED display.",
            iconName = "cable",
            createdAt = now - (28 * dayMs),
            updatedAt = now
        ),
        Product(
            id = 14,
            name = "Spigen Ultra Hybrid Clear Case",
            category = "Electronics Accessories",
            brand = "Spigen",
            modelNumber = "ACS06541",
            barcode = "890123400501",
            purchasePrice = 650.0,
            sellingPrice = 1499.0,
            stockQuantity = 19,
            minStockAlert = 5,
            description = "Anti-yellowing crystal clear shock absorption bumper case.",
            iconName = "devices",
            createdAt = now - (11 * dayMs),
            updatedAt = now
        )
    )
    productDao.insertProducts(defaultProducts)

    // 4. Sample Stock In Transactions
    val transactions = listOf(
        StockTransaction(
            productId = 1,
            productName = "iPhone 15 Pro",
            type = "STOCK_IN",
            quantity = 10,
            costPerUnit = 118000.0,
            notes = "Initial shipment received from Apple Distributor",
            supplierOrSource = "Ingram Micro India",
            timestamp = now - (15 * dayMs)
        ),
        StockTransaction(
            productId = 4,
            productName = "Anker 65W GaN Charger",
            type = "STOCK_IN",
            quantity = 25,
            costPerUnit = 2400.0,
            notes = "Restock order #AK-8891",
            supplierOrSource = "Anker Direct Logistics",
            timestamp = now - (10 * dayMs)
        ),
        StockTransaction(
            productId = 9,
            productName = "Marshall Emberton II",
            type = "STOCK_IN",
            quantity = 5,
            costPerUnit = 12500.0,
            notes = "Received luxury speakers batch",
            supplierOrSource = "Marshall Audio Global",
            timestamp = now - (5 * dayMs)
        ),
        StockTransaction(
            productId = 11,
            productName = "Galaxy Watch 6 Classic",
            type = "STOCK_OUT",
            quantity = -3,
            costPerUnit = 24000.0,
            notes = "Transferred to partner exhibition stall",
            supplierOrSource = "Internal Demo",
            timestamp = now - (2 * dayMs)
        )
    )
    stockTransactionDao.insertTransactions(transactions)

    // 5. Sample Sales & Invoices
    val sale1 = Sale(
        id = 1,
        invoiceNumber = "INV-2026-0001",
        customerName = "Rahul Sharma",
        customerPhone = "+91 98450 11223",
        paymentMethod = "UPI / QR",
        subtotal = 138899.0,
        discountAmount = 2000.0,
        totalAmount = 136899.0,
        totalProfit = 18499.0,
        itemCount = 2,
        timestamp = now - (2 * dayMs),
        notes = "Festive bundle offer applied"
    )
    saleDao.insertSale(sale1)
    saleDao.insertSaleItems(
        listOf(
            SaleItem(
                id = 1,
                saleId = 1,
                productId = 1,
                productName = "iPhone 15 Pro",
                modelNumber = "A3102",
                unitPrice = 134900.0,
                costPrice = 118000.0,
                quantity = 1,
                totalPrice = 134900.0,
                profit = 16900.0
            ),
            SaleItem(
                id = 2,
                saleId = 1,
                productId = 4,
                productName = "Anker 65W GaN Charger",
                modelNumber = "A2668",
                unitPrice = 3999.0,
                costPrice = 2400.0,
                quantity = 1,
                totalPrice = 3999.0,
                profit = 1599.0
            )
        )
    )

    val sale2 = Sale(
        id = 2,
        invoiceNumber = "INV-2026-0002",
        customerName = "Priya Nair",
        customerPhone = "+91 97412 88990",
        paymentMethod = "Card",
        subtotal = 31489.0,
        discountAmount = 500.0,
        totalAmount = 30989.0,
        totalProfit = 6790.0,
        itemCount = 2,
        timestamp = now - (1 * dayMs),
        notes = "Standard customer invoice"
    )
    saleDao.insertSale(sale2)
    saleDao.insertSaleItems(
        listOf(
            SaleItem(
                id = 3,
                saleId = 2,
                productId = 6,
                productName = "Sony WH-1000XM5",
                modelNumber = "WH1000XM5/B",
                unitPrice = 29990.0,
                costPrice = 23500.0,
                quantity = 1,
                totalPrice = 29990.0,
                profit = 6490.0
            ),
            SaleItem(
                id = 4,
                saleId = 2,
                productId = 14,
                productName = "Spigen Ultra Hybrid Clear Case",
                modelNumber = "ACS06541",
                unitPrice = 1499.0,
                costPrice = 650.0,
                quantity = 1,
                totalPrice = 1499.0,
                profit = 849.0
            )
        )
    )

    val sale3 = Sale(
        id = 3,
        invoiceNumber = "INV-2026-0003",
        customerName = "Arjun Patel",
        customerPhone = "+91 99001 55443",
        paymentMethod = "Cash",
        subtotal = 11898.0,
        discountAmount = 300.0,
        totalAmount = 11598.0,
        totalProfit = 3349.0,
        itemCount = 2,
        timestamp = now - (3 * 60 * 60 * 1000L), // 3 hours ago today
        notes = "Cash counter sale"
    )
    saleDao.insertSale(sale3)
    saleDao.insertSaleItems(
        listOf(
            SaleItem(
                id = 5,
                saleId = 3,
                productId = 8,
                productName = "JBL Flip 6",
                modelNumber = "JBLFLIP6BLU",
                unitPrice = 10999.0,
                costPrice = 7800.0,
                quantity = 1,
                totalPrice = 10999.0,
                profit = 3199.0
            ),
            SaleItem(
                id = 6,
                saleId = 3,
                productId = 13,
                productName = "Baseus 100W PD Type-C Cable",
                modelNumber = "CATJK-D01",
                unitPrice = 899.0,
                costPrice = 450.0,
                quantity = 1,
                totalPrice = 899.0,
                profit = 449.0
            )
        )
    )
}

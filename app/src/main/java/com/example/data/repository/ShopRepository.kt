package com.example.data.repository

import com.example.data.dao.CategoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.dao.ShopProfileDao
import com.example.data.dao.StockTransactionDao
import com.example.data.database.AppDatabase
import com.example.data.database.populateInitialData
import com.example.data.entity.Category
import com.example.data.entity.Product
import com.example.data.entity.Sale
import com.example.data.entity.SaleItem
import com.example.data.entity.ShopProfile
import com.example.data.entity.StockTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShopRepository(
    private val database: AppDatabase,
    private val productDao: ProductDao = database.productDao(),
    private val categoryDao: CategoryDao = database.categoryDao(),
    private val stockTransactionDao: StockTransactionDao = database.stockTransactionDao(),
    private val saleDao: SaleDao = database.saleDao(),
    private val shopProfileDao: ShopProfileDao = database.shopProfileDao()
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<StockTransaction>> = stockTransactionDao.getAllTransactions()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allSaleItems: Flow<List<SaleItem>> = saleDao.getAllSaleItems()
    val shopProfile: Flow<ShopProfile?> = shopProfileDao.getProfile()

    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> = saleDao.getSaleItems(saleId)

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        val id = productDao.insertProduct(product)
        if (product.stockQuantity > 0) {
            stockTransactionDao.insertTransaction(
                StockTransaction(
                    productId = id,
                    productName = product.name,
                    type = "STOCK_IN",
                    quantity = product.stockQuantity,
                    costPerUnit = product.purchasePrice,
                    notes = "Initial product stock entry",
                    supplierOrSource = "Inventory Opening",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        id
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun addStock(
        productId: Long,
        quantity: Int,
        costPerUnit: Double,
        notes: String,
        supplier: String
    ): Boolean = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: return@withContext false
        val newQty = product.stockQuantity + quantity
        productDao.updateStockQuantity(productId, newQty)
        stockTransactionDao.insertTransaction(
            StockTransaction(
                productId = productId,
                productName = product.name,
                type = "STOCK_IN",
                quantity = quantity,
                costPerUnit = if (costPerUnit > 0) costPerUnit else product.purchasePrice,
                notes = notes,
                supplierOrSource = supplier,
                timestamp = System.currentTimeMillis()
            )
        )
        true
    }

    suspend fun reduceStock(
        productId: Long,
        quantity: Int,
        notes: String,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: return@withContext false
        val newQty = (product.stockQuantity - quantity).coerceAtLeast(0)
        productDao.updateStockQuantity(productId, newQty)
        stockTransactionDao.insertTransaction(
            StockTransaction(
                productId = productId,
                productName = product.name,
                type = "STOCK_OUT",
                quantity = -quantity,
                costPerUnit = product.purchasePrice,
                notes = notes,
                supplierOrSource = reason,
                timestamp = System.currentTimeMillis()
            )
        )
        true
    }

    suspend fun adjustStock(
        productId: Long,
        newPhysicalQuantity: Int,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: return@withContext false
        val diff = newPhysicalQuantity - product.stockQuantity
        productDao.updateStockQuantity(productId, newPhysicalQuantity)
        stockTransactionDao.insertTransaction(
            StockTransaction(
                productId = productId,
                productName = product.name,
                type = "ADJUSTMENT",
                quantity = diff,
                costPerUnit = product.purchasePrice,
                notes = "Stock adjusted from ${product.stockQuantity} to $newPhysicalQuantity ($reason)",
                supplierOrSource = "Physical Audit",
                timestamp = System.currentTimeMillis()
            )
        )
        true
    }

    data class CartItem(
        val product: Product,
        val quantity: Int
    )

    suspend fun createSale(
        customerName: String,
        customerPhone: String,
        paymentMethod: String,
        cartItems: List<CartItem>,
        discountAmount: Double,
        notes: String
    ): Sale? = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) return@withContext null

        val subtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }
        val totalCost = cartItems.sumOf { it.product.purchasePrice * it.quantity }
        val totalAmount = (subtotal - discountAmount).coerceAtLeast(0.0)
        val totalProfit = totalAmount - totalCost
        val totalItemsCount = cartItems.sumOf { it.quantity }

        val profile = shopProfileDao.getProfileDirect()
        val prefix = profile?.invoicePrefix ?: "INV-"
        val timeStamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val invoiceNumber = "$prefix${dateFormat.format(Date(timeStamp))}"

        val sale = Sale(
            invoiceNumber = invoiceNumber,
            customerName = customerName.ifBlank { "Walk-in Customer" },
            customerPhone = customerPhone,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            totalProfit = totalProfit,
            itemCount = totalItemsCount,
            timestamp = timeStamp,
            notes = notes
        )

        val saleId = saleDao.insertSale(sale)
        val createdSale = sale.copy(id = saleId)

        val saleItems = cartItems.map { item ->
            val itemTotal = item.product.sellingPrice * item.quantity
            val itemProfit = (item.product.sellingPrice - item.product.purchasePrice) * item.quantity
            SaleItem(
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                modelNumber = item.product.modelNumber,
                unitPrice = item.product.sellingPrice,
                costPrice = item.product.purchasePrice,
                quantity = item.quantity,
                totalPrice = itemTotal,
                profit = itemProfit
            )
        }
        saleDao.insertSaleItems(saleItems)

        // Deduct inventory and record stock transactions
        for (item in cartItems) {
            val freshProduct = productDao.getProductById(item.product.id)
            if (freshProduct != null) {
                val updatedQty = (freshProduct.stockQuantity - item.quantity).coerceAtLeast(0)
                productDao.updateStockQuantity(item.product.id, updatedQty)
                stockTransactionDao.insertTransaction(
                    StockTransaction(
                        productId = item.product.id,
                        productName = item.product.name,
                        type = "SALE",
                        quantity = -item.quantity,
                        costPerUnit = item.product.purchasePrice,
                        notes = "Invoice #$invoiceNumber",
                        supplierOrSource = customerName.ifBlank { "Sale" },
                        timestamp = timeStamp
                    )
                )
            }
        }

        createdSale
    }

    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    suspend fun updateShopProfile(profile: ShopProfile) = withContext(Dispatchers.IO) {
        shopProfileDao.insertOrUpdate(profile)
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        productDao.deleteAllProducts()
        categoryDao.deleteAllCategories()
        stockTransactionDao.deleteAllTransactions()
        saleDao.deleteAllSales()
        saleDao.deleteAllSaleItems()
        populateInitialData(database)
    }

    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "ElectroGlass")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val profile = shopProfileDao.getProfileDirect()
        if (profile != null) {
            val pObj = JSONObject()
            pObj.put("shopName", profile.shopName)
            pObj.put("tagline", profile.tagline)
            pObj.put("address", profile.address)
            pObj.put("phoneNumber", profile.phoneNumber)
            pObj.put("email", profile.email)
            pObj.put("gstin", profile.gstin)
            pObj.put("currencySymbol", profile.currencySymbol)
            pObj.put("invoicePrefix", profile.invoicePrefix)
            pObj.put("invoiceFooterNote", profile.invoiceFooterNote)
            pObj.put("themeMode", profile.themeMode)
            root.put("profile", pObj)
        }

        // Categories
        val catsArray = JSONArray()
        // Query categories directly
        // We can collect first emission or use database directly
        val cats = database.openHelper.readableDatabase.query("SELECT * FROM categories")
        while (cats.moveToNext()) {
            val cObj = JSONObject()
            cObj.put("id", cats.getLong(cats.getColumnIndexOrThrow("id")))
            cObj.put("name", cats.getString(cats.getColumnIndexOrThrow("name")))
            cObj.put("iconName", cats.getString(cats.getColumnIndexOrThrow("iconName")))
            cObj.put("colorHex", cats.getString(cats.getColumnIndexOrThrow("colorHex")))
            cObj.put("isCustom", cats.getInt(cats.getColumnIndexOrThrow("isCustom")) == 1)
            catsArray.put(cObj)
        }
        cats.close()
        root.put("categories", catsArray)

        // Products
        val prodsArray = JSONArray()
        val prods = database.openHelper.readableDatabase.query("SELECT * FROM products")
        while (prods.moveToNext()) {
            val pr = JSONObject()
            pr.put("id", prods.getLong(prods.getColumnIndexOrThrow("id")))
            pr.put("name", prods.getString(prods.getColumnIndexOrThrow("name")))
            pr.put("category", prods.getString(prods.getColumnIndexOrThrow("category")))
            pr.put("brand", prods.getString(prods.getColumnIndexOrThrow("brand")))
            pr.put("modelNumber", prods.getString(prods.getColumnIndexOrThrow("modelNumber")))
            pr.put("barcode", prods.getString(prods.getColumnIndexOrThrow("barcode")))
            pr.put("purchasePrice", prods.getDouble(prods.getColumnIndexOrThrow("purchasePrice")))
            pr.put("sellingPrice", prods.getDouble(prods.getColumnIndexOrThrow("sellingPrice")))
            pr.put("stockQuantity", prods.getInt(prods.getColumnIndexOrThrow("stockQuantity")))
            pr.put("minStockAlert", prods.getInt(prods.getColumnIndexOrThrow("minStockAlert")))
            pr.put("description", prods.getString(prods.getColumnIndexOrThrow("description")))
            pr.put("iconName", prods.getString(prods.getColumnIndexOrThrow("iconName")))
            pr.put("createdAt", prods.getLong(prods.getColumnIndexOrThrow("createdAt")))
            pr.put("updatedAt", prods.getLong(prods.getColumnIndexOrThrow("updatedAt")))
            prodsArray.put(pr)
        }
        prods.close()
        root.put("products", prodsArray)

        // Sales
        val salesArray = JSONArray()
        val salesCursor = database.openHelper.readableDatabase.query("SELECT * FROM sales")
        while (salesCursor.moveToNext()) {
            val s = JSONObject()
            s.put("id", salesCursor.getLong(salesCursor.getColumnIndexOrThrow("id")))
            s.put("invoiceNumber", salesCursor.getString(salesCursor.getColumnIndexOrThrow("invoiceNumber")))
            s.put("customerName", salesCursor.getString(salesCursor.getColumnIndexOrThrow("customerName")))
            s.put("customerPhone", salesCursor.getString(salesCursor.getColumnIndexOrThrow("customerPhone")))
            s.put("paymentMethod", salesCursor.getString(salesCursor.getColumnIndexOrThrow("paymentMethod")))
            s.put("subtotal", salesCursor.getDouble(salesCursor.getColumnIndexOrThrow("subtotal")))
            s.put("discountAmount", salesCursor.getDouble(salesCursor.getColumnIndexOrThrow("discountAmount")))
            s.put("totalAmount", salesCursor.getDouble(salesCursor.getColumnIndexOrThrow("totalAmount")))
            s.put("totalProfit", salesCursor.getDouble(salesCursor.getColumnIndexOrThrow("totalProfit")))
            s.put("itemCount", salesCursor.getInt(salesCursor.getColumnIndexOrThrow("itemCount")))
            s.put("timestamp", salesCursor.getLong(salesCursor.getColumnIndexOrThrow("timestamp")))
            s.put("notes", salesCursor.getString(salesCursor.getColumnIndexOrThrow("notes")))
            salesArray.put(s)
        }
        salesCursor.close()
        root.put("sales", salesArray)

        // Sale Items
        val itemsArray = JSONArray()
        val itemsCursor = database.openHelper.readableDatabase.query("SELECT * FROM sale_items")
        while (itemsCursor.moveToNext()) {
            val it = JSONObject()
            it.put("id", itemsCursor.getLong(itemsCursor.getColumnIndexOrThrow("id")))
            it.put("saleId", itemsCursor.getLong(itemsCursor.getColumnIndexOrThrow("saleId")))
            it.put("productId", itemsCursor.getLong(itemsCursor.getColumnIndexOrThrow("productId")))
            it.put("productName", itemsCursor.getString(itemsCursor.getColumnIndexOrThrow("productName")))
            it.put("modelNumber", itemsCursor.getString(itemsCursor.getColumnIndexOrThrow("modelNumber")))
            it.put("unitPrice", itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow("unitPrice")))
            it.put("costPrice", itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow("costPrice")))
            it.put("quantity", itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow("quantity")))
            it.put("totalPrice", itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow("totalPrice")))
            it.put("profit", itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow("profit")))
            itemsArray.put(it)
        }
        itemsCursor.close()
        root.put("saleItems", itemsArray)

        // Transactions
        val txArray = JSONArray()
        val txCursor = database.openHelper.readableDatabase.query("SELECT * FROM stock_transactions")
        while (txCursor.moveToNext()) {
            val t = JSONObject()
            t.put("id", txCursor.getLong(txCursor.getColumnIndexOrThrow("id")))
            t.put("productId", txCursor.getLong(txCursor.getColumnIndexOrThrow("productId")))
            t.put("productName", txCursor.getString(txCursor.getColumnIndexOrThrow("productName")))
            t.put("type", txCursor.getString(txCursor.getColumnIndexOrThrow("type")))
            t.put("quantity", txCursor.getInt(txCursor.getColumnIndexOrThrow("quantity")))
            t.put("costPerUnit", txCursor.getDouble(txCursor.getColumnIndexOrThrow("costPerUnit")))
            t.put("notes", txCursor.getString(txCursor.getColumnIndexOrThrow("notes")))
            t.put("supplierOrSource", txCursor.getString(txCursor.getColumnIndexOrThrow("supplierOrSource")))
            t.put("timestamp", txCursor.getLong(txCursor.getColumnIndexOrThrow("timestamp")))
            txArray.put(t)
        }
        txCursor.close()
        root.put("stockTransactions", txArray)

        root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)

            // Profile
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                shopProfileDao.insertOrUpdate(
                    ShopProfile(
                        id = 1,
                        shopName = p.optString("shopName", "VoltTech Electronics"),
                        tagline = p.optString("tagline", "Premium Gadgets Hub"),
                        address = p.optString("address", ""),
                        phoneNumber = p.optString("phoneNumber", ""),
                        email = p.optString("email", ""),
                        gstin = p.optString("gstin", ""),
                        currencySymbol = p.optString("currencySymbol", "₹"),
                        invoicePrefix = p.optString("invoicePrefix", "INV-"),
                        invoiceFooterNote = p.optString("invoiceFooterNote", "Thank you for shopping with us!"),
                        themeMode = p.optString("themeMode", "DARK")
                    )
                )
            }

            // Categories
            if (root.has("categories")) {
                val cats = root.getJSONArray("categories")
                val catList = mutableListOf<Category>()
                for (i in 0 until cats.length()) {
                    val c = cats.getJSONObject(i)
                    catList.add(
                        Category(
                            id = c.optLong("id", 0),
                            name = c.getString("name"),
                            iconName = c.optString("iconName", "devices"),
                            colorHex = c.optString("colorHex", "#00F2FE"),
                            isCustom = c.optBoolean("isCustom", false)
                        )
                    )
                }
                if (catList.isNotEmpty()) {
                    categoryDao.deleteAllCategories()
                    categoryDao.insertCategories(catList)
                }
            }

            // Products
            if (root.has("products")) {
                val prods = root.getJSONArray("products")
                val prodList = mutableListOf<Product>()
                for (i in 0 until prods.length()) {
                    val pr = prods.getJSONObject(i)
                    prodList.add(
                        Product(
                            id = pr.optLong("id", 0),
                            name = pr.getString("name"),
                            category = pr.getString("category"),
                            brand = pr.optString("brand", ""),
                            modelNumber = pr.optString("modelNumber", ""),
                            barcode = pr.optString("barcode", ""),
                            purchasePrice = pr.optDouble("purchasePrice", 0.0),
                            sellingPrice = pr.optDouble("sellingPrice", 0.0),
                            stockQuantity = pr.optInt("stockQuantity", 0),
                            minStockAlert = pr.optInt("minStockAlert", 5),
                            description = pr.optString("description", ""),
                            iconName = pr.optString("iconName", "devices"),
                            createdAt = pr.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = pr.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (prodList.isNotEmpty()) {
                    productDao.deleteAllProducts()
                    productDao.insertProducts(prodList)
                }
            }

            // Transactions
            if (root.has("stockTransactions")) {
                val txs = root.getJSONArray("stockTransactions")
                val txList = mutableListOf<StockTransaction>()
                for (i in 0 until txs.length()) {
                    val t = txs.getJSONObject(i)
                    txList.add(
                        StockTransaction(
                            id = t.optLong("id", 0),
                            productId = t.optLong("productId", 0),
                            productName = t.optString("productName", ""),
                            type = t.optString("type", "STOCK_IN"),
                            quantity = t.optInt("quantity", 0),
                            costPerUnit = t.optDouble("costPerUnit", 0.0),
                            notes = t.optString("notes", ""),
                            supplierOrSource = t.optString("supplierOrSource", ""),
                            timestamp = t.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (txList.isNotEmpty()) {
                    stockTransactionDao.deleteAllTransactions()
                    stockTransactionDao.insertTransactions(txList)
                }
            }

            Result.success("Data imported successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

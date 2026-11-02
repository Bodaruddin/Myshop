package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.Sale
import com.example.data.entity.SaleItem
import com.example.data.entity.ShopProfile
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NeonCyan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDialog(
    sale: Sale,
    items: List<SaleItem>,
    profile: ShopProfile?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val effectiveProfile = profile ?: ShopProfile()
    val currency = effectiveProfile.currencySymbol
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldMint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Paid",
                                tint = EmeraldMint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TAX INVOICE",
                            color = NeonCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Printable Paper Receipt Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Shop Header
                        Text(
                            text = effectiveProfile.shopName.uppercase(),
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (effectiveProfile.tagline.isNotBlank()) {
                            Text(
                                text = effectiveProfile.tagline,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            text = effectiveProfile.address,
                            color = Color(0xFF475569),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Tel: ${effectiveProfile.phoneNumber}  |  GSTIN: ${effectiveProfile.gstin}",
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Invoice & Customer Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Invoice #: ${sale.invoiceNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Date: ${dateFormat.format(Date(sale.timestamp))}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Customer: ${sale.customerName}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Payment: ${sale.paymentMethod}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Items Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ITEM", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(2f))
                            Text(text = "QTY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text(text = "RATE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                            Text(text = "TOTAL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                        }

                        // Items list
                        for (item in items) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(
                                        text = item.productName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (item.modelNumber.isNotBlank()) {
                                        Text(
                                            text = item.modelNumber,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                Text(
                                    text = "${item.quantity}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$currency${formatter.format(item.unitPrice)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.weight(1.1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = "$currency${formatter.format(item.totalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals Summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal:", fontSize = 12.sp, color = Color(0xFF475569))
                                Text(text = "$currency${formatter.format(sale.subtotal)}", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                            }
                            if (sale.discountAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.7f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Discount:", fontSize = 12.sp, color = Color(0xFFDC2626))
                                    Text(text = "-$currency${formatter.format(sale.discountAmount)}", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFF0F172A), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Grand Total:", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text(
                                    text = "$currency${formatter.format(sale.totalAmount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Barcode & QR footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = effectiveProfile.invoiceFooterNote,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            QrCodePreview(
                                content = "INV:${sale.invoiceNumber}|TOTAL:${sale.totalAmount}|SHOP:${effectiveProfile.shopName}",
                                sizeDp = 64
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        BarcodeView(
                            code = sale.invoiceNumber.replace("-", ""),
                            modifier = Modifier.fillMaxWidth(),
                            showCodeText = false,
                            lightBackground = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Print / PDF & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidGlassButton(
                        text = "Print / PDF",
                        icon = Icons.Default.Print,
                        onClick = {
                            printInvoiceHtml(context, sale, items, effectiveProfile)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    LiquidGlassButton(
                        text = "Share Receipt",
                        icon = Icons.Default.Share,
                        onClick = {
                            shareInvoiceText(context, sale, items, effectiveProfile)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun shareInvoiceText(
    context: Context,
    sale: Sale,
    items: List<SaleItem>,
    profile: ShopProfile
) {
    val currency = profile.currencySymbol
    val sb = StringBuilder()
    sb.appendLine("🧾 *TAX INVOICE - ${profile.shopName.uppercase()}*")
    sb.appendLine("${profile.address}")
    sb.appendLine("Tel: ${profile.phoneNumber} | GSTIN: ${profile.gstin}")
    sb.appendLine("--------------------------------")
    sb.appendLine("Invoice: ${sale.invoiceNumber}")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    sb.appendLine("Date: ${dateFormat.format(Date(sale.timestamp))}")
    sb.appendLine("Customer: ${sale.customerName}")
    sb.appendLine("Payment: ${sale.paymentMethod}")
    sb.appendLine("--------------------------------")
    sb.appendLine("ITEMS:")
    for (item in items) {
        sb.appendLine("• ${item.productName} (x${item.quantity}) - $currency${item.totalPrice}")
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("Subtotal: $currency${sale.subtotal}")
    if (sale.discountAmount > 0) {
        sb.appendLine("Discount: -$currency${sale.discountAmount}")
    }
    sb.appendLine("GRAND TOTAL: $currency${sale.totalAmount}")
    sb.appendLine("--------------------------------")
    sb.appendLine(profile.invoiceFooterNote)

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Invoice via"))
}

private fun printInvoiceHtml(
    context: Context,
    sale: Sale,
    items: List<SaleItem>,
    profile: ShopProfile
) {
    val currency = profile.currencySymbol
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(sale.timestamp))

    val itemsHtml = StringBuilder()
    for (item in items) {
        itemsHtml.append(
            """
            <tr>
                <td style="padding: 8px 4px; border-bottom: 1px solid #e2e8f0;">
                    <b>${item.productName}</b><br><small style="color:#64748b;">${item.modelNumber}</small>
                </td>
                <td style="text-align: center; padding: 8px 4px; border-bottom: 1px solid #e2e8f0;">${item.quantity}</td>
                <td style="text-align: right; padding: 8px 4px; border-bottom: 1px solid #e2e8f0;">$currency${item.unitPrice}</td>
                <td style="text-align: right; padding: 8px 4px; border-bottom: 1px solid #e2e8f0;"><b>$currency${item.totalPrice}</b></td>
            </tr>
            """.trimIndent()
        )
    }

    val htmlDocument = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>${sale.invoiceNumber}</title>
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 20px; color: #0f172a; }
                .receipt { max-width: 600px; margin: 0 auto; border: 1px solid #cbd5e1; padding: 24px; border-radius: 12px; }
                .center { text-align: center; }
                .divider { border-top: 1px dashed #94a3b8; margin: 16px 0; }
                table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 13px; }
                th { background: #f1f5f9; padding: 8px 4px; text-align: left; }
                .totals { margin-top: 16px; float: right; width: 250px; }
                .totals-row { display: flex; justify-content: space-between; padding: 4px 0; }
                .grand-total { font-size: 16px; font-weight: bold; border-top: 2px solid #0f172a; margin-top: 4px; padding-top: 4px; }
                .clear { clear: both; }
                .footer { font-size: 11px; color: #64748b; margin-top: 30px; text-align: center; }
            </style>
        </head>
        <body>
            <div class="receipt">
                <div class="center">
                    <h2 style="margin:0; text-transform:uppercase;">${profile.shopName}</h2>
                    <p style="margin:4px 0; font-size: 12px; color: #64748b;">${profile.tagline}</p>
                    <p style="margin:2px 0; font-size: 12px;">${profile.address}</p>
                    <p style="margin:2px 0; font-size: 12px;">Tel: ${profile.phoneNumber} | GSTIN: ${profile.gstin}</p>
                </div>
                <div class="divider"></div>
                <div style="display: flex; justify-content: space-between; font-size: 12px;">
                    <div>
                        <b>Invoice #:</b> ${sale.invoiceNumber}<br>
                        <b>Date:</b> $dateStr
                    </div>
                    <div style="text-align: right;">
                        <b>Customer:</b> ${sale.customerName}<br>
                        <b>Payment:</b> ${sale.paymentMethod}
                    </div>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Item</th>
                            <th style="text-align:center;">Qty</th>
                            <th style="text-align:right;">Rate</th>
                            <th style="text-align:right;">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsHtml
                    </tbody>
                </table>
                <div class="totals">
                    <div class="totals-row"><span>Subtotal:</span><span>$currency${sale.subtotal}</span></div>
                    ${if (sale.discountAmount > 0) "<div class=\"totals-row\" style=\"color:#dc2626;\"><span>Discount:</span><span>-$currency${sale.discountAmount}</span></div>" else ""}
                    <div class="totals-row grand-total"><span>Grand Total:</span><span>$currency${sale.totalAmount}</span></div>
                </div>
                <div class="clear"></div>
                <div class="footer">
                    <p>${profile.invoiceFooterNote}</p>
                    <p>Computer Generated Tax Invoice</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(sale.invoiceNumber)
                printManager?.print(sale.invoiceNumber, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
    } catch (e: Exception) {
        // Fallback to text share
        shareInvoiceText(context, sale, items, profile)
    }
}

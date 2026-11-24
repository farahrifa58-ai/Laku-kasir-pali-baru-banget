package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.screens.ActivationScreen
import com.example.ui.screens.AddEditProductScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ProductListScreen
import com.example.ui.screens.ReceiptScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.LakuKasirTheme

class MainActivity : ComponentActivity() {

    // Lock system Configuration fontScale to 1.0f so device font size settings never resize app fonts
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        if (config.fontScale != 1.0f) {
            config.fontScale = 1.0f
        }
        val lockedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(lockedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LakuKasirViewModel = viewModel()
            val storeProfile by viewModel.storeProfile.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val productToEdit by viewModel.productToEdit.collectAsState()
            val activeReceipt by viewModel.activeReceiptTransaction.collectAsState()
            val isCheckingLicense by viewModel.isCheckingLicense.collectAsState()

            LakuKasirTheme(themeSetting = if (currentScreen == Screen.SPLASH) "light" else storeProfile.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (currentScreen == Screen.SPLASH) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.background
                ) {
                    // Back button handling (disabled on POS, SPLASH, and ACTIVATION gate)
                    BackHandler(
                        enabled = currentScreen != Screen.POS &&
                                currentScreen != Screen.SPLASH &&
                                currentScreen != Screen.ACTIVATION
                    ) {
                        when (currentScreen) {
                            Screen.ADD_EDIT_PRODUCT -> viewModel.navigateTo(Screen.PRODUCTS)
                            Screen.SETTINGS -> viewModel.navigateTo(Screen.POS)
                            Screen.RECEIPT_VIEW -> viewModel.navigateTo(Screen.POS)
                            else -> viewModel.navigateTo(Screen.POS)
                        }
                    }

                    when (currentScreen) {
                        Screen.SPLASH -> {
                            SplashScreen(
                                lang = storeProfile.language,
                                isCheckingLicense = isCheckingLicense,
                                onContinue = { viewModel.finishSplash() }
                            )
                        }

                        Screen.ACTIVATION -> {
                            ActivationScreen(viewModel = viewModel)
                        }

                        Screen.POS -> {
                            PosScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                                onOpenCheckout = {
                                    viewModel.navigateTo(Screen.CHECKOUT)
                                }
                            )
                        }

                        Screen.CHECKOUT -> {
                            CheckoutScreen(
                                viewModel = viewModel,
                                lang = storeProfile.language,
                                onBack = { viewModel.navigateTo(Screen.POS) },
                                onSuccess = { savedTx ->
                                    viewModel.activeReceiptTransaction.value = savedTx
                                    viewModel.navigateTo(Screen.RECEIPT_VIEW)
                                }
                            )
                        }

                        Screen.PRODUCTS -> {
                            ProductListScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                        }

                        Screen.ADD_EDIT_PRODUCT -> {
                            AddEditProductScreen(
                                productToEdit = productToEdit,
                                lang = storeProfile.language,
                                onBack = { viewModel.navigateTo(Screen.PRODUCTS) },
                                onCurrencyChanged = { newCurrency ->
                                    viewModel.setCurrency(newCurrency)
                                },
                                onSave = { id, name, category, unit, barcode, costPrice, sellingPrice, stock, isUnlimited, minAlert, supplierName, supplierPhone, imageUri ->
                                    viewModel.saveProduct(
                                        id = id,
                                        name = name,
                                        category = category,
                                        unit = unit,
                                        barcode = barcode,
                                        costPrice = costPrice,
                                        sellingPrice = sellingPrice,
                                        stock = stock,
                                        isUnlimitedStock = isUnlimited,
                                        minStockAlert = minAlert,
                                        supplierName = supplierName,
                                        supplierPhone = supplierPhone,
                                        imageUri = imageUri
                                    )
                                }
                            )
                        }

                        Screen.HISTORY -> {
                            HistoryScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                                onSelectTransaction = { tx ->
                                    viewModel.activeReceiptTransaction.value = tx
                                    viewModel.navigateTo(Screen.RECEIPT_VIEW)
                                }
                            )
                        }

                        Screen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                        }

                        Screen.EXPENSES -> {
                            ExpenseScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                        }

                        Screen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.POS) }
                            )
                        }

                        Screen.RECEIPT_VIEW -> {
                            if (activeReceipt != null) {
                                ReceiptScreen(
                                    transaction = activeReceipt!!,
                                    storeProfile = storeProfile,
                                    lang = storeProfile.language,
                                    onBack = { viewModel.navigateTo(Screen.POS) },
                                    onNewTransaction = {
                                        viewModel.clearCart()
                                        viewModel.activeReceiptTransaction.value = null
                                        viewModel.navigateTo(Screen.POS)
                                    }
                                )
                            } else {
                                viewModel.navigateTo(Screen.POS)
                            }
                        }
                    }
                }
            }
        }
    }
}

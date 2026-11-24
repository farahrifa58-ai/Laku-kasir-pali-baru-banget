package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.ui.LakuKasirViewModel
import com.example.util.ImageUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SampleProductsRobolectricTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("laku_kasir_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun `seeds 4 editable and deletable sample products with real drawables including 1 kg product`() = runBlocking {
        val viewModel = LakuKasirViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val dao = db.productDao()

        // Wait briefly for IO seeding coroutine in ViewModel.init
        var products = dao.getAllProductsOnce()
        var attempts = 0
        while (products.size < 4 && attempts < 40) {
            Thread.sleep(50)
            products = dao.getAllProductsOnce()
            attempts++
        }

        assertEquals("Expected 4 seeded sample products", 4, products.size)
        assertTrue(
            "Expected at least one product with unit kg",
            products.any { it.unit.equals("kg", ignoreCase = true) }
        )

        // Verify all 4 products resolve to real drawable resource IDs
        products.forEach { product ->
            val model = ImageUtils.resolveProductImageModel(product.imageUri)
            assertTrue("Expected Int drawable res ID for ${product.name}", model is Int)
            assertNotNull("Expected drawable resource to load for ${product.name}", app.getDrawable(model as Int))
        }

        // Verify edit works
        val kgProduct = products.first { it.unit.equals("kg", ignoreCase = true) }
        dao.update(kgProduct.copy(name = "Beras Premium Edited", sellingPrice = 17000.0))
        val updated = dao.getProductById(kgProduct.id)
        assertEquals("Beras Premium Edited", updated?.name)
        assertEquals(17000.0, updated?.sellingPrice ?: 0.0, 0.01)

        // Verify delete works permanently and does not re-seed on next ViewModel init
        dao.delete(updated!!)
        assertEquals(3, dao.getProductCount())
        assertTrue(PreferencesManager(app).hasSeededSampleProducts())

        // Recreate ViewModel -> deleted product must stay deleted
        LakuKasirViewModel(app)
        Thread.sleep(150)
        assertEquals(3, dao.getProductCount())
    }
}

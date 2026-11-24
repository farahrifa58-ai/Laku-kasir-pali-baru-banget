package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.R
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LicenseInfo(
    val isLicensed: Boolean = false,
    val licenseCode: String = "",
    val registeredDeviceId: String = "",
    val businessName: String = ""
)

sealed class ActivationResult {
    data class Success(val info: LicenseInfo) : ActivationResult()
    data object Offline : ActivationResult()
    data class Error(val message: String) : ActivationResult()
}

/**
 * LicenseManager: Sistem Aktivasi Lisensi 1 Perangkat (Tanpa Login Email/Password)
 * dengan Auto-Restore otomatis saat re-install menggunakan Device ID unik,
 * dan penggunaan operasional harian 100% Offline Lokal.
 */
object LicenseManager {

    const val SELLER_WHATSAPP = "081234567890"
    private const val COLLECTION_ACTIVATION_CODES = "activation_codes"
    private const val FIREBASE_ANDROID_APP_ID = "1:1074593799025:android:6142b566ab15aa8bc0f4d0"

    private const val PREFS_NAME = "laku_kasir_license_prefs"
    private const val KEY_IS_LICENSED = "is_licensed"
    private const val KEY_LICENSE_CODE = "license_code"
    private const val KEY_REGISTERED_DEVICE_ID = "registered_device_id"
    private const val KEY_BUSINESS_NAME = "business_name"
    private const val KEY_FALLBACK_UUID = "fallback_device_uuid"

    private val VALID_CODE_REGEX = Regex("^[A-Za-z0-9_\\-\\.]{3,128}$")

    private var prefs: SharedPreferences? = null

    private val _licenseInfoFlow = MutableStateFlow(LicenseInfo())
    val licenseInfoFlow: StateFlow<LicenseInfo> = _licenseInfoFlow.asStateFlow()

    private val _isProFlow = MutableStateFlow(false)
    val isProFlow: StateFlow<Boolean> = _isProFlow.asStateFlow()

    private fun ensurePrefs(context: Context): SharedPreferences {
        return prefs ?: context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .also { prefs = it }
    }

    fun init(context: Context) {
        val sp = ensurePrefs(context)
        val currentDeviceId = getDeviceId(context)
        val isLicensed = sp.getBoolean(KEY_IS_LICENSED, false)
        val savedCode = sp.getString(KEY_LICENSE_CODE, "") ?: ""
        val savedDevice = sp.getString(KEY_REGISTERED_DEVICE_ID, "")?.ifBlank { currentDeviceId } ?: currentDeviceId
        val savedBusiness = sp.getString(KEY_BUSINESS_NAME, "") ?: ""

        val info = LicenseInfo(
            isLicensed = isLicensed,
            licenseCode = savedCode,
            registeredDeviceId = savedDevice,
            businessName = savedBusiness
        )
        _licenseInfoFlow.value = info
        _isProFlow.value = isLicensed
    }

    fun isLicensedLocally(context: Context? = null): Boolean {
        if (context != null && prefs == null) {
            init(context)
        }
        return _licenseInfoFlow.value.isLicensed
    }

    /**
     * Mengambil ID unik perangkat yang persisten antar re-install (ANDROID_ID),
     * dengan fallback UUID permanen jika ANDROID_ID tidak tersedia.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val appContext = context.applicationContext
        val sp = ensurePrefs(appContext)
        val androidId = try {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (_: Exception) {
            null
        }

        val cleanedAndroidId = androidId
            ?.trim()
            ?.lowercase(Locale.US)
            ?.replace(Regex("[^a-z0-9_-]"), "")

        if (!cleanedAndroidId.isNullOrBlank() && cleanedAndroidId.length >= 6 && cleanedAndroidId != "9774d56d682e549c") {
            return cleanedAndroidId
        }

        val cachedFallback = sp.getString(KEY_FALLBACK_UUID, null)
        if (!cachedFallback.isNullOrBlank()) {
            return cachedFallback
        }

        val generated = "dev-${UUID.randomUUID().toString().lowercase(Locale.US)}"
        sp.edit().putString(KEY_FALLBACK_UUID, generated).apply()
        return generated
    }

    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    fun getFirestoreDatabaseId(context: Context): String {
        return context.applicationContext.getString(R.string.firestore_database_id)
    }

    private fun getNamedFirestore(context: Context): FirebaseFirestore {
        return FirebaseFirestore.getInstance(getFirestoreDatabaseId(context))
    }

    /**
     * Memeriksa kecocokan field appId dokumen dengan aplikasi Laku Kasir.
     * Jika appId kosong / tidak ada pada dokumen, filter diabaikan (return true).
     */
    private fun isMatchingLakuKasirAppId(docAppId: String): Boolean {
        val trimmed = docAppId.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed.equals(FIREBASE_ANDROID_APP_ID, ignoreCase = true)) return true

        val normalized = trimmed.lowercase(Locale.US).replace(Regex("[\\s_\\-.]+"), "")
        if (normalized == "all" || normalized == "*") return true

        val exactMatches = setOf(
            "lakukasir",
            "lakukasirpro",
            "lakukasirpos",
            "comaistudiolakukasirpro",
            "169dc4af6d5244fe866a7d319a1964fa"
        )
        if (normalized in exactMatches) return true
        return normalized.contains("lakukasir")
    }

    private fun selectBestMatchingDocument(docs: List<DocumentSnapshot>): DocumentSnapshot? {
        if (docs.isEmpty()) return null
        return docs.firstOrNull { doc ->
            val status = doc.getString("status")?.trim()?.lowercase(Locale.US)
            val appId = doc.getString("appId")?.trim().orEmpty()
            status == "available" && isMatchingLakuKasirAppId(appId)
        } ?: docs.firstOrNull { doc ->
            val appId = doc.getString("appId")?.trim().orEmpty()
            isMatchingLakuKasirAppId(appId)
        } ?: docs.firstOrNull()
    }

    /**
     * Auto-Restore Lisensi saat aplikasi di-install ulang di perangkat yang sama.
     * Jika lokal sudah aktif -> langsung kembalikan lokal (100% offline).
     * Jika lokal belum aktif & online -> cek Firestore activation_codes berdasarkan registeredDeviceId.
     */
    suspend fun attemptAutoRestore(context: Context): LicenseInfo? = withContext(Dispatchers.IO) {
        init(context)
        if (_licenseInfoFlow.value.isLicensed) {
            return@withContext _licenseInfoFlow.value
        }

        if (!isOnline(context)) {
            return@withContext null
        }

        val deviceId = getDeviceId(context)
        return@withContext try {
            val db = getNamedFirestore(context)
            val querySnapshot = db.collection(COLLECTION_ACTIVATION_CODES)
                .whereEqualTo("registeredDeviceId", deviceId)
                .limit(5)
                .get(Source.SERVER)
                .awaitResult()

            val matchedDoc = querySnapshot.documents.firstOrNull { doc ->
                val status = doc.getString("status")?.trim()?.lowercase(Locale.US)
                val appId = doc.getString("appId")?.trim().orEmpty()
                status == "used" && isMatchingLakuKasirAppId(appId)
            }
            if (matchedDoc != null) {
                val restoredCode = matchedDoc.getString("code")
                    ?.trim()
                    ?.uppercase(Locale.US)
                    ?.ifBlank { matchedDoc.id }
                    ?: matchedDoc.id
                val restoredBusinessName = matchedDoc.getString("usedByBusinessName")?.trim().orEmpty()
                saveLocalLicense(
                    context = context,
                    licenseCode = restoredCode,
                    deviceId = deviceId,
                    businessName = restoredBusinessName
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(
                error = e,
                operationType = FirestoreOperationType.LIST,
                path = COLLECTION_ACTIVATION_CODES,
                deviceId = deviceId
            )
            null
        }
    }

    /**
     * Aktivasi kode lisensi sekali pakai (membutuhkan internet 1x saat aktivasi pertama).
     * 1. Cek dokumen langsung via Document ID: activation_codes/{kode}.
     * 2. Jika tidak ditemukan, cari via query where("code", "==", kode) pada koleksi activation_codes.
     * 3. Validasi status == "available" serta kecocokan appId (jika terisi).
     * 4. Update dokumen menggunakan DocumentReference (docRef) yang ditemukan menjadi status "used".
     */
    suspend fun activateWithCode(
        context: Context,
        rawCode: String,
        rawBusinessName: String
    ): ActivationResult = withContext(Dispatchers.IO) {
        val rawTrimmedCode = rawCode.trim()
        val cleanCode = rawTrimmedCode.uppercase(Locale.US)
        val cleanBusinessName = rawBusinessName.trim().take(120)

        if (cleanBusinessName.isEmpty()) {
            return@withContext ActivationResult.Error("Nama Usaha / Toko wajib diisi.")
        }

        if (cleanCode.isEmpty()) {
            return@withContext ActivationResult.Error("Kode aktivasi wajib diisi.")
        }

        if (!isOnline(context)) {
            return@withContext ActivationResult.Offline
        }

        if (!VALID_CODE_REGEX.matches(cleanCode)) {
            return@withContext ActivationResult.Error(
                "Format kode '$cleanCode' tidak valid. Gunakan huruf, angka, garis bawah (_), atau tanda hubung (-)."
            )
        }

        val deviceId = getDeviceId(context).take(128)
        val databaseId = getFirestoreDatabaseId(context)
        var currentStage = "cek dokumen $COLLECTION_ACTIVATION_CODES/$cleanCode"
        var currentPath = "$COLLECTION_ACTIVATION_CODES/$cleanCode"

        return@withContext try {
            val db = getNamedFirestore(context)
            val collectionRef = db.collection(COLLECTION_ACTIVATION_CODES)

            // Langkah 1: Cek langsung via Document ID activation_codes/{kode}
            var matchedDoc: DocumentSnapshot? = null
            val directSnapshot = collectionRef
                .document(cleanCode)
                .get(Source.SERVER)
                .awaitResult()

            if (directSnapshot.exists()) {
                matchedDoc = directSnapshot
            }

            // Langkah 2: Jika tidak ditemukan via Document ID, query berdasarkan field "code"
            if (matchedDoc == null) {
                currentStage = "pencarian field 'code' == '$cleanCode'"
                currentPath = "$COLLECTION_ACTIVATION_CODES?code=$cleanCode"
                val querySnapshot = collectionRef
                    .whereEqualTo("code", cleanCode)
                    .limit(5)
                    .get(Source.SERVER)
                    .awaitResult()
                matchedDoc = selectBestMatchingDocument(querySnapshot.documents)
            }

            // Fallback tambahan jika admin menyimpan kode dengan casing berbeda dari uppercase
            if (matchedDoc == null && rawTrimmedCode != cleanCode) {
                currentStage = "pencarian field 'code' == '$rawTrimmedCode'"
                currentPath = "$COLLECTION_ACTIVATION_CODES?code=$rawTrimmedCode"
                val fallbackSnapshot = collectionRef
                    .whereEqualTo("code", rawTrimmedCode)
                    .limit(5)
                    .get(Source.SERVER)
                    .awaitResult()
                matchedDoc = selectBestMatchingDocument(fallbackSnapshot.documents)
            }

            if (matchedDoc == null) {
                val shortDbId = if (databaseId.length > 10) "...${databaseId.takeLast(8)}" else databaseId
                return@withContext ActivationResult.Error(
                    "Kode '$cleanCode' tidak ditemukan di database ($shortDbId). Sudah dicek via Document ID maupun field 'code'."
                )
            }

            // Langkah 3: Validasi status == "available"
            val rawStatus = matchedDoc.getString("status")?.trim().orEmpty()
            val normalizedStatus = rawStatus.lowercase(Locale.US)
            if (normalizedStatus != "available") {
                val statusText = rawStatus.ifBlank { "kosong" }
                val usedBy = matchedDoc.getString("usedByBusinessName")?.trim().orEmpty()
                val usedBySuffix = if (usedBy.isNotEmpty()) " oleh '$usedBy'" else ""
                return@withContext ActivationResult.Error(
                    "Kode '$cleanCode' sudah berstatus '$statusText'$usedBySuffix dan tidak dapat digunakan lagi."
                )
            }

            // Validasi field appId jika ada dan tidak kosong
            val docAppId = matchedDoc.getString("appId")?.trim().orEmpty()
            if (docAppId.isNotEmpty() && !isMatchingLakuKasirAppId(docAppId)) {
                return@withContext ActivationResult.Error(
                    "Kode '$cleanCode' tidak cocok untuk Laku Kasir (terdaftar untuk appId: '$docAppId')."
                )
            }

            // Langkah 4: Update dokumen menggunakan referensi dokumen (docRef) yang ditemukan
            val matchedDocRef = matchedDoc.reference
            currentStage = "memperbarui status dokumen '${matchedDocRef.id}' menjadi 'used'"
            currentPath = matchedDocRef.path

            val updates = hashMapOf<String, Any>(
                "status" to "used",
                "usedByBusinessName" to cleanBusinessName,
                "registeredDeviceId" to deviceId,
                "usedAt" to FieldValue.serverTimestamp()
            )
            matchedDocRef.update(updates).awaitResult()

            val resolvedCode = matchedDoc.getString("code")
                ?.trim()
                ?.uppercase(Locale.US)
                ?.ifBlank { cleanCode }
                ?: cleanCode

            val info = saveLocalLicense(
                context = context,
                licenseCode = resolvedCode,
                deviceId = deviceId,
                businessName = cleanBusinessName
            )
            ActivationResult.Success(info)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            FirestoreErrorHandler.handleFirestoreError(
                error = e,
                operationType = FirestoreOperationType.WRITE,
                path = currentPath,
                deviceId = deviceId
            )
            val errMessage = e.message?.takeIf { it.isNotBlank() }
                ?: e.localizedMessage
                ?: "FirebaseFirestoreException"
            ActivationResult.Error("Gagal $currentStage [${e.code.name}]: $errMessage")
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(
                error = e,
                operationType = FirestoreOperationType.WRITE,
                path = currentPath,
                deviceId = deviceId
            )
            val errMessage = e.message?.takeIf { it.isNotBlank() }
                ?: e.localizedMessage
                ?: e.javaClass.simpleName
            ActivationResult.Error("Gagal $currentStage: $errMessage")
        }
    }

    private fun saveLocalLicense(
        context: Context,
        licenseCode: String,
        deviceId: String,
        businessName: String
    ): LicenseInfo {
        val sp = ensurePrefs(context)
        sp.edit()
            .putBoolean(KEY_IS_LICENSED, true)
            .putString(KEY_LICENSE_CODE, licenseCode)
            .putString(KEY_REGISTERED_DEVICE_ID, deviceId)
            .putString(KEY_BUSINESS_NAME, businessName)
            .apply()

        val info = LicenseInfo(
            isLicensed = true,
            licenseCode = licenseCode,
            registeredDeviceId = deviceId,
            businessName = businessName
        )
        _licenseInfoFlow.value = info
        _isProFlow.value = true
        return info
    }

    fun maskLicenseCode(code: String): String {
        val trimmed = code.trim()
        if (trimmed.length <= 6) return trimmed.replace(Regex("."), "•")
        val prefix = trimmed.take(4)
        val suffix = trimmed.takeLast(4)
        return "$prefix-••••-$suffix"
    }

    fun contactSellerForLicense(context: Context, sellerPhone: String = SELLER_WHATSAPP) {
        val targetPhone = if (sellerPhone.isNotBlank()) sellerPhone else SELLER_WHATSAPP
        var cleanPhone = targetPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }
        val deviceId = getDeviceId(context)
        val message = "Halo Admin Laku Kasir, saya ingin membeli Kode Aktivasi resmi untuk perangkat saya (ID: $deviceId). Terima kasih!"
        try {
            val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) {
            cont.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (cont.isActive) {
            cont.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (cont.isActive) {
            cont.cancel()
        }
    }
}

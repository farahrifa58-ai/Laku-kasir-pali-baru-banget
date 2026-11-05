package com.example.data.model

data class AppLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val country: String,
    val region: String,
    val flag: String
)

object LanguageCatalog {

    val REGIONS = listOf("Semua", "Asia Tenggara", "Asia", "Eropa & Rusia", "Amerika", "Afrika")

    val ALL_LANGUAGES = listOf(
        // ASIA TENGGARA
        AppLanguage(
            code = "id",
            name = "Bahasa Indonesia",
            nativeName = "Bahasa Indonesia",
            country = "Indonesia",
            region = "Asia Tenggara",
            flag = "🇮🇩"
        ),
        AppLanguage(
            code = "ms",
            name = "Bahasa Melayu",
            nativeName = "Bahasa Melayu",
            country = "Malaysia",
            region = "Asia Tenggara",
            flag = "🇲🇾"
        ),
        AppLanguage(
            code = "vi",
            name = "Vietnam",
            nativeName = "Tiếng Việt",
            country = "Vietnam",
            region = "Asia Tenggara",
            flag = "🇻🇳"
        ),
        AppLanguage(
            code = "th",
            name = "Thailand",
            nativeName = "ภาษาไทย",
            country = "Thailand",
            region = "Asia Tenggara",
            flag = "🇹🇭"
        ),
        AppLanguage(
            code = "fil",
            name = "Filipina",
            nativeName = "Tagalog / Filipino",
            country = "Filipina",
            region = "Asia Tenggara",
            flag = "🇵🇭"
        ),

        // ASIA
        AppLanguage(
            code = "ja",
            name = "Jepang",
            nativeName = "日本語",
            country = "Jepang",
            region = "Asia",
            flag = "🇯🇵"
        ),
        AppLanguage(
            code = "ko",
            name = "Korea Selatan",
            nativeName = "한국어",
            country = "Korea Selatan",
            region = "Asia",
            flag = "🇰🇷"
        ),
        AppLanguage(
            code = "zh",
            name = "Mandarin / Tiongkok",
            nativeName = "中文 (简体)",
            country = "Tiongkok",
            region = "Asia",
            flag = "🇨🇳"
        ),
        AppLanguage(
            code = "hi",
            name = "Hindi / India",
            nativeName = "हिन्दी",
            country = "India",
            region = "Asia",
            flag = "🇮🇳"
        ),
        AppLanguage(
            code = "ar",
            name = "Arab",
            nativeName = "العربية",
            country = "Arab Saudi",
            region = "Asia",
            flag = "🇸🇦"
        ),

        // EROPA & RUSIA
        AppLanguage(
            code = "en",
            name = "Inggris",
            nativeName = "English",
            country = "Inggris / Global",
            region = "Eropa & Rusia",
            flag = "🇬🇧"
        ),
        AppLanguage(
            code = "ru",
            name = "Rusia",
            nativeName = "Русский",
            country = "Rusia",
            region = "Eropa & Rusia",
            flag = "🇷🇺"
        ),
        AppLanguage(
            code = "de",
            name = "Jerman",
            nativeName = "Deutsch",
            country = "Jerman",
            region = "Eropa & Rusia",
            flag = "🇩🇪"
        ),
        AppLanguage(
            code = "fr",
            name = "Prancis",
            nativeName = "Français",
            country = "Prancis",
            region = "Eropa & Rusia",
            flag = "🇫🇷"
        ),
        AppLanguage(
            code = "es",
            name = "Spanyol",
            nativeName = "Español",
            country = "Spanyol",
            region = "Eropa & Rusia",
            flag = "🇪🇸"
        ),
        AppLanguage(
            code = "it",
            name = "Italia",
            nativeName = "Italiano",
            country = "Italia",
            region = "Eropa & Rusia",
            flag = "🇮🇹"
        ),
        AppLanguage(
            code = "nl",
            name = "Belanda",
            nativeName = "Nederlands",
            country = "Belanda",
            region = "Eropa & Rusia",
            flag = "🇳🇱"
        ),

        // AMERIKA
        AppLanguage(
            code = "pt",
            name = "Portugis Brasil",
            nativeName = "Português (Brasil)",
            country = "Brasil",
            region = "Amerika",
            flag = "🇧🇷"
        ),

        // AFRIKA
        AppLanguage(
            code = "sw",
            name = "Swahili / Kiswahili",
            nativeName = "Kiswahili",
            country = "Afrika Timur & Tengah",
            region = "Afrika",
            flag = "🇰🇪"
        )
    )

    fun getLanguage(code: String): AppLanguage {
        return ALL_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
            ?: ALL_LANGUAGES.first()
    }
}

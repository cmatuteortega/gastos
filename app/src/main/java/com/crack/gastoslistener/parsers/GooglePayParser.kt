package com.crack.gastoslistener.parsers

/**
 * Google Wallet (antes Google Pay) manda notificaciones del tipo:
 *   "Has pagado 4,20 € a Cafetería Central"
 *   "Pagaste 4,20 € en Cafetería Central con Google Wallet"
 * En inglés: "You paid €4.20 to Cafetería Central"
 *
 * OJO: el formato exacto cambia con el idioma del móvil y con actualizaciones
 * de la app. Antes de confiar en esto, captura 5-10 notificaciones reales
 * (mira NotificationListener.kt, hay un log del rawText) y ajusta el regex.
 */
class GooglePayParser : NotificationParser {
    override val source = "googlepay"
    override val packageNames = setOf(
        "com.google.android.apps.walletnfcrel", // Google Wallet
        "com.google.android.apps.nbu.paisa.user" // por si acaso, variante regional
    )

    private val amountRegex = Regex("""(\d+[.,]\d{2})\s?€""")
    // "a X" / "en X" / "to X" / "at X" seguido de texto hasta fin de línea
    private val merchantRegex = Regex("""(?:a|en|to|at)\s+([^.\n]+?)(?:\s+con Google Wallet)?$""", RegexOption.IGNORE_CASE)

    override fun parse(title: String, text: String): ParsedPayment? {
        val full = "$title $text"
        // Filtramos notificaciones que no son de pago (recordatorios, promos, etc.)
        val esPago = Regex("has pagado|pagaste|you paid", RegexOption.IGNORE_CASE).containsMatchIn(full)
        if (!esPago) return null

        val amountMatch = amountRegex.find(full) ?: return null
        val amountCents = parseAmountToCents(amountMatch.groupValues[1])

        val merchant = merchantRegex.find(text)?.groupValues?.get(1)?.trim()

        return ParsedPayment(amountCents = amountCents, currency = "EUR", merchant = merchant)
    }
}

/** "4,20" o "1.234,56" -> céntimos. Asume formato europeo. Compartida entre parsers. */
fun parseAmountToCents(raw: String): Long {
    val cleaned = raw.replace(Regex("[^0-9,.]"), "")
    val withDot = if (cleaned.contains(",")) {
        cleaned.replace(".", "").replace(",", ".")
    } else cleaned
    val value = withDot.toDoubleOrNull() ?: 0.0
    return Math.round(value * 100)
}

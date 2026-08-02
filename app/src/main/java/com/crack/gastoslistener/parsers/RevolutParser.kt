package com.crack.gastoslistener.parsers

/**
 * Revolut suele mandar algo como:
 *   "You spent €12.50 at Mercadona"
 *   "Has gastado 12,50 € en Mercadona"  (si el móvil está en español)
 * El símbolo de moneda puede variar (€, $, £), así que capturamos el código
 * de forma más flexible que en los parsers solo-EUR.
 */
class RevolutParser : NotificationParser {
    override val source = "revolut"
    override val packageNames = setOf("com.revolut.revolut")

    private val amountRegex = Regex("""([€$£])\s?(\d+[.,]\d{2})""")
    private val merchantRegex = Regex("""(?:at|en)\s+([^.\n]+)$""", RegexOption.IGNORE_CASE)

    private fun currencyFor(symbol: String) = when (symbol) {
        "€" -> "EUR"
        "$" -> "USD"
        "£" -> "GBP"
        else -> "EUR"
    }

    override fun parse(title: String, text: String): ParsedPayment? {
        val esGasto = Regex("you spent|has gastado", RegexOption.IGNORE_CASE).containsMatchIn(text)
        if (!esGasto) return null

        val match = amountRegex.find(text) ?: return null
        val currency = currencyFor(match.groupValues[1])
        val amountCents = parseAmountToCents(match.groupValues[2])
        val merchant = merchantRegex.find(text)?.groupValues?.get(1)?.trim()

        return ParsedPayment(amountCents = amountCents, currency = currency, merchant = merchant)
    }
}

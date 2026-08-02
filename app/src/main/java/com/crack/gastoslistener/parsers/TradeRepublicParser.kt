package com.crack.gastoslistener.parsers

/**
 * Trade Republic manda sobre todo notificaciones de ejecución de órdenes
 * ("Tu orden de compra de X se ha ejecutado") y movimientos de la cuenta
 * de efectivo asociada a su tarjeta. No es tanto "gasto" como inversión/ahorro,
 * así que aquí solo capturamos pagos con tarjeta TR si la usas para compras,
 * y descartamos confirmaciones de trading.
 */
class TradeRepublicParser : NotificationParser {
    override val source = "traderepublic"
    override val packageNames = setOf("de.traderepublic.app")

    private val amountRegex = Regex("""(\d+[.,]\d{2})\s?€""")

    override fun parse(title: String, text: String): ParsedPayment? {
        val esOrdenTrading = Regex("orden|order|compra de acciones|shares", RegexOption.IGNORE_CASE)
            .containsMatchIn("$title $text")
        if (esOrdenTrading) return null // no lo contamos como gasto de consumo

        val esPagoTarjeta = Regex("pago|payment|has pagado", RegexOption.IGNORE_CASE)
            .containsMatchIn("$title $text")
        if (!esPagoTarjeta) return null

        val match = amountRegex.find(text) ?: return null
        return ParsedPayment(
            amountCents = parseAmountToCents(match.groupValues[1]),
            currency = "EUR",
            merchant = null
        )
    }
}

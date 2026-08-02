package com.crack.gastoslistener.parsers

/**
 * App Santander España. Ejemplos típicos de notificación (verificar contra
 * capturas reales, esto es una primera aproximación):
 *   "Pago con tarjeta"  /  "Has pagado 12,50 € en MERCADONA"
 *   "Bizum enviado"     /  "Has enviado 20,00 € a Juan Pérez"
 *   "Bizum recibido"    /  "Has recibido 15,00 € de Ana García"
 */
class SantanderParser : NotificationParser {
    override val source = "santander"
    override val packageNames = setOf("es.bancosantander.apps")

    private val amountRegex = Regex("""(\d+[.,]\d{2})\s?€""")
    private val comercioRegex = Regex("""en\s+([^.\n]+)$""", RegexOption.IGNORE_CASE)
    private val bizumDestinoRegex = Regex("""a\s+([^.\n]+)$""", RegexOption.IGNORE_CASE)

    override fun parse(title: String, text: String): ParsedPayment? {
        val esPagoTarjeta = title.contains("pago", ignoreCase = true) &&
            text.contains("has pagado", ignoreCase = true)
        val esBizumEnviado = title.contains("bizum", ignoreCase = true) &&
            text.contains("enviado", ignoreCase = true)
        // Los Bizum recibidos son ingresos, no gasto: los ignoramos para el digest de costes
        val esBizumRecibido = title.contains("bizum", ignoreCase = true) &&
            text.contains("recibido", ignoreCase = true)

        if (esBizumRecibido) return null
        if (!esPagoTarjeta && !esBizumEnviado) return null

        val amountMatch = amountRegex.find(text) ?: return null
        val amountCents = parseAmountToCents(amountMatch.groupValues[1])

        val merchant = if (esBizumEnviado) {
            bizumDestinoRegex.find(text)?.groupValues?.get(1)?.trim()?.let { "Bizum a $it" }
        } else {
            comercioRegex.find(text)?.groupValues?.get(1)?.trim()
        }

        return ParsedPayment(amountCents = amountCents, currency = "EUR", merchant = merchant)
    }
}

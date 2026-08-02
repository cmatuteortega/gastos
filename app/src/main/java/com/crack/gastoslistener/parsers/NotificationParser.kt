package com.crack.gastoslistener.parsers

data class ParsedPayment(
    val amountCents: Long,
    val currency: String,
    val merchant: String?
)

interface NotificationParser {
    /** Nombre corto de la fuente, ej. "santander", "googlepay" */
    val source: String

    /** Paquetes Android a los que aplica este parser */
    val packageNames: Set<String>

    /**
     * Intenta extraer un pago del título+texto de la notificación.
     * Devuelve null si el texto no encaja con el patrón esperado
     * (ej. notificaciones de marketing, login, etc. que no son gastos).
     */
    fun parse(title: String, text: String): ParsedPayment?
}

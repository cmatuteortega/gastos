package com.crack.gastoslistener.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,        // "santander", "revolut", "traderepublic", "googlepay"...
    val amountCents: Long,     // en céntimos para evitar líos de redondeo
    val currency: String,      // "EUR", "USD"...
    val merchant: String?,     // comercio si se pudo extraer, si no null
    val rawText: String,       // texto original de la notificación, por si el parser falla
    val timestamp: Date,
    val category: String? = null // se puede rellenar luego con reglas o a mano
)

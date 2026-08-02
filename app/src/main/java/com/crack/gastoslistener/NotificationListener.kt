package com.crack.gastoslistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.crack.gastoslistener.db.AppDatabase
import com.crack.gastoslistener.db.Transaction
import com.crack.gastoslistener.parsers.ParserRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Servicio que el sistema Android invoca cada vez que llega/cambia una
 * notificación en CUALQUIER app, una vez el usuario ha dado el permiso
 * "Acceso a notificaciones" en Ajustes.
 *
 * Todo el procesado ocurre en el propio móvil: no hay llamada de red aquí.
 */
class NotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val parser = ParserRegistry.forPackage(packageName) ?: return // no nos interesa esta app

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()

        // Útil mientras calibras los regex: mira este log con
        // `adb logcat | grep GastosListener` y copia el rawText real
        // de tus notificaciones para ajustar cada parser.
        Log.d("GastosListener", "[$packageName] title=\"$title\" text=\"$text\"")

        val parsed = parser.parse(title, text) ?: return

        scope.launch {
            val dao = AppDatabase.getInstance(applicationContext).transactionDao()
            dao.insert(
                Transaction(
                    source = parser.source,
                    amountCents = parsed.amountCents,
                    currency = parsed.currency,
                    merchant = parsed.merchant,
                    rawText = "$title | $text",
                    timestamp = Date(sbn.postTime)
                )
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // No necesitamos hacer nada al eliminarse la notificación
    }
}

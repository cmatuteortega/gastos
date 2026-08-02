package com.crack.gastoslistener.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crack.gastoslistener.R
import com.crack.gastoslistener.databinding.ActivityMainBinding
import com.crack.gastoslistener.db.AppDatabase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TotalesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TotalesAdapter()
        binding.recyclerTotales.layoutManager = LinearLayoutManager(this)
        binding.recyclerTotales.adapter = adapter

        binding.btnPermiso.setOnClickListener {
            // No hay forma de pedir este permiso con un diálogo estándar:
            // hay que llevar al usuario a la pantalla de Ajustes correspondiente.
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        cargarDigestDelMes()
    }

    private fun cargarDigestDelMes() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        val inicioMes: Date = cal.time
        val ahora = Date()

        val dao = AppDatabase.getInstance(applicationContext).transactionDao()
        dao.getTotalsBySource(inicioMes, ahora)
            .onEach { totales ->
                adapter.submitList(totales)
                binding.statusText.text = if (totales.isEmpty()) {
                    "Aún no hay datos. Activa el acceso a notificaciones y realiza algún pago."
                } else {
                    "Datos desde ${dateFmt(inicioMes)}"
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun dateFmt(d: Date): String =
        java.text.SimpleDateFormat("d MMM", java.util.Locale("es", "ES")).format(d)
}

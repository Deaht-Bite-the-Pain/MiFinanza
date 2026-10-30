package com.udb.mifinanza.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udb.mifinanza.MainActivity
import com.udb.mifinanza.R
import com.udb.mifinanza.data.AlertLevel
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.alertLevel
import com.udb.mifinanza.data.budgetPeriodRange
import com.udb.mifinanza.data.daysLeftInPeriod
import com.udb.mifinanza.data.daysLeftText
import com.udb.mifinanza.data.formatMoney

/**
 * Notificaciones del sistema cuando un presupuesto se acerca a su límite o lo excede.
 *
 * Para no repetir el aviso en cada apertura de la app, por presupuesto se recuerda
 * "inicio del periodo:nivel" y solo se notifica cuando eso cambia (nuevo periodo o nivel más alto).
 */
object BudgetNotifier {
    private const val CHANNEL_ID = "budget_alerts"
    private const val PREFS = "budget_alerts"

    fun check(context: Context, budgets: List<CategoryBudget>, symbol: String) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val canNotify = NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        val editor = prefs.edit()

        budgets.filter { it.id.isNotEmpty() }.forEach { budget ->
            val level = budget.alertLevel()
            val state = "${budgetPeriodRange(budget.period).first}:${level.name}"
            when {
                level == AlertLevel.NONE -> editor.putString(budget.id, state)
                prefs.getString(budget.id, null) == state -> Unit
                // Sin permiso no se registra el estado: se avisará en cuanto el usuario lo conceda.
                canNotify -> {
                    notify(appContext, budget, level, symbol)
                    editor.putString(budget.id, state)
                }
            }
        }
        editor.apply()
    }

    @SuppressLint("MissingPermission")
    private fun notify(context: Context, budget: CategoryBudget, level: AlertLevel, symbol: String) {
        createChannel(context)
        val spent = formatMoney(budget.spent, symbol)
        val limit = formatMoney(budget.limit, symbol)
        val (title, text) = when (level) {
            AlertLevel.EXCEEDED ->
                "Presupuesto de ${budget.category} excedido" to
                    "Gastaste $spent de un límite de $limit. El exceso es de ${formatMoney(budget.exceededAmount, symbol)}."
            else ->
                "${budget.category} cerca del límite" to
                    "Has usado el ${budget.percentage}% de tu presupuesto: $spent de $limit " +
                    "(${daysLeftText(daysLeftInPeriod(budget.period), budget.period)})."
        }
        val openApp = PendingIntent.getActivity(
            context,
            budget.id.hashCode(),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(budget.id.hashCode(), notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Alertas de presupuesto", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Avisos cuando un presupuesto se acerca a su límite o lo excede."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

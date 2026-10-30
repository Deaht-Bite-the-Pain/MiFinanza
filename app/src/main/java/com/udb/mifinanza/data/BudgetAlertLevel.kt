package com.udb.mifinanza.data

/** Qué tan comprometido está un presupuesto respecto a su límite. */
enum class AlertLevel { NONE, NEAR, EXCEEDED }

/**
 * Nivel de alerta de un presupuesto: [AlertLevel.EXCEEDED] si ya superó el límite,
 * [AlertLevel.NEAR] si alcanzó su umbral de aviso y [AlertLevel.NONE] en otro caso.
 * Un presupuesto con las alertas desactivadas nunca avisa.
 */
fun CategoryBudget.alertLevel(): AlertLevel = when {
    !alertEnabled || limit <= 0 -> AlertLevel.NONE
    isExceeded -> AlertLevel.EXCEEDED
    isNearLimit -> AlertLevel.NEAR
    else -> AlertLevel.NONE
}

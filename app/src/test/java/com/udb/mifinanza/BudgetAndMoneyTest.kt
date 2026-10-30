package com.udb.mifinanza

import com.udb.mifinanza.data.AlertLevel
import com.udb.mifinanza.data.CategoryBudget
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.alertLevel
import com.udb.mifinanza.data.budgetPeriodRange
import com.udb.mifinanza.data.daysLeftText
import com.udb.mifinanza.data.filterAmountInput
import com.udb.mifinanza.data.formatMoney
import com.udb.mifinanza.data.spentInPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BudgetAndMoneyTest {

    private fun budget(spent: Double, limit: Double = 100.0, threshold: Int = 80, enabled: Boolean = true) =
        CategoryBudget(id = "b", category = "Ocio", spent = spent, limit = limit, alertEnabled = enabled, alertThreshold = threshold)

    // --- Nivel de alerta -------------------------------------------------------------------

    @Test
    fun `por debajo del umbral no hay alerta`() {
        assertEquals(AlertLevel.NONE, budget(spent = 50.0).alertLevel())
    }

    @Test
    fun `al llegar al umbral avisa que esta cerca`() {
        assertEquals(AlertLevel.NEAR, budget(spent = 80.0).alertLevel())
        assertEquals(AlertLevel.NEAR, budget(spent = 100.0).alertLevel())
    }

    @Test
    fun `al pasar el limite esta excedido`() {
        assertEquals(AlertLevel.EXCEEDED, budget(spent = 100.01).alertLevel())
    }

    @Test
    fun `con las alertas desactivadas nunca avisa`() {
        assertEquals(AlertLevel.NONE, budget(spent = 500.0, enabled = false).alertLevel())
    }

    @Test
    fun `un limite en cero no genera alertas`() {
        assertEquals(AlertLevel.NONE, budget(spent = 10.0, limit = 0.0).alertLevel())
    }

    @Test
    fun `el umbral es configurable`() {
        assertEquals(AlertLevel.NONE, budget(spent = 60.0, threshold = 90).alertLevel())
        assertEquals(AlertLevel.NEAR, budget(spent = 60.0, threshold = 50).alertLevel())
    }

    @Test
    fun `porcentaje restante y exceso`() {
        val b = budget(spent = 132.0, limit = 120.0)
        assertEquals(110, b.percentage)
        assertEquals(12.0, b.exceededAmount, 0.0001)
        assertEquals(0.0, b.remaining, 0.0)
    }

    // --- Periodos -----------------------------------------------------------------------------

    @Test
    fun `el periodo mensual abarca el mes completo`() {
        val now = Calendar.getInstance().apply { set(2026, Calendar.AUGUST, 12, 15, 0, 0) }.timeInMillis
        val (from, to) = budgetPeriodRange("Mensual", now)
        assertEquals(1, Calendar.getInstance().apply { timeInMillis = from }.get(Calendar.DAY_OF_MONTH))
        assertEquals(31, Calendar.getInstance().apply { timeInMillis = to }.get(Calendar.DAY_OF_MONTH))
        assertTrue(now in from..to)
    }

    @Test
    fun `el periodo semanal empieza el lunes`() {
        // 12/08/2026 es miércoles.
        val now = Calendar.getInstance().apply { set(2026, Calendar.AUGUST, 12, 15, 0, 0) }.timeInMillis
        val (from, to) = budgetPeriodRange("Semanal", now)
        assertEquals(Calendar.MONDAY, Calendar.getInstance().apply { timeInMillis = from }.get(Calendar.DAY_OF_WEEK))
        assertEquals(Calendar.SUNDAY, Calendar.getInstance().apply { timeInMillis = to }.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `lo gastado solo cuenta gastos de la categoria dentro del periodo`() {
        val now = System.currentTimeMillis()
        val movements = listOf(
            Movement("1", "a", "Ocio", 10.0, MovementType.GASTO, now),
            Movement("2", "b", "ocio", 5.0, MovementType.GASTO, now),
            Movement("3", "c", "Ocio", 99.0, MovementType.INGRESO, now),
            Movement("4", "d", "Salud", 20.0, MovementType.GASTO, now),
            Movement("5", "e", "Ocio", 40.0, MovementType.GASTO, now - 800L * 86_400_000L)
        )
        assertEquals(15.0, spentInPeriod(movements, "Ocio", "Mensual"), 0.0001)
    }

    @Test
    fun `textos de dias restantes`() {
        assertEquals("es el último día del mes", daysLeftText(0, "Mensual"))
        assertEquals("queda 1 día de la semana", daysLeftText(1, "Semanal"))
        assertEquals("quedan 6 días del año", daysLeftText(6, "Anual"))
    }

    // --- Dinero ---------------------------------------------------------------------------------

    @Test
    fun `formatea montos con separador de miles y simbolo`() {
        assertEquals("$1,284.50", formatMoney(1284.5, "$"))
        assertEquals("Q10", formatMoney(10.0, "Q", decimals = 0))
    }

    @Test
    fun `el campo de monto solo acepta digitos y dos decimales`() {
        assertEquals("12.5", filterAmountInput("12.5", ""))
        assertEquals("12.5", filterAmountInput("12,5", ""))
        assertEquals("12.50", filterAmountInput("12.50", ""))
        assertEquals("12.50", filterAmountInput("12.505", "12.50"))
        assertEquals("7", filterAmountInput("7a", "7"))
    }
}

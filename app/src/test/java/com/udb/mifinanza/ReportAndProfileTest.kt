package com.udb.mifinanza

import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.UserProfile
import com.udb.mifinanza.data.buildMonthlyReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ReportAndProfileTest {
    // 15 de agosto de 2026, 12:00
    private val now = Calendar.getInstance().apply { set(2026, Calendar.AUGUST, 15, 12, 0, 0) }.timeInMillis
    private fun on(month: Int, day: Int) =
        Calendar.getInstance().apply { set(2026, month, day, 10, 0, 0) }.timeInMillis

    private val movements = listOf(
        Movement("1", "Salario", "Salario", 1000.0, MovementType.INGRESO, on(Calendar.AUGUST, 1)),
        Movement("2", "Super", "Alimentación", 200.0, MovementType.GASTO, on(Calendar.AUGUST, 3)),
        Movement("3", "Cine", "Ocio", 50.0, MovementType.GASTO, on(Calendar.AUGUST, 10)),
        Movement("4", "Super julio", "Alimentación", 100.0, MovementType.GASTO, on(Calendar.JULY, 20))
    )

    @Test
    fun `totales del mes en curso`() {
        val r = buildMonthlyReport(movements, monthsAgo = 0, now = now)
        assertEquals(1000.0, r.income, 0.0001)
        assertEquals(250.0, r.expenses, 0.0001)
        assertEquals(750.0, r.net, 0.0001)
        assertEquals(3, r.movementCount)
        assertEquals(75, r.savingsRate)
    }

    @Test
    fun `compara el gasto contra el mes anterior`() {
        val r = buildMonthlyReport(movements, monthsAgo = 0, now = now)
        assertEquals(100.0, r.previousExpenses, 0.0001)
        assertEquals(150, r.expenseChange) // de 100 a 250
    }

    @Test
    fun `promedio diario usa los dias transcurridos`() {
        val r = buildMonthlyReport(movements, monthsAgo = 0, now = now)
        assertEquals(15, r.elapsedDays)
        assertEquals(250.0 / 15, r.dailyAverageExpense, 0.0001)
    }

    @Test
    fun `desglose por categoria ordenado y con porcentaje`() {
        val r = buildMonthlyReport(movements, monthsAgo = 0, now = now)
        assertEquals("Alimentación", r.expensesByCategory.first().category)
        assertEquals(80, r.expensesByCategory.first().share)
        assertEquals("Super", r.largestExpense?.title)
    }

    @Test
    fun `un mes pasado usa todos sus dias y no tiene variacion sin datos previos`() {
        val r = buildMonthlyReport(movements, monthsAgo = 1, now = now)
        assertEquals(31, r.elapsedDays)
        assertEquals(100.0, r.expenses, 0.0001)
        assertNull(r.expenseChange) // junio no tuvo gastos
        assertNull(r.savingsRate)   // julio no tuvo ingresos
    }

    @Test
    fun `un mes sin movimientos queda vacio`() {
        val r = buildMonthlyReport(movements, monthsAgo = 5, now = now)
        assertEquals(0, r.movementCount)
        assertNotNull(r.title)
    }

    // --- Perfil ------------------------------------------------------------------------------

    @Test
    fun `iniciales con nombre y apellido`() {
        assertEquals("MR", UserProfile(name = "María Fernanda Ruiz").initials)
        assertEquals("JA", UserProfile(name = "Josué Alfaro").initials)
        assertEquals("PE", UserProfile(name = "Pedro").initials)
        assertEquals("?", UserProfile(name = "  ").initials)
    }

    @Test
    fun `simbolo segun la moneda elegida`() {
        assertEquals("$", UserProfile(currency = "USD").currencySymbol)
        assertEquals("€", UserProfile(currency = "EUR").currencySymbol)
        assertEquals("Q", UserProfile(currency = "GTQ").currencySymbol)
        assertEquals("$", UserProfile(currency = "XXX").currencySymbol)
    }

    @Test
    fun `el perfil sobrevive al viaje de ida y vuelta a Firestore`() {
        val profile = UserProfile(
            name = "Ana", email = "ana@correo.com", currency = "CRC",
            phone = "7000-0000", institution = "UDB", career = "Ing. Software", semester = "6°"
        )
        assertEquals(profile, UserProfile.fromMap(profile.toMap()))
    }

    @Test
    fun `campos ausentes en Firestore usan valores por defecto`() {
        val p = UserProfile.fromMap(mapOf("name" to "Ana"))
        assertEquals("USD", p.currency)
        assertEquals("", p.phone)
    }
}

package com.udb.mifinanza

import com.udb.mifinanza.data.HistoryFilter
import com.udb.mifinanza.data.Movement
import com.udb.mifinanza.data.MovementType
import com.udb.mifinanza.data.TypeFilter
import com.udb.mifinanza.data.endOfDay
import com.udb.mifinanza.data.filterHistory
import com.udb.mifinanza.data.startOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryFilterTest {
    private val day = 86_400_000L
    private val now = System.currentTimeMillis()

    private val super_ = Movement("1", "Supermercado Santa María", "Alimentación", 42.80, MovementType.GASTO, now, "Efectivo", "compra semanal", listOf("hogar"))
    private val cine = Movement("2", "Cine Multiplaza", "Ocio", 18.0, MovementType.GASTO, now - day, "Tarjeta")
    private val salario = Movement("3", "Salario agosto", "Salario", 1850.0, MovementType.INGRESO, now - 2 * day, "Transferencia")
    private val all = listOf(super_, cine, salario)

    @Test
    fun `sin filtros ni busqueda devuelve todo`() {
        assertEquals(all, all.filterHistory("", HistoryFilter()))
    }

    @Test
    fun `la busqueda ignora tildes y mayusculas`() {
        assertEquals(listOf(super_), all.filterHistory("ALIMENTACION", HistoryFilter()))
        assertEquals(listOf(super_), all.filterHistory("maría", HistoryFilter()))
    }

    @Test
    fun `la busqueda encuentra por etiqueta con o sin numeral`() {
        assertEquals(listOf(super_), all.filterHistory("hogar", HistoryFilter()))
        assertEquals(listOf(super_), all.filterHistory("#hogar", HistoryFilter()))
    }

    @Test
    fun `la busqueda encuentra por descripcion`() {
        assertEquals(listOf(super_), all.filterHistory("semanal", HistoryFilter()))
    }

    @Test
    fun `filtra por tipo`() {
        assertEquals(listOf(salario), all.filterHistory("", HistoryFilter(type = TypeFilter.INGRESOS)))
        assertEquals(listOf(super_, cine), all.filterHistory("", HistoryFilter(type = TypeFilter.GASTOS)))
    }

    @Test
    fun `filtra por categorias sin distinguir mayusculas`() {
        val filter = HistoryFilter(categories = setOf("ocio", "Salario"))
        assertEquals(listOf(cine, salario), all.filterHistory("", filter))
    }

    @Test
    fun `filtra por rango de monto incluyendo los extremos`() {
        assertEquals(listOf(super_, cine), all.filterHistory("", HistoryFilter(minAmount = 18.0, maxAmount = 42.80)))
        assertEquals(listOf(salario), all.filterHistory("", HistoryFilter(minAmount = 100.0)))
    }

    @Test
    fun `filtra por rango de fechas`() {
        val onlyYesterday = HistoryFilter(dateFrom = startOfDay(now - day), dateTo = endOfDay(now - day))
        assertEquals(listOf(cine), all.filterHistory("", onlyYesterday))
    }

    @Test
    fun `los filtros se combinan con y`() {
        val filter = HistoryFilter(type = TypeFilter.GASTOS, categories = setOf("Ocio"))
        assertEquals(listOf(cine), all.filterHistory("", filter))
        assertTrue(all.filterHistory("salario", filter).isEmpty())
    }

    @Test
    fun `cuenta solo los criterios activos`() {
        assertEquals(0, HistoryFilter().activeCount)
        assertEquals(2, HistoryFilter(type = TypeFilter.GASTOS, minAmount = 5.0).activeCount)
    }

    @Test
    fun `normalized corrige rangos invertidos`() {
        val f = HistoryFilter(minAmount = 500.0, maxAmount = 10.0).normalized()
        assertEquals(10.0, f.minAmount!!, 0.0)
        assertEquals(500.0, f.maxAmount!!, 0.0)

        val d = HistoryFilter(dateFrom = startOfDay(now), dateTo = startOfDay(now - 5 * day)).normalized()
        assertTrue(d.dateFrom!! <= d.dateTo!!)
    }
}

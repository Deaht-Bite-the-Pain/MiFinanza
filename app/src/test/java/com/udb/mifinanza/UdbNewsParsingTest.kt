package com.udb.mifinanza

import com.udb.mifinanza.data.NewsCategory
import com.udb.mifinanza.data.htmlToText
import com.udb.mifinanza.data.isRecentNews
import com.udb.mifinanza.data.parseSpanishDate
import com.udb.mifinanza.data.parseUdbArticle
import com.udb.mifinanza.data.parseUdbEvents
import com.udb.mifinanza.data.parseUdbNews
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class UdbNewsParsingTest {

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, minute)
        }.timeInMillis

    // 30 de octubre de 2026, 10:00 am
    private val now = millis(2026, Calendar.OCTOBER, 30, 10)

    private val newsHtml = """
        <article class="news-body-last-first">
          <a href="https://www.udb.edu.sv/udb/noticia/udb-fortalece-la-investigacion" class="news-body-last-first-title">
            <h3> UDB fortalece la investigación </h3>
          </a>
        </article>
        <article class="news-body-last-first">
          <figure><a href="https://www.udb.edu.sv/udb/noticia/udb-fortalece-la-investigacion">
            <img src="https://www.udb.edu.sv/udb_files/recursos_noticias/2536/facebook.jpg" alt=""></a></figure>
        </article>
        <article class="newnav" data-aos="fade-right">
          <div class="newnav-head-image"><a href="https://www.udb.edu.sv/udb/noticia/expo-la-tierra">
            <img src="https://www.udb.edu.sv/udb_files/recursos_noticias/2543/thumb.jpg"></a></div>
          <div class="newnav-body-title"><a href="https://www.udb.edu.sv/udb/noticia/expo-la-tierra"><h2>UDB inaugura la exposición “La Tierra” </h2></a></div>
          <div class="newnav-body-date">
            <p>
              30 octubre, 2026     </p>
          </div>
        </article>
        <article class = "new"><div class = "new-head"><div class = "new-head-image"><a href = "https://www.udb.edu.sv/udb/noticia/centro-pastoral" ><img src="https://www.udb.edu.sv/udb_files/recursos_noticias/2534/imagen.jpg"></a></div></div><div class = "new-body"><div class = "new-body-title"><a href = "https://www.udb.edu.sv/udb/noticia/centro-pastoral"><h2>UDB coloca la primera piedra del Centro Pastoral &#34;Cafarelli&#34; </h2></a></div><div class = "new-body-date"><p>20 octubre, 2026</p></div><div class = "new-body-intro"><p>El proyecto genera espacios de convivencia. </p></div></div></article>
        <article class = "new"><div class = "new-body"><div class = "new-body-title"><a href = "https://www.udb.edu.sv/udb/noticia/startup-stem"><h2>Primer lugar en Startup STEM</h2></a></div><div class = "new-body-date"><p>15 septiembre, 2026</p></div></div></article>
    """.trimIndent()

    @Test
    fun parsesNewsListAndMergesFeaturedBlocks() {
        val items = parseUdbNews(newsHtml)
        assertEquals(4, items.size)

        val featured = items.first { it.link.endsWith("udb-fortalece-la-investigacion") }
        assertEquals("UDB fortalece la investigación", featured.title)
        assertEquals("https://www.udb.edu.sv/udb_files/recursos_noticias/2536/imagen.jpg", featured.imageUrl)
        assertNull(featured.publishedAt)

        val pastoral = items.first { it.link.endsWith("centro-pastoral") }
        assertEquals("UDB coloca la primera piedra del Centro Pastoral \"Cafarelli\"", pastoral.title)
        assertEquals("El proyecto genera espacios de convivencia.", pastoral.summary)
        assertEquals(millis(2026, Calendar.OCTOBER, 20), pastoral.publishedAt)
        assertTrue(items.all { it.category == NewsCategory.NOTICIAS })
    }

    @Test
    fun onlyRecentNewsAreKept() {
        val recent = parseUdbNews(newsHtml).filter { isRecentNews(it, now) }.map { it.link.substringAfterLast('/') }
        // La de septiembre tiene más de 30 días y la destacada sin fecha no se puede comprobar.
        assertEquals(listOf("expo-la-tierra", "centro-pastoral"), recent)
    }

    private fun event(day: String, month: String, id: Int, title: String, footer: String) =
        """<div class="search-agenda-content-body-results-item"><div class="search-agenda-content-body-results-item-head"><div class="search-agenda-results-item-head-day">$day</div><div class="search-agenda-results-item-head-month">$month</div></div><div class="search-agenda-content-body-results-item-body"><div class="search-agenda-results-item-body-title"><a href="https://www.udb.edu.sv/udb/evento/$id" target="blank" ><h3>$title</h3></a></div><div class="search-agenda-results-item-body-content"><p> - Campus Soyapango</p></div></div><div class="search-agenda-content-body-results-item-footer"><span>$footer</span></div></div>"""

    @Test
    fun keepsOnlyEventsThatHaveNotEnded() {
        val html = listOf(
            event("21", "Oct", 1, "Trámite ya terminado", "Desde 21 Oct hasta el 21 Oct"),
            event("30", "Oct", 2, "Charla de esta mañana", "Desde 7:00 am hasta el 9:00 am"),
            event("30", "Oct", 3, "Charla de esta noche", "Desde 5:30 pm hasta el 7:00 pm"),
            event("26", "Oct", 4, "Semana en curso", "Desde 26 Oct hasta el 06 Nov"),
            event("23", "Nov", 5, "Ceremonias de graduación", "Desde 23 Nov hasta el 27 Nov")
        ).joinToString("")

        val events = parseUdbEvents(html, now)
        assertEquals(listOf(3, 4, 5), events.map { it.link.substringAfterLast('/').toInt() })

        val evening = events.first()
        assertEquals(millis(2026, Calendar.OCTOBER, 30, 17, 30), evening.publishedAt)
        assertEquals("Campus Soyapango", evening.source)
        assertEquals(NewsCategory.EVENTOS, evening.category)

        val graduation = events.last()
        assertEquals(millis(2026, Calendar.NOVEMBER, 23), graduation.publishedAt)
        assertTrue(graduation.endsAt!! > millis(2026, Calendar.NOVEMBER, 27, 23))
        assertEquals("Desde 23 Nov hasta el 27 Nov", graduation.schedule)
    }

    @Test
    fun eventsInJanuaryListedInDecemberBelongToNextYear() {
        val december = millis(2026, Calendar.DECEMBER, 20)
        val events = parseUdbEvents(event("12", "Ene", 9, "Inicio de clases", "Desde 7:00 am hasta el 8:00 pm"), december)
        assertEquals(millis(2027, Calendar.JANUARY, 12, 7), events.single().publishedAt)
    }

    @Test
    fun parsesArticleBody() {
        val html = """
            <meta property="og:description" content="Un espacio de intercambio. ">
            <div class="dnew-date">
              <span>27 de Oct, 2026</span>
            </div>
            <div class="dnew-body-content">
              <p dir="ltr">Primer párrafo con <strong>negritas</strong>.</p>
              <p dir="ltr"><img alt="" src="/udb_files/foto.jpg" /></p>
              <p>Segundo&nbsp;párrafo.</p>
              <!-- Código para generar las Galerias -->
            </div>
            <div class="dnew-foot"><p>Pie que no es parte de la nota</p></div>
        """.trimIndent()

        val article = parseUdbArticle(html)
        assertEquals(millis(2026, Calendar.OCTOBER, 27), article.publishedAt)
        assertEquals("Un espacio de intercambio.", article.summary)
        assertEquals(listOf("Primer párrafo con negritas.", "Segundo párrafo."), article.paragraphs)
    }

    @Test
    fun parsesSpanishDatesAndEntities() {
        assertEquals(millis(2026, Calendar.SEPTEMBER, 30), parseSpanishDate("30 septiembre, 2026"))
        assertEquals(millis(2026, Calendar.OCTOBER, 2), parseSpanishDate("02 de Oct, 2026"))
        assertNull(parseSpanishDate("sin fecha"))
        assertEquals("“La Tierra” & más", htmlToText("&ldquo;La Tierra&rdquo; &amp; <b>más</b>"))
        assertFalse(htmlToText("<p> a </p>").startsWith(" "))
    }
}

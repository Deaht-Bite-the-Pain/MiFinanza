package com.udb.mifinanza.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Tipos de publicación que se toman del sitio de la Universidad Don Bosco. */
enum class NewsCategory(val label: String, val tag: String) {
    NOTICIAS("Noticias", "Noticia"),
    EVENTOS("Eventos", "Evento")
}

data class NewsItem(
    val title: String,
    val link: String,
    /** Noticias: "Universidad Don Bosco". Eventos: campus donde se realiza. */
    val source: String,
    /** Noticias: fecha de publicación. Eventos: fecha de inicio. */
    val publishedAt: Long?,
    val category: NewsCategory,
    val summary: String = "",
    val imageUrl: String? = null,
    /** Solo eventos: cuándo termina; los que ya terminaron no se muestran. */
    val endsAt: Long? = null,
    /** Solo eventos: horario tal como lo publica la UDB ("Desde 23 Nov hasta el 27 Nov"). */
    val schedule: String = ""
)

/** Contenido de la página de una noticia: fecha, resumen y párrafos del cuerpo. */
data class NewsArticle(
    val publishedAt: Long?,
    val summary: String,
    val paragraphs: List<String>
)

/** Noticias recientes y eventos vigentes publicados en www.udb.edu.sv. */
class NewsRepository {

    /** Carga noticias y eventos en paralelo; solo falla si no se pudo obtener ninguna página. */
    suspend fun loadNews(now: Long = System.currentTimeMillis()): Result<List<NewsItem>> = runCatching {
        coroutineScope {
            val news = async { runCatching { loadRecentNews(now) } }
            // La agenda solo trae eventos próximos; la del mes incluye los que ya empezaron y siguen en curso.
            val events = listOf(EVENTS_URL, EVENTS_MONTH_URL).map { url ->
                async { runCatching { parseUdbEvents(download(url), now) } }
            }
            val results = listOf(news.await()) + events.awaitAll()
            if (results.all { it.isFailure }) throw results.first().exceptionOrNull() ?: IllegalStateException()
            val items = results.mapNotNull { it.getOrNull() }.flatten().distinctBy { it.link }
            items.filter { it.category == NewsCategory.NOTICIAS }.sortedByDescending { it.publishedAt ?: 0L } +
                items.filter { it.category == NewsCategory.EVENTOS }.sortedBy { it.publishedAt ?: Long.MAX_VALUE }
        }
    }

    /** Cuerpo de una noticia, para mostrarlo en el detalle. */
    suspend fun loadArticle(link: String): Result<NewsArticle> = runCatching { parseUdbArticle(download(link)) }

    private suspend fun loadRecentNews(now: Long): List<NewsItem> = coroutineScope {
        // Las noticias destacadas no traen fecha o resumen en el listado: se completan con su propia página.
        parseUdbNews(download(NEWS_URL)).map { item ->
            async {
                if (item.publishedAt != null && item.summary.isNotBlank()) return@async item
                val article = runCatching { parseUdbArticle(download(item.link)) }.getOrNull()
                item.copy(
                    publishedAt = item.publishedAt ?: article?.publishedAt,
                    summary = item.summary.ifBlank { article?.summary.orEmpty() }
                )
            }
        }.awaitAll().filter { isRecentNews(it, now) }
    }

    private suspend fun download(url: String): String = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Android) MiFinanza")
        }
        try {
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            connection.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val NEWS_URL = "https://www.udb.edu.sv/udb/noticias"
        const val EVENTS_URL = "https://www.udb.edu.sv/udb/eventos"
        const val EVENTS_MONTH_URL = "https://www.udb.edu.sv/udb/eventos/mes"
    }
}

/** Días que una noticia se considera reciente. */
const val NEWS_MAX_AGE_DAYS = 30L

internal const val UDB_SOURCE = "Universidad Don Bosco"
private const val UDB_FILES = "https://www.udb.edu.sv/udb_files/recursos_noticias"

private val NEWS_BLOCK = Regex("""<article\s+class\s*=\s*"(?:new|newnav|news-body-last-first)"[^>]*>(.*?)</article>""", RegexOption.DOT_MATCHES_ALL)
private val NEWS_LINK = Regex("""href\s*=\s*"(https?://[^"]*/udb/noticia/[^"]+)"""")
private val NEWS_IMAGE_ID = Regex("""recursos_noticias/(\d+)/""")
private val NEWS_TITLE = Regex("""<h[23]>(.*?)</h[23]>""", RegexOption.DOT_MATCHES_ALL)
private val NEWS_DATE = Regex("""-body-date">\s*<p>(.*?)</p>""", RegexOption.DOT_MATCHES_ALL)
private val NEWS_INTRO = Regex("""-body-intro">\s*<p>(.*?)</p>""", RegexOption.DOT_MATCHES_ALL)

private val EVENT_BLOCK = Regex(
    """head-day">\s*(\d{1,2})\s*</div>\s*<div class="search-agenda-results-item-head-month">\s*(\S+?)\s*</div>""" +
        """.*?href="([^"]*/udb/evento/\d+)"[^>]*>\s*<h3>(.*?)</h3>""" +
        """.*?item-body-content">\s*<p>(.*?)</p>""" +
        """.*?item-footer">\s*<span>(.*?)</span>""",
    RegexOption.DOT_MATCHES_ALL
)
private const val ALL_DAY = "Desde 12:00 am hasta el 11:59 pm"
private val SCHEDULE =Regex("""Desde\s+(.+?)\s+hasta\s+(?:el\s+)?(.+)""", RegexOption.IGNORE_CASE)
private val DAY_MONTH = Regex("""^(\d{1,2})\s+([A-Za-zÁÉÍÓÚáéíóú]+)""")
private val TIME = Regex("""^(\d{1,2}):(\d{2})\s*([ap])\.?\s*m""", RegexOption.IGNORE_CASE)
private val FULL_DATE = Regex("""(\d{1,2})\s+(?:de\s+)?([A-Za-zÁÉÍÓÚáéíóú]+)\.?,?\s+(?:de\s+)?(\d{4})""")

private val ARTICLE_DATE = Regex("""class="dnew-date">\s*<span>(.*?)</span>""", RegexOption.DOT_MATCHES_ALL)
private val ARTICLE_SUMMARY = Regex("""<meta property="og:description" content="([^"]*)"""")
private val ARTICLE_BODY = Regex("""class="dnew-body-content">(.*?)(?:<!--\s*Código para generar|class="dnew-foot")""", RegexOption.DOT_MATCHES_ALL)
private val PARAGRAPH = Regex("""<p[^>]*>(.*?)</p>""", RegexOption.DOT_MATCHES_ALL)

/**
 * Noticias del listado de la UDB (sin filtrar por fecha). La destacada viene partida en dos bloques
 * (título por un lado, imagen por otro), así que los bloques se unen por enlace.
 */
internal fun parseUdbNews(html: String): List<NewsItem> =
    NEWS_BLOCK.findAll(html).mapNotNull { match ->
        val block = match.groupValues[1]
        val link = NEWS_LINK.find(block)?.groupValues?.get(1) ?: return@mapNotNull null
        NewsItem(
            title = NEWS_TITLE.find(block)?.groupValues?.get(1)?.let(::htmlToText).orEmpty(),
            link = link,
            source = UDB_SOURCE,
            publishedAt = NEWS_DATE.find(block)?.groupValues?.get(1)?.let { parseSpanishDate(htmlToText(it)) },
            category = NewsCategory.NOTICIAS,
            summary = NEWS_INTRO.find(block)?.groupValues?.get(1)?.let(::htmlToText).orEmpty(),
            imageUrl = NEWS_IMAGE_ID.find(block)?.groupValues?.get(1)?.let { "$UDB_FILES/$it/imagen.jpg" }
        )
    }.groupBy { it.link }.values.mapNotNull { parts ->
        val title = parts.firstOrNull { it.title.isNotEmpty() }?.title ?: return@mapNotNull null
        parts.first().copy(
            title = title,
            publishedAt = parts.firstNotNullOfOrNull { it.publishedAt },
            summary = parts.firstOrNull { it.summary.isNotEmpty() }?.summary.orEmpty(),
            imageUrl = parts.firstNotNullOfOrNull { it.imageUrl }
        )
    }

/** Una noticia es reciente si se publicó en los últimos [NEWS_MAX_AGE_DAYS] días. */
internal fun isRecentNews(item: NewsItem, now: Long): Boolean {
    val date = item.publishedAt ?: return false
    return now - date <= TimeUnit.DAYS.toMillis(NEWS_MAX_AGE_DAYS)
}

/** Eventos de la agenda de la UDB que todavía no han terminado. */
internal fun parseUdbEvents(html: String, now: Long): List<NewsItem> =
    EVENT_BLOCK.findAll(html).mapNotNull { match ->
        val (day, month, link, rawTitle, place, rawSchedule) = match.destructured
        val title = htmlToText(rawTitle)
        val schedule = htmlToText(rawSchedule)
        val range = eventRange(day.toInt(), monthIndex(month) ?: return@mapNotNull null, schedule, now)
        if (title.isEmpty() || range.second < now) return@mapNotNull null
        NewsItem(
            title = title,
            link = link,
            source = htmlToText(place).removePrefix("-").trim().ifEmpty { UDB_SOURCE },
            publishedAt = range.first,
            category = NewsCategory.EVENTOS,
            endsAt = range.second,
            schedule = if (schedule.equals(ALL_DAY, ignoreCase = true)) "Todo el día" else schedule
        )
    }.toList()

/** Página de una noticia: fecha, resumen y párrafos de texto (sin imágenes). */
internal fun parseUdbArticle(html: String): NewsArticle {
    val body = ARTICLE_BODY.find(html)?.groupValues?.get(1).orEmpty()
    return NewsArticle(
        publishedAt = ARTICLE_DATE.find(html)?.groupValues?.get(1)?.let { parseSpanishDate(htmlToText(it)) },
        summary = ARTICLE_SUMMARY.find(html)?.groupValues?.get(1)?.let(::htmlToText).orEmpty(),
        paragraphs = PARAGRAPH.findAll(body).map { htmlToText(it.groupValues[1]) }.filter { it.isNotEmpty() }.toList()
    )
}

/**
 * Inicio y fin de un evento. La agenda no publica el año: se toma el más cercano a [now].
 * El horario puede ser de horas ("Desde 5:30 pm hasta el 7:00 pm") o de fechas ("Desde 23 Nov hasta el 27 Nov").
 */
private fun eventRange(day: Int, month: Int, schedule: String, now: Long): Pair<Long, Long> {
    val year = nearestYear(month, now)
    val parts = SCHEDULE.find(schedule)?.destructured
    val start = parts?.component1()?.let { TIME.find(it) }
        ?.let { dateMillis(year, month, day, hour24(it), it.groupValues[2].toInt()) }
        ?: dateMillis(year, month, day, 0, 0)
    val endText = parts?.component2().orEmpty()
    val endTime = TIME.find(endText)
    val endDate = DAY_MONTH.find(endText)
    val end = when {
        endTime != null -> dateMillis(year, month, day, hour24(endTime), endTime.groupValues[2].toInt(), 59)
        endDate != null -> {
            val endMonth = monthIndex(endDate.groupValues[2]) ?: month
            val endYear = if (endMonth < month) year + 1 else year
            dateMillis(endYear, endMonth, endDate.groupValues[1].toInt(), 23, 59, 59)
        }
        else -> dateMillis(year, month, day, 23, 59, 59)
    }
    return start to maxOf(start, end)
}

private fun nearestYear(month: Int, now: Long): Int {
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val year = today.get(Calendar.YEAR)
    val diff = month - today.get(Calendar.MONTH)
    return when {
        diff < -6 -> year + 1
        diff > 6 -> year - 1
        else -> year
    }
}

private fun hour24(time: MatchResult): Int {
    val hour = time.groupValues[1].toInt() % 12
    return if (time.groupValues[3].equals("p", ignoreCase = true)) hour + 12 else hour
}

private fun dateMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): Long =
    Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, minute, second)
    }.timeInMillis

/** "27 octubre, 2026" o "27 de Oct, 2026". */
internal fun parseSpanishDate(text: String): Long? {
    val match = FULL_DATE.find(text) ?: return null
    val month = monthIndex(match.groupValues[2]) ?: return null
    return dateMillis(match.groupValues[3].toInt(), month, match.groupValues[1].toInt(), 0, 0)
}

/** Mes (0-11) a partir de su nombre o abreviatura en español. */
private fun monthIndex(name: String): Int? =
    when (name.lowercase().take(3)) {
        "ene" -> 0; "feb" -> 1; "mar" -> 2; "abr" -> 3; "may" -> 4; "jun" -> 5
        "jul" -> 6; "ago" -> 7; "sep", "set" -> 8; "oct" -> 9; "nov" -> 10; "dic" -> 11
        else -> null
    }

private val LINE_BREAK = Regex("""<br\s*/?>""",RegexOption.IGNORE_CASE)
private val TAG = Regex("<[^>]+>")
private val NUMERIC_ENTITY = Regex("&#(x?)([0-9a-fA-F]+);")
private val SPACES = Regex("\\s+")
private val NAMED_ENTITIES = mapOf(
    "&nbsp;" to " ", "&quot;" to "\"", "&apos;" to "'", "&lt;" to "<", "&gt;" to ">",
    "&aacute;" to "á", "&eacute;" to "é", "&iacute;" to "í", "&oacute;" to "ó", "&uacute;" to "ú",
    "&ntilde;" to "ñ", "&Ntilde;" to "Ñ", "&ldquo;" to "“", "&rdquo;" to "”", "&ndash;" to "–", "&mdash;" to "—"
)

/** Quita etiquetas HTML, decodifica entidades y normaliza espacios. */
internal fun htmlToText(html: String): String {
    var text = TAG.replace(LINE_BREAK.replace(html, " "), "")
    text = NUMERIC_ENTITY.replace(text) { m ->
        val code = m.groupValues[2].toIntOrNull(if (m.groupValues[1].isEmpty()) 10 else 16)
        code?.let { String(Character.toChars(it)) } ?: m.value
    }
    NAMED_ENTITIES.forEach { (entity, value) -> text = text.replace(entity, value) }
    text = text.replace("&amp;", "&")
    return SPACES.replace(text, " ").trim()
}

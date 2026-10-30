package com.udb.mifinanza.data

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/** Categorías de noticias académicas; cada una se consulta con su propia búsqueda. */
enum class NewsCategory(val label: String, val query: String) {
    BECAS("Becas", "becas universitarias El Salvador"),
    FINANZAS("Finanzas", "educación financiera ahorro estudiantes universitarios El Salvador"),
    CREDITOS("Créditos", "crédito educativo estudiantes El Salvador")
}

data class NewsItem(
    val title: String,
    val link: String,
    val source: String,
    val publishedAt: Long?,
    val category: NewsCategory
)

/** Noticias de becas, finanzas y créditos educativos tomadas del RSS público de Google Noticias. */
class NewsRepository {

    /** Carga las tres categorías en paralelo; solo falla si ninguna se pudo obtener. */
    suspend fun loadNews(): Result<List<NewsItem>> = runCatching {
        coroutineScope {
            val results = NewsCategory.entries.map { category ->
                async { runCatching { fetch(category) } }
            }.awaitAll()
            if (results.all { it.isFailure }) throw results.first().exceptionOrNull() ?: IllegalStateException()
            results.mapNotNull { it.getOrNull() }
                .flatten()
                .distinctBy { it.link }
                .sortedByDescending { it.publishedAt ?: 0L }
        }
    }

    private suspend fun fetch(category: NewsCategory): List<NewsItem> = withContext(Dispatchers.IO) {
        val url = URL(
            "https://news.google.com/rss/search?q=" + URLEncoder.encode(category.query, "UTF-8") +
                "&hl=es-419&gl=SV&ceid=SV:es-419"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = true
        }
        try {
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            connection.inputStream.use { parse(it, category) }
                .sortedByDescending { it.publishedAt ?: 0L }
                .take(MAX_PER_CATEGORY)
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(input: InputStream, category: NewsCategory): List<NewsItem> {
        val parser = Xml.newPullParser().apply { setInput(input, null) }
        val items = mutableListOf<NewsItem>()
        var fields: MutableMap<String, String>? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                when {
                    parser.name == "item" -> fields = mutableMapOf()
                    fields != null && parser.name in ITEM_FIELDS -> fields[parser.name] = parser.nextText().trim()
                }
            } else if (parser.eventType == XmlPullParser.END_TAG && parser.name == "item") {
                fields?.let { toItem(it, category) }?.let(items::add)
                fields = null
            }
        }
        return items
    }

    private fun toItem(fields: Map<String, String>, category: NewsCategory): NewsItem? {
        val link = fields["link"].orEmpty()
        val source = fields["source"].orEmpty()
        // Google Noticias agrega " - Medio" al final del título.
        val title = fields["title"].orEmpty().removeSuffix(" - $source").trim()
        if (title.isEmpty() || link.isEmpty()) return null
        val date = fields["pubDate"]?.let {
            runCatching { SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(it)?.time }.getOrNull()
        }
        return NewsItem(title = title, link = link, source = source, publishedAt = date, category = category)
    }

    private companion object {
        const val MAX_PER_CATEGORY = 10
        val ITEM_FIELDS = setOf("title", "link", "pubDate", "source")
    }
}

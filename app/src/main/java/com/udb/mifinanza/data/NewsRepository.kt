package com.udb.mifinanza.data

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

data class NewsItem(
    val title: String,
    val link: String,
    val source: String,
    val publishedAt: Long?
)

/** Noticias de becas y educación superior tomadas del RSS público de Google Noticias. */
class NewsRepository(
    private val query: String = "becas universitarias El Salvador"
) {
    suspend fun loadNews(): Result<List<NewsItem>> = runCatching {
        withContext(Dispatchers.IO) {
            val url = URL(
                "https://news.google.com/rss/search?q=" + URLEncoder.encode(query, "UTF-8") +
                    "&hl=es-419&gl=SV&ceid=SV:es-419"
            )
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                instanceFollowRedirects = true
            }
            try {
                if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
                connection.inputStream.use(::parse)
                    .sortedByDescending { it.publishedAt ?: 0L }
                    .take(MAX_ITEMS)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun parse(input: InputStream): List<NewsItem> {
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
                fields?.let { toItem(it) }?.let(items::add)
                fields = null
            }
        }
        return items
    }

    private fun toItem(fields: Map<String, String>): NewsItem? {
        val link = fields["link"].orEmpty()
        val source = fields["source"].orEmpty()
        // Google Noticias agrega " - Medio" al final del título.
        val title = fields["title"].orEmpty().removeSuffix(" - $source").trim()
        if (title.isEmpty() || link.isEmpty()) return null
        val date = fields["pubDate"]?.let {
            runCatching { SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(it)?.time }.getOrNull()
        }
        return NewsItem(title = title, link = link, source = source, publishedAt = date)
    }

    private companion object {
        const val MAX_ITEMS = 25
        val ITEM_FIELDS = setOf("title", "link", "pubDate", "source")
    }
}

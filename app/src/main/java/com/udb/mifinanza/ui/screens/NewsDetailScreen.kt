package com.udb.mifinanza.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.NewsCategory
import com.udb.mifinanza.data.NewsItem
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.PrimaryButton
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Accent100
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Accent700
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink300
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink600
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.MiFinanzaTheme
import com.udb.mifinanza.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/** Detalle de una noticia o evento de la UDB: imagen, fecha, título, texto y acciones para compartir o abrir la página. */
@Composable
fun NewsDetailScreen(
    item: NewsItem,
    onBack: () -> Unit,
    paragraphs: List<String> = emptyList(),
    isLoadingArticle: Boolean = false
) {
    StatusBarIcons(dark = true)
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppTopBar(title = "Noticia", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val image = rememberRemoteImage(item.imageUrl)
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                // Eventos (o imagen aún sin cargar): bloque con el tipo de publicación.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Accent100, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.category.tag.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = Accent700
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(item.category)
                Text(
                    listOfNotNull(newsDateLabel(item), item.source.ifBlank { null }).joinToString(" · "),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Ink500
                )
            }

            Text(
                item.title,
                fontFamily = Manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                lineHeight = 29.sp,
                color = Ink950
            )

            if (item.category == NewsCategory.EVENTOS) {
                EventInfo("Horario", item.schedule)
                EventInfo("Lugar", item.source)
            } else if (item.summary.isNotBlank()) {
                Text(item.summary, fontFamily = Manrope, fontSize = 15.sp, lineHeight = 23.sp, color = Ink950)
            }
            paragraphs.forEach { paragraph ->
                Text(paragraph, fontFamily = Manrope, fontSize = 14.sp, lineHeight = 22.sp, color = Ink600)
            }
            if (isLoadingArticle) {
                Text("Cargando la noticia completa…", fontFamily = Manrope, fontSize = 13.sp, color = Ink500)
            } else if (paragraphs.isEmpty()) {
                Text(
                    if (item.category == NewsCategory.EVENTOS) "Más información en la agenda de la Universidad Don Bosco."
                    else "Para leer la nota completa, ábrela en el navegador.",
                    fontFamily = Manrope,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Ink600
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink200))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, Ink300, RoundedCornerShape(6.dp))
                    .clickable {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.link}")
                        }
                        runCatching { context.startActivity(Intent.createChooser(send, "Compartir")) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = Accent500, modifier = Modifier.size(20.dp))
            }
            PrimaryButton(
                text = "Abrir en el navegador",
                onClick = { runCatching { uriHandler.openUri(item.link) } },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EventInfo(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, letterSpacing = 0.6.sp, color = Ink500)
        Text(value, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink950)
    }
}

/** Descarga la imagen reducida a ~1000 px de ancho; null mientras carga o si falla. */
@Composable
private fun rememberRemoteImage(url: String?): ImageBitmap? {
    val image by produceState<ImageBitmap?>(null, url) {
        value = url?.let {
            withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = URL(it).openStream().use { stream -> stream.readBytes() }
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    var sample = 1
                    while (bounds.outWidth / (sample * 2) >= 1000) sample *= 2
                    val options = BitmapFactory.Options().apply { inSampleSize = sample }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
                }.getOrNull()
            }
        }
    }
    return image
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NewsDetailPreview() {
    MiFinanzaTheme {
        NewsDetailScreen(
            item = NewsItem(
                title = "Sandra Cauffman comparte su experiencia en la NASA con la Comunidad UDB",
                link = "https://www.udb.edu.sv/udb/noticias",
                source = "Universidad Don Bosco",
                publishedAt = System.currentTimeMillis(),
                category = NewsCategory.NOTICIAS,
                summary = "Un espacio de intercambio de conocimientos y experiencias en torno al desarrollo de capacidades científicas y aeroespaciales."
            ),
            onBack = {}
        )
    }
}

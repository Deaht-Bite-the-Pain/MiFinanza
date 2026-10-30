package com.udb.mifinanza.ui.screens

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.NewsCategory
import com.udb.mifinanza.data.NewsItem
import com.udb.mifinanza.data.formatDate
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

/** Detalle de una noticia: categoría, fecha, fuente, título y acciones para compartir o abrir el artículo. */
@Composable
fun NewsDetailScreen(item: NewsItem, onBack: () -> Unit) {
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
            // El RSS no trae imagen: se muestra un bloque con la categoría en su lugar.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Accent100, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.category.label.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = Accent700
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(item.category)
                Text(
                    listOfNotNull(item.publishedAt?.let(::formatDate), item.source.ifBlank { null }).joinToString(" · "),
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

            Text(
                buildString {
                    append("Esta noticia fue publicada")
                    if (item.source.isNotBlank()) append(" por ${item.source}")
                    append(". Para leer el artículo completo, ábrelo en el navegador.")
                },
                fontFamily = Manrope,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Ink600
            )
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
                        runCatching { context.startActivity(Intent.createChooser(send, "Compartir noticia")) }
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NewsDetailPreview() {
    MiFinanzaTheme {
        NewsDetailScreen(
            item = NewsItem(
                title = "Convocatoria de becas de manutención 2026-2027 abre postulaciones",
                link = "https://example.com",
                source = "EduNoticias",
                publishedAt = System.currentTimeMillis(),
                category = NewsCategory.BECAS
            ),
            onBack = {}
        )
    }
}

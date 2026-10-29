package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.NewsItem
import com.udb.mifinanza.data.formatDate
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.EmptyState
import com.udb.mifinanza.ui.components.LoadingBox
import com.udb.mifinanza.ui.components.StatusBarIcons
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink50
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Ink950
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.White

/** Noticias sobre becas y educación superior; cada una se abre en el navegador. */
@Composable
fun NewsScreen(
    items: List<NewsItem>,
    loaded: Boolean,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    StatusBarIcons(dark = true)
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppTopBar(title = "Noticias académicas", onBack = onBack)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                isLoading && items.isEmpty() -> LoadingBox()
                error != null && items.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Info,
                    title = "No se pudieron cargar las noticias",
                    message = error,
                    actionText = "Reintentar",
                    onAction = onRetry
                )
                loaded && items.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Sin noticias por ahora",
                    message = "Vuelve más tarde para ver novedades sobre becas y educación superior.",
                    actionText = "Actualizar",
                    onAction = onRetry
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.link }) { item ->
                        NewsCard(item) { runCatching { uriHandler.openUri(item.link) } }
                    }
                    item {
                        Text(
                            "Fuente: Google Noticias",
                            fontFamily = Manrope,
                            fontSize = 11.5.sp,
                            color = Ink500,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, Ink200, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            item.title,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Ink950,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            listOfNotNull(item.source.ifBlank { null }, item.publishedAt?.let(::formatDate)).joinToString(" · "),
            fontFamily = Manrope,
            fontSize = 12.sp,
            color = Ink500
        )
    }
}

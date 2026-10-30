package com.udb.mifinanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.data.NewsCategory
import com.udb.mifinanza.data.NewsItem
import com.udb.mifinanza.data.formatDate
import com.udb.mifinanza.ui.components.AppTopBar
import com.udb.mifinanza.ui.components.EmptyState
import com.udb.mifinanza.ui.components.LoadingBox
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
import com.udb.mifinanza.ui.theme.White

/** Noticias sobre becas, finanzas y créditos educativos, con filtro por categoría. */
@Composable
fun NewsScreen(
    items: List<NewsItem>,
    loaded: Boolean,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onItemClick: (NewsItem) -> Unit = {}
) {
    StatusBarIcons(dark = true)
    // null = "Todas". Se guarda el nombre para que sobreviva a la rotación.
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = NewsCategory.entries.firstOrNull { it.name == selectedName }
    val visible = if (selected == null) items else items.filter { it.category == selected }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink50)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppTopBar(title = "Noticias académicas", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryChip("Todas", selected == null) { selectedName = null }
            NewsCategory.entries.forEach { category ->
                CategoryChip(category.label, selected == category) { selectedName = category.name }
            }
        }

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
                visible.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Sin noticias en esta categoría",
                    message = "Prueba con otra categoría o vuelve a mostrar todas las noticias.",
                    actionText = "Ver todas",
                    onAction = { selectedName = null }
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.link }) { item ->
                        NewsCard(item) { onItemClick(item) }
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
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = if (selected) White else Ink600,
        modifier = Modifier
            .then(if (selected) Modifier.background(Accent500, CircleShape) else Modifier.border(1.dp, Ink300, CircleShape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

/** Etiqueta de categoría en mayúsculas (BECAS, FINANZAS, CRÉDITOS). */
@Composable
internal fun CategoryTag(category: NewsCategory) {
    Text(
        category.label.uppercase(),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.6.sp,
        color = Accent700,
        modifier = Modifier
            .background(Accent100, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryTag(item.category)
            item.publishedAt?.let {
                Text(
                    formatDate(it),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    color = Ink500
                )
            }
        }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.source,
                fontFamily = Manrope,
                fontSize = 12.sp,
                color = Ink500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text("Leer más →", fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Accent500)
        }
    }
}

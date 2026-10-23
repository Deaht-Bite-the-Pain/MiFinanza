package com.udb.mifinanza.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udb.mifinanza.ui.theme.Accent500
import com.udb.mifinanza.ui.theme.Ink200
import com.udb.mifinanza.ui.theme.Ink500
import com.udb.mifinanza.ui.theme.Manrope
import com.udb.mifinanza.ui.theme.White

enum class NavTab(val label: String, val paths: List<String>) {
    Inicio("Inicio", listOf("M3 10.5 12 3l9 7.5V21H3z", "M9.5 21v-6h5v6")),
    Movimientos("Movimientos", listOf("M7 21V5m0 0L4 8m3-3 3 3", "M17 3v16m0 0 3-3m-3 3-3-3")),
    Presupuestos(
        "Presupuestos",
        listOf("M12 3.5a8.5 8.5 0 1 0 0 17a8.5 8.5 0 1 0 0-17z", "M12 3.5A8.5 8.5 0 0 1 20.5 12H12z")
    ),
    Historial("Historial", listOf("M12 3.5a8.5 8.5 0 1 0 0 17a8.5 8.5 0 1 0 0-17z", "M12 7.5V12l3 2"))
}

private fun tabIcon(paths: List<String>): ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        paths.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()

/** Barra inferior de 4 pestañas (Inicio, Movimientos, Presupuestos, Historial). */
@Composable
fun BottomNavBar(active: NavTab, onSelect: (NavTab) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(White)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Ink200))
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val color = if (tab == active) Accent500 else Ink500
                val icon = remember(tab) { tabIcon(tab.paths) }
                Column(
                    modifier = Modifier.weight(1f).clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(icon, contentDescription = tab.label, tint = color, modifier = Modifier.size(22.dp))
                    Text(tab.label, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, color = color)
                }
            }
        }
    }
}

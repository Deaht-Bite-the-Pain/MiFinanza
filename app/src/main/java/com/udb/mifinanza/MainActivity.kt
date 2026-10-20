package com.udb.mifinanza

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.udb.mifinanza.navigation.AppNavigation
import com.udb.mifinanza.ui.theme.MiFinanzaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiFinanzaTheme {
                AppNavigation()
            }
        }
    }
}

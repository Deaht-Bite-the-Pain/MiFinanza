# MiFinanza

Aplicación Android de finanzas estudiantiles: registra ingresos y gastos, define presupuestos con alertas,
consulta el historial con búsqueda y filtros, revisa reportes mensuales y lee noticias académicas.

Proyecto de cátedra de **DSM441 – Desarrollo de Software para Móviles** (Universidad Don Bosco).

**Autores:** Julián Andrés Santacruz Palomino · Josué Engelberto Alfaro Trinidad

## Funcionalidades

| Módulo | Qué incluye |
|---|---|
| Acceso y perfil | Bienvenida, registro, inicio de sesión, "Recordarme", recuperación de contraseña por enlace al correo, perfil y edición del perfil |
| Inicio | Saldo, ingresos y gastos del mes, alerta del presupuesto más comprometido, gráfico de los últimos 6 meses y movimientos recientes |
| Movimientos | Crear, editar y eliminar ingresos y gastos (categoría, fecha, método de pago, descripción y etiquetas) |
| Presupuestos y alertas | Presupuestos por categoría con periodo mensual, semanal o anual; barra de progreso; aviso dentro de la app y **notificación del sistema** al llegar al umbral o excederse |
| Historial | Movimientos agrupados por día, búsqueda (sin distinguir tildes) y filtros por tipo, fechas, categorías y monto |
| Reportes | Resumen mensual con balance, ahorro, comparación con el mes anterior y desglose por categoría |
| Noticias académicas | Becas, finanzas y créditos educativos desde un RSS público, con filtro por categoría, detalle, compartir y apertura en el navegador |
| Estados vacíos y de error | Sin movimientos, sin resultados y sin conexión con opción de reintentar |

## Tecnología

- **Kotlin** y **Jetpack Compose** (Material 3), navegación con `navigation-compose`.
- **Firebase Authentication** (correo y contraseña) y **Cloud Firestore**.
- Arquitectura por capas: `data/` (modelos y repositorios), `ui/` (pantallas, componentes y ViewModels), `navigation/`, `notifications/`.
- Noticias: RSS público de Google Noticias leído con `HttpURLConnection` y `XmlPullParser`.
- `minSdk 26`, `compileSdk 35`.

### Estructura de datos en Firestore

```
users/{uid}                    perfil (nombre, correo, moneda, teléfono, institución, carrera, semestre…)
users/{uid}/movements/{id}     movimientos
users/{uid}/budgets/{id}       presupuestos (categoría, límite, periodo, alerta y umbral)
```

Las reglas de seguridad (`firestore.rules`) permiten a cada usuario leer y escribir solo sus propios datos.

## Cómo ejecutarlo

Requisitos: Android Studio reciente y JDK 17.

1. Clona el repositorio y ábrelo con **Android Studio** (la carpeta que contiene `settings.gradle.kts`).
2. Crea un proyecto en [Firebase](https://console.firebase.google.com) y registra una app Android con el
   paquete **`com.udb.mifinanza`**.
3. Descarga `google-services.json` y colócalo en `app/`. **No se incluye en el repositorio** porque contiene
   la configuración privada de cada proyecto Firebase; cada integrante debe descargar el suyo.
4. En Firebase activa **Authentication → Correo electrónico/contraseña** y crea **Firestore Database**.
5. Publica las reglas de [`firestore.rules`](firestore.rules) en *Firestore Database → Reglas*.
6. Ejecuta la configuración **app** en un emulador o dispositivo.

> Si varios integrantes usan el mismo proyecto de Firebase, basta con que ambos descarguen el mismo
> `google-services.json` desde *Configuración del proyecto*.

## Pruebas

Pruebas unitarias de la lógica de negocio (filtros e historial, presupuestos y alertas, periodos,
reportes mensuales, formato de dinero y perfil):

```bash
./gradlew :app:testDebugUnitTest
```

## Notas

- **Recuperar contraseña:** Firebase envía un *enlace* al correo para crear la nueva contraseña; no envía
  códigos de 6 dígitos.
- **Notificaciones:** desde Android 13 la app pide el permiso de notificaciones al entrar al Inicio.
  Cada alerta se muestra una sola vez por presupuesto y periodo.
- **Noticias:** se consultan tres búsquedas (becas, finanzas y créditos educativos) en Google Noticias;
  el RSS no incluye imágenes ni el cuerpo del artículo, por eso el detalle abre la nota completa en el navegador.

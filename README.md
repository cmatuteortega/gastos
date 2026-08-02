# Gastos Listener — esqueleto Android

App 100% local (sin permiso de INTERNET) que escucha las notificaciones de
apps de pago (Google Wallet, Santander, Revolut, Trade Republic...) para
construir un digest de gasto por plataforma. Nada sale del móvil.

## Cómo funciona

1. `NotificationListener` (un `NotificationListenerService`) recibe TODAS las
   notificaciones del sistema, pero solo procesa las de los paquetes
   registrados en `ParserRegistry`.
2. Cada app tiene un `NotificationParser` con regex adaptados a su formato
   de texto habitual.
3. Lo parseado se guarda en Room (`gastos.db`, local, sin sincronizar).
4. `MainActivity` muestra el total agregado por fuente del mes en curso.

## Pasos para probarlo

1. Abre el proyecto en Android Studio (Koala o superior).
2. Instala en tu móvil físico (el listener no funciona bien en emulador
   sin generar notificaciones de prueba manualmente).
3. Abre la app y pulsa "Activar acceso a notificaciones" → te lleva a
   Ajustes → actívalo para "Gastos Listener".
4. Haz un pago real pequeño con Google Wallet o mira el log:
   ```
   adb logcat | grep GastosListener
   ```
   Ahí verás el `title` y `text` EXACTOS de la notificación real.

## Lo más importante: calibrar los parsers

Los regex que he puesto son una primera aproximación basada en formatos
típicos, **no** están verificados contra las notificaciones reales de tu
móvil (idioma, versión de la app, etc. cambian el texto). El flujo de
calibración:

1. Genera una notificación real (paga algo pequeño).
2. Copia el `rawText` del log.
3. Ajusta el regex en el parser correspondiente
   (`GooglePayParser.kt`, `SantanderParser.kt`, etc.) hasta que
   `parse()` devuelva el importe/comercio correctos.
4. Repite para cada banco/fintech que uses.

## Añadir más bancos

1. Averigua el `packageName` real:
   ```
   adb shell pm list packages | grep -i nombreapp
   ```
2. Crea `TuBancoParser.kt` implementando `NotificationParser`.
3. Regístralo en `ParserRegistry.kt`.

## Compilar el APK con GitHub Actions (sin Android Studio)

El proyecto incluye `.github/workflows/build.yml`, que compila un APK debug
en los runners de GitHub cada vez que subes cambios, y lo deja como artifact
descargable. Pasos:

1. Crea un repositorio nuevo en GitHub (puede ser privado).
2. Sube este proyecto:
   ```
   cd gastoslistener
   git init
   git add .
   git commit -m "esqueleto inicial"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
   git push -u origin main
   ```
3. En GitHub, ve a la pestaña **Actions** de tu repo. Debería arrancar solo
   el workflow "Build APK" (o lánzalo a mano con el botón "Run workflow").
4. Cuando termine (2-4 min), entra en esa ejecución → sección **Artifacts**
   abajo del todo → descarga `gastoslistener-debug-apk`. Es un .zip que
   contiene `app-debug.apk`.
5. Pasa el APK a tu móvil (Drive, email, cable...) y ábrelo desde el
   gestor de archivos. Te pedirá permitir "instalar apps de origen
   desconocido" para esa fuente — actívalo solo para esta instalación.

Nota: es un APK **debug**, sin firma de release. Vale perfectamente para
uso personal en tu propio móvil, pero no lo subas a la Play Store tal cual.

Cada vez que quieras una nueva versión tras tocar el código (por ejemplo,
al calibrar los regex de los parsers), simplemente:
```
git add .
git commit -m "ajusto parser santander"
git push
```
y el Action vuelve a compilarte el APK automáticamente.

## Limitaciones conocidas

- **Solo Android.** iOS no permite leer notificaciones de otras apps.
- Si el banco cambia el texto de sus notificaciones (rediseño de app,
  cambio de idioma), el parser deja de acertar hasta que lo actualices.
- Las notificaciones "agrupadas" (varias en una) o las que llegan ya
  descartadas por el sistema antes de que el listener esté activo no
  se capturan — solo ves lo que llega desde que activas el permiso.
- Bizum llega a través de la notificación de tu banco, no hay app propia
  de Bizum que escuchar.
- El digest actual es solo un ejemplo (totales por fuente del mes). Se
  puede extender fácilmente a categorías, exportación CSV, gráficos, etc.

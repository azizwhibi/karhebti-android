package com.example.karhebti_android.ui.components

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Composant pour afficher une carte avec plusieurs marqueurs (client et garage)
 * et une ligne entre eux
 */
@Composable
fun OpenStreetMapViewMultiple(
    clientLatitude: Double,
    clientLongitude: Double,
    garageLatitude: Double?,
    garageLongitude: Double?,
    modifier: Modifier = Modifier,
    zoom: Double = 13.0
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        initializeOsmDroid(context)
        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                val clientPoint = GeoPoint(clientLatitude, clientLongitude)

                // Centrer sur le client par défaut
                controller.setZoom(zoom)
                controller.setCenter(clientPoint)

                // Marqueur client (rouge)
                val clientMarker = Marker(this).apply {
                    position = clientPoint
                    title = "Votre position"
                    snippet = "Client"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(clientMarker)

                // Si position du garage disponible
                if (garageLatitude != null && garageLongitude != null) {
                    val garagePoint = GeoPoint(garageLatitude, garageLongitude)

                    // Marqueur garage (bleu)
                    val garageMarker = Marker(this).apply {
                        position = garagePoint
                        title = "Assistant"
                        snippet = "Garage"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(garageMarker)

                    // Ligne entre client et garage
                    val line = Polyline().apply {
                        addPoint(clientPoint)
                        addPoint(garagePoint)
                        outlinePaint.color = Color.BLUE
                        outlinePaint.strokeWidth = 5f
                    }
                    overlays.add(line)

                    // Ajuster le zoom pour voir les deux points
                    val boundingBox = org.osmdroid.util.BoundingBox(
                        maxOf(clientLatitude, garageLatitude),
                        maxOf(clientLongitude, garageLongitude),
                        minOf(clientLatitude, garageLatitude),
                        minOf(clientLongitude, garageLongitude)
                    )

                    post {
                        zoomToBoundingBox(boundingBox, true, 100)
                    }
                }

                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                )
            }
        },
        update = { mapView ->
            // Mise à jour si les positions changent
            mapView.overlays.clear()

            val clientPoint = GeoPoint(clientLatitude, clientLongitude)

            val clientMarker = Marker(mapView).apply {
                position = clientPoint
                title = "Votre position"
                snippet = "Client"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(clientMarker)

            if (garageLatitude != null && garageLongitude != null) {
                val garagePoint = GeoPoint(garageLatitude, garageLongitude)

                val garageMarker = Marker(mapView).apply {
                    position = garagePoint
                    title = "Assistant"
                    snippet = "Garage"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(garageMarker)

                val line = Polyline().apply {
                    addPoint(clientPoint)
                    addPoint(garagePoint)
                    outlinePaint.color = Color.BLUE
                    outlinePaint.strokeWidth = 5f
                }
                mapView.overlays.add(line)
            }

            mapView.invalidate()
        }
    )
}

private fun initializeOsmDroid(context: Context) {
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidBasePath = context.filesDir
        osmdroidTileCache = context.cacheDir
    }
}


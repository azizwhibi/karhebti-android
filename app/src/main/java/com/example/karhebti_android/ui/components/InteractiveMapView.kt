package com.example.karhebti_android.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Carte interactive OpenStreetMap permettant de sélectionner une position
 * L'utilisateur peut cliquer sur la carte pour choisir sa position
 *
 * @param latitude Latitude initiale
 * @param longitude Longitude initiale
 * @param modifier Modificateur Compose
 * @param zoom Niveau de zoom initial (défaut: 15.0)
 * @param onLocationSelected Callback appelé quand l'utilisateur sélectionne une position
 */
@Composable
fun InteractiveMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoom: Double = 15.0,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }

    // Initialiser la configuration osmdroid une seule fois
    DisposableEffect(Unit) {
        initializeOsmDroid(context)
        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                // Configuration de la carte
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Centrer la carte sur la position initiale
                val startPoint = GeoPoint(latitude, longitude)
                controller.setZoom(zoom)
                controller.setCenter(startPoint)

                // Créer le marqueur initial
                val marker = Marker(this).apply {
                    position = startPoint
                    title = "Votre position"
                    snippet = "Appuyez sur la carte pour changer"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    isDraggable = true

                    // Callback quand le marqueur est déplacé
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker?) {
                            // Optionnel: afficher les coordonnées pendant le drag
                        }

                        override fun onMarkerDragEnd(marker: Marker?) {
                            marker?.let {
                                onLocationSelected(it.position.latitude, it.position.longitude)
                                android.util.Log.d("InteractiveMapView",
                                    "Position sélectionnée (drag): ${it.position.latitude}, ${it.position.longitude}")
                            }
                        }

                        override fun onMarkerDragStart(marker: Marker?) {
                            // Optionnel: actions au début du drag
                        }
                    })
                }

                selectedMarker = marker
                overlays.add(marker)

                // Ajouter un overlay pour détecter les clics sur la carte
                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                        // Déplacer le marqueur à la position cliquée
                        selectedMarker?.position = geoPoint

                        // Notifier le callback
                        onLocationSelected(geoPoint.latitude, geoPoint.longitude)

                        android.util.Log.d("InteractiveMapView",
                            "Position sélectionnée (tap): ${geoPoint.latitude}, ${geoPoint.longitude}")

                        // Rafraîchir la carte
                        invalidate()

                        return true
                    }

                    override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                        // Long press: même comportement que tap simple
                        selectedMarker?.position = geoPoint
                        onLocationSelected(geoPoint.latitude, geoPoint.longitude)
                        invalidate()
                        return true
                    }
                })

                overlays.add(0, mapEventsOverlay) // Ajouter en premier pour capturer les événements

                // Activer le zoom avec les boutons
                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                )
            }
        },
        update = { mapView ->
            // Mettre à jour la position si elle change de l'extérieur
            val newPoint = GeoPoint(latitude, longitude)

            // Mettre à jour le marqueur
            selectedMarker?.position = newPoint

            // Optionnel: recentrer la carte (commenté pour ne pas perturber l'utilisateur)
            // mapView.controller.setCenter(newPoint)

            mapView.invalidate()
        }
    )
}

/**
 * Initialise la configuration osmdroid
 * Doit être appelé une seule fois au démarrage de l'application
 */
private fun initializeOsmDroid(context: Context) {
    // Configuration du cache et du user agent
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
    }
}


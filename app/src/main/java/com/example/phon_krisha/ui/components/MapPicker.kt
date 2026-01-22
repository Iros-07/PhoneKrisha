//MapPicker.kt
package com.example.phon_krisha.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

@Composable
fun MapPicker(
    modifier: Modifier = Modifier,
    initialLat: Double = 43.2567,
    initialLon: Double = 76.9563,
    readOnly: Boolean = false,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
) {
    val initialPoint = Point.fromLngLat(initialLon, initialLat)
    var selectedPoint by remember { mutableStateOf(initialPoint) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx: Context ->
                MapLibre.getInstance(ctx)

                val mapView = MapView(ctx)
                mapViewRef = mapView

                mapView.getMapAsync { map ->
                    map.setStyle("https://demotiles.maplibre.org/style.json") { style ->

                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(initialLat, initialLon))
                            .zoom(12.0)
                            .build()

                        val sourceId = "marker-source"
                        val layerId = "marker-layer"

                        style.addSource(
                            GeoJsonSource(
                                sourceId,
                                FeatureCollection.fromFeatures(
                                    arrayOf(Feature.fromGeometry(initialPoint))
                                )
                            )
                        )

                        style.addLayer(
                            SymbolLayer(layerId, sourceId)
                                .withProperties(
                                    PropertyFactory.iconImage("marker-icon"),
                                    PropertyFactory.iconAllowOverlap(true),
                                    PropertyFactory.iconIgnorePlacement(true)
                                )
                        )

                        if (!readOnly) {
                            map.addOnMapClickListener { latLng ->
                                val newPoint = Point.fromLngLat(latLng.longitude, latLng.latitude)
                                selectedPoint = newPoint

                                // Обновляем маркер
                                style.getSourceAs<GeoJsonSource>(sourceId)?.setGeoJson(
                                    FeatureCollection.fromFeatures(
                                        arrayOf(Feature.fromGeometry(newPoint))
                                    )
                                )

                                onLocationSelected(latLng.latitude, latLng.longitude)
                                true
                            }
                        }
                    }
                }

                mapView
            }
        )

        if (!readOnly) {
            Button(
                onClick = {
                    onLocationSelected(selectedPoint.latitude(), selectedPoint.longitude())
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Выбрать это место")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDestroy()
        }
    }
}
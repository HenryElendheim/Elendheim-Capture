package com.truecapture.app

import android.graphics.ColorMatrix
import org.json.JSONArray
import org.json.JSONObject

// A colour look. matrix == null means the original photo (no change). The same
// look is used for the live preview and baked into the saved photo.
data class Filter(val name: String, val matrix: ColorMatrix?)

object Filters {

    // The five built-in looks, plus the untouched original.
    val standard: List<Filter> = listOf(
        Filter("Original", null),
        Filter(
            "Warm",
            ColorMatrix(
                floatArrayOf(
                    1.15f, 0f, 0f, 0f, 12f,
                    0f, 1.02f, 0f, 0f, 4f,
                    0f, 0f, 0.9f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        ),
        Filter(
            "Cool",
            ColorMatrix(
                floatArrayOf(
                    0.9f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1.15f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        ),
        Filter(
            "Vintage",
            ColorMatrix(
                floatArrayOf(
                    0.9f, 0.45f, 0.18f, 0f, 0f,
                    0.32f, 0.8f, 0.16f, 0f, 0f,
                    0.24f, 0.34f, 0.6f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        ),
        Filter("Mono", ColorMatrix().apply { setSaturation(0f) }),
        Filter("Vivid", ColorMatrix().apply { setSaturation(1.6f) })
    )

    // Build a look from three simple sliders. warmth and brightness are roughly
    // -50 to 50, saturation is 0 (grey) to 2 (punchy).
    fun custom(name: String, warmth: Float, brightness: Float, saturation: Float): Filter {
        val cm = ColorMatrix()
        cm.setSaturation(saturation)
        val warmAndBright = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, warmth + brightness,
                0f, 1f, 0f, 0f, brightness,
                0f, 0f, 1f, 0f, -warmth + brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(warmAndBright)
        return Filter(name, cm)
    }

    // Load the user's saved custom filters from the stored JSON.
    fun loadCustom(json: String?): List<Filter> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                custom(
                    o.getString("name"),
                    o.getDouble("warmth").toFloat(),
                    o.getDouble("brightness").toFloat(),
                    o.getDouble("saturation").toFloat()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add one custom filter to the stored JSON and return the new JSON string.
    fun appendCustom(
        json: String?,
        name: String,
        warmth: Float,
        brightness: Float,
        saturation: Float
    ): String {
        val array = try {
            if (json.isNullOrEmpty()) JSONArray() else JSONArray(json)
        } catch (e: Exception) {
            JSONArray()
        }
        array.put(
            JSONObject()
                .put("name", name)
                .put("warmth", warmth.toDouble())
                .put("brightness", brightness.toDouble())
                .put("saturation", saturation.toDouble())
        )
        return array.toString()
    }
}

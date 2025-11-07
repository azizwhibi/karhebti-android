package com.example.karhebti_android.data.api

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Custom deserializer for user field that can be either a String (ID) or an object
 */
class FlexibleUserDeserializer : JsonDeserializer<String?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                json.asString
            }
            json.isJsonObject -> {
                // Extract the _id field from the user object
                json.asJsonObject.get("_id")?.asString
            }
            else -> null
        }
    }
}

/**
 * Custom deserializer for garage field that can be either a String (ID) or an object
 */
class FlexibleGarageDeserializer : JsonDeserializer<String?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                json.asString
            }
            json.isJsonObject -> {
                // Extract the _id field from the garage object
                json.asJsonObject.get("_id")?.asString
            }
            else -> null
        }
    }
}

/**
 * Custom deserializer for voiture/car field that can be either a String (ID) or an object
 */
class FlexibleCarDeserializer : JsonDeserializer<String?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                json.asString
            }
            json.isJsonObject -> {
                // Extract the _id field from the car object
                json.asJsonObject.get("_id")?.asString
            }
            else -> null
        }
    }
}


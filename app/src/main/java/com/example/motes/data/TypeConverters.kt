package com.example.motes.data

import androidx.room.TypeConverter
import com.example.motes.data.entity.ChecklistItem
import org.json.JSONArray
import org.json.JSONObject

class AppTypeConverters {
    @TypeConverter
    fun checklistItemsToString(items: List<ChecklistItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            jsonArray.put(
                JSONObject().apply {
                    put("text", item.text)
                    put("isChecked", item.isChecked)
                }
            )
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun stringToChecklistItems(value: String): List<ChecklistItem> {
        if (value.isBlank()) return emptyList()

        val jsonArray = JSONArray(value)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val itemObject = jsonArray.getJSONObject(index)
                add(
                    ChecklistItem(
                        text = itemObject.optString("text"),
                        isChecked = itemObject.optBoolean("isChecked")
                    )
                )
            }
        }
    }

    @TypeConverter
    fun strokePathsToString(paths: List<String>): String = JSONArray(paths).toString()

    @TypeConverter
    fun stringToStrokePaths(value: String): List<String> {
        if (value.isBlank()) return emptyList()

        val jsonArray = JSONArray(value)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                add(jsonArray.optString(index))
            }
        }
    }
}

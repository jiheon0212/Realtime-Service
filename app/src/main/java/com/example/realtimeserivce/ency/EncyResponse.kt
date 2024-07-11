package com.example.realtimeserivce.ency

import com.google.gson.annotations.SerializedName

data class EncyResponse(
    @SerializedName("items") val items: List<Item>
)
data class Item(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
) {
    val trimResults: Pair<String, String>
        get() = Pair(title.replace(Regex("<[^>]*>"), ""),
            description.replace(Regex("<[^>]*>"), ""))
}

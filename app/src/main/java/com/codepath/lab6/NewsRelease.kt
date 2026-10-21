package com.codepath.lab6

import android.support.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NewsReleaseResponse(
    @SerialName("data")
    val data: List<NewsRelease>?
)

@Keep
@Serializable
data class NewsRelease(
    @SerialName("title")
    val title: String?,
    @SerialName("abstract")
    val abstract: String?,
    @SerialName("url")
    val url: String?,
    @SerialName("parkCode")
    val parkCode: String?,
    @SerialName("image")
    val image: NewsImage?
) {
    val imageUrl: String?
        get() = image?.url?.takeIf { it.isNotEmpty() }
}

@Keep
@Serializable
data class NewsImage(
    @SerialName("url")
    val url: String?
)

package com.aslnstbk.unsplash.common.models.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PhotoUrlsApiData(
    @JsonProperty("raw")
    val raw: String?,
    @JsonProperty("full")
    val full: String?,
    @JsonProperty("regular")
    val regular: String?,
    @JsonProperty("small")
    val small: String?,
    @JsonProperty("thumb")
    val thumb: String?
)
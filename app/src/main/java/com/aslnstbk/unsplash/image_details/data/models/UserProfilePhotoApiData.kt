package com.aslnstbk.unsplash.image_details.data.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserProfilePhotoApiData(
    @JsonProperty("small")
    val small: String?,
    @JsonProperty("medium")
    val medium: String?,
    @JsonProperty("large")
    val large: String?
)
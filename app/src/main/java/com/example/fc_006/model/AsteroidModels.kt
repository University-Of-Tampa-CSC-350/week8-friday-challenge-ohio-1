package com.example.fc_006.model

import com.google.gson.annotations.SerializedName

data class AsteroidBrowseResponse(
    val page: PageInfo,
    @SerializedName("near_earth_objects")
    val nearEarthObjects: List<Asteroid>
)

data class PageInfo(
    val size: Int,
    @SerializedName("total_elements")
    val totalElements: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    val number: Int
)

data class Asteroid(
    val id: String,
    val name: String,
    @SerializedName("nasa_jpl_url")
    val nasaJplUrl: String,
    @SerializedName("is_potentially_hazardous_asteroid")
    val isPotentiallyHazardous: Boolean,
    @SerializedName("estimated_diameter")
    val estimatedDiameter: EstimatedDiameter,
    @SerializedName("close_approach_data")
    val closeApproachData: List<CloseApproachData>
)

data class EstimatedDiameter(
    val kilometers: DiameterRange
)

data class DiameterRange(
    @SerializedName("estimated_diameter_min")
    val minKilometers: Double,
    @SerializedName("estimated_diameter_max")
    val maxKilometers: Double
)

data class CloseApproachData(
    @SerializedName("close_approach_date")
    val closeApproachDate: String,
    @SerializedName("orbiting_body")
    val orbitingBody: String,
    @SerializedName("miss_distance")
    val missDistance: MissDistance
)

data class MissDistance(
    val kilometers: String
)

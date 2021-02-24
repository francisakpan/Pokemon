package com.francis.mapapplication.model

/**
 *@param id partner's id
 * @param latitude location latitude
 * @param longitude location longitude
 * Partner's model class
 */
data class Partner(
    var id: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
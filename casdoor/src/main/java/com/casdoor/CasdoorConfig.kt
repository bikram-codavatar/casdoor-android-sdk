package com.casdoor

/**
 *
 *
 * Casdoor Config
 */
data class CasdoorConfig(
    val clientID: String,
    val organizationName: String,
    val redirectUri: String,
    var endpoint: String,
    val appName: String,
)

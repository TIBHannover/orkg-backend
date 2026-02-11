package org.orkg.testing.drivers.registration.keycloak

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.testing.dsl.require
import org.orkg.world.Environment
import tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpResponse.BodyHandlers
import java.util.Base64
import org.keycloak.admin.client.Keycloak as KeycloakAPIClient

/** Helper class to provide configuration information. */
class Keycloak private constructor(
    realm: String,
    serverName: String,
    scheme: String = "http",
    private val userClientId: String,
    private val userClientSecret: String,
    adminUsername: String,
    adminPassword: String,
    adminRealm: String,
    adminClientId: String,
) {
    val adminClient: KeycloakAPIClient by lazy {
        KeycloakAPIClient.getInstance(baseURL, adminRealm, adminUsername, adminPassword, adminClientId)
    }

    val baseURL: String = "$scheme://$serverName"

    val realmURL: String = "$baseURL/realms/$realm"

    // Cannot be refreshed, but that should not be a problem
    val realmInfo: RealmInformation by lazy { fetchInfo(realmURL) }

    // Cannot be refreshed, but that should not be a problem
    val oidc: OpenIDConnetConfiguration by lazy { fetchInfo("$realmURL/.well-known/openid-configuration") }

    fun login(username: String, password: String): TokenResponse {
        val credentials = Base64.getEncoder().encodeToString("$userClientId:$userClientSecret".toByteArray())
        val formData: BodyPublisher = mapOf(
            "username" to username,
            "password" to password,
            "grant_type" to "password",
        )
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { encoded -> HttpRequest.BodyPublishers.ofString(encoded) }
        val endpoint = oidc.tokenEndpoint
        val mapper = jacksonObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        val client: HttpClient = HttpClient.newBuilder().followRedirects(NORMAL).build()
        val request = HttpRequest.newBuilder()
            .POST(formData)
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", "Basic $credentials")
            .build()
        val response = client.send(request, BodyHandlers.ofString())
        return mapper.readValue(response.body(), TokenResponse::class.java)
    }

    companion object {
        fun from(environment: Environment): Keycloak {
            val hostname = environment.require("KEYCLOAK_HOST")
            val port = environment.require("KEYCLOAK_TCP_8080")
            val realm = environment.require("ORKG_KEYCLOAK_REALM")
            val userClientId = environment.require("ORKG_KEYCLOAK_USER_CLIENT_ID")
            val userClientSecret = environment.require("ORKG_KEYCLOAK_USER_CLIENT_SECRET")
            val adminUsername = environment.require("ORKG_KEYCLOAK_ADMIN_USERNAME")
            val adminPassword = environment.require("ORKG_KEYCLOAK_ADMIN_PASSWORD")
            val adminRealm = environment.require("ORKG_KEYCLOAK_ADMIN_REALM")
            val adminClientId = environment.require("ORKG_KEYCLOAK_ADMIN_CLIENT_ID")
            return Keycloak(
                realm = realm,
                serverName = "$hostname:$port",
                scheme = "http",
                userClientId = userClientId,
                userClientSecret = userClientSecret,
                adminUsername = adminUsername,
                adminPassword = adminPassword,
                adminRealm = adminRealm,
                adminClientId = adminClientId,
            )
        }
    }

    private inline fun <reified T> fetchInfo(url: String): T {
        val mapper = jacksonObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        val client: HttpClient = HttpClient.newBuilder().followRedirects(NORMAL).build()
        val request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build()
        val response = client.send(request, BodyHandlers.ofString())
        return mapper.readValue(response.body(), T::class.java)
    }

    data class RealmInformation(
        @get:JsonProperty("realm") val realm: String,
        @get:JsonProperty("public_key") val publicKey: String,
        @get:JsonProperty("token-service") val tokenService: String,
        @get:JsonProperty("account-service") val accountService: String,
    )

    data class OpenIDConnetConfiguration(
        @get:JsonProperty("issuer") val issuer: String,
        @get:JsonProperty("token_endpoint") val tokenEndpoint: String,
    )

    data class TokenResponse(
        @get:JsonProperty("access_token") val accessToken: String,
        @get:JsonProperty("refresh_token") val refreshToken: String,
        @get:JsonProperty("token_type") val tokenType: String,
        @get:JsonProperty("scope") val scope: String,
        @get:JsonProperty("session_state") val sessionState: String,
        @get:JsonProperty("not-before-policy") val notBeforePolicy: UInt,
    )
}

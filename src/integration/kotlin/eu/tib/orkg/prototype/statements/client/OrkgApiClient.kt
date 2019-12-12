package eu.tib.orkg.prototype.statements.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.net.URI
import java.util.Base64

class OrkgApiClient(private val port: Int = 80) {

    @Value("orkg.client.oauth2.client-id")
    private var clientId = "orkg-client"

    @Value("orkg.client.oauth2.client-secret")
    private var clientSecret = "secret"

    fun getAccessToken(username: String, password: String): String? {
        // TODO: externalize settings
        val uri = URI("http", null, "localhost", port, "/oauth/token", null, null)
        val url = uri.toString()

        val params = getRequestParameters(username, password)
        val headers = getRequestHeaders()
        val request = HttpEntity(params, headers)

        val restTemplate = RestTemplate().apply {
            messageConverters.add(StringHttpMessageConverter())
        }
        return restTemplate.postForObject<TokenResponse>(url, request).accessToken
    }

    private fun getRequestHeaders() =
        HttpHeaders().apply {
            add("Authorization", "Basic ${encodeCredentials()}")
            contentType = APPLICATION_FORM_URLENCODED
        }

    private fun getRequestParameters(
        username: String,
        password: String
    ): MultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("client_id", clientId)
            add("username", username)
            add("password", password)
        }

    /**
     * Helper function to encode client credentials in Base64.
     */
    private fun encodeCredentials() = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

    /**
     * Helper class to provide access to the elements in the OAuth2 token response.
     */
    internal data class TokenResponse(
        @JsonProperty("access_token")
        val accessToken: String?,
        @JsonProperty("token_type")
        val tokenType: String?,
        @JsonProperty("expires_in")
        val expiresIn: String?,
        @JsonProperty("scope")
        val scope: String?
    )
}

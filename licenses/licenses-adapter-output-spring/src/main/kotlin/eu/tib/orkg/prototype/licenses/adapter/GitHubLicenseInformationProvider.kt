package eu.tib.orkg.prototype.licenses.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.licenses.domain.LicenseInformation
import eu.tib.orkg.prototype.licenses.domain.LicenseInformationProvider
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.regex.Pattern
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

private val REPO_PATH_PATTERN = Pattern.compile("""/([a-zA-Z0-9-_.]+)/([a-zA-Z0-9-_.]+)/?""")
private const val API_URL = "https://api.github.com"

@Component
class GitHubLicenseInformationProvider(
    val objectMapper: ObjectMapper,
    val httpClient: HttpClient
) : LicenseInformationProvider {
    override val id: String = "github"
    override val description: String = "Determines licenses of GitHub projects"

    override fun canProcess(uri: URI): Boolean =
        uri.host == "github.com" && REPO_PATH_PATTERN.matcher(uri.path).matches()

    override fun determineLicense(from: URI): LicenseInformation? {
        val path = from.path.dropLastWhile { it == '/' }
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$API_URL/repos$path/license"))
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                return null
            }
            val spdx = objectMapper.readTree(response.body())
                .path("license")
                .path("spdx_id")
                .asText(null)
            return spdx?.let { LicenseInformation(id, it) }
        } catch (e: IOException) {
            return null
        }
    }
}

@Configuration
class GitHubLicenseInformationConfiguration {
    @Bean
    fun httpClient(): HttpClient = HttpClient.newHttpClient()
}

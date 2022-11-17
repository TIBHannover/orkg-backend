package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DoiService(
    private val literalService: LiteralUseCases
) {

    fun registerDoi(doiData: String, credentials: String, url: String): Optional<String> {
        val httpConnection = prepareHttpCall(url, credentials)
        return doiRegisterRequest(doiData, httpConnection)
    }

    private fun prepareHttpCall(url: String, credentials: String): HttpURLConnection {
        val dataciteUrl = URL(url)
        val con = dataciteUrl.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/vnd.api+json; utf-8")
        con.setRequestProperty("Authorization", "Basic $credentials")
        con.setRequestProperty("Accept", "application/json")
        con.doOutput = true
        return con
    }

    private fun doiRegisterRequest(doiData: String, httpConnection: HttpURLConnection): Optional<String> {
        httpConnection.outputStream.write(doiData.toByteArray(charset("utf-8")))
        val responseBody = try {
            BufferedReader(InputStreamReader(httpConnection.inputStream, "utf-8"))
                .readLines()
                .joinToString("\n", transform = String::trim)
        } catch (e: Exception) {
            throw RuntimeException("Error reading DOI response from ${httpConnection.url}.", e)
        }
        if (httpConnection.responseCode == HttpURLConnection.HTTP_CREATED) {
            return Optional.of(responseBody)
        } else {
            throw RuntimeException("Error establishing connection to ${httpConnection.url}. Response: $responseBody")
        }
    }

    fun getRelatedPapers(relatedResources: Set<ResourceId>): String {
        val doiList: MutableSet<String> = mutableSetOf()
        relatedResources.map { resourceId ->
            val doi = literalService.findDOIByContributionId(resourceId)
            if (doi.isPresent && !doiList.contains(doi.get().label)) {
                doiList.add(doi.get().label)
            }
        }

        return doiList.joinToString("\n", transform = { """<relatedIdentifier relationType="References" relatedIdentifierType="DOI">$it</relatedIdentifier>""" })
    }
}

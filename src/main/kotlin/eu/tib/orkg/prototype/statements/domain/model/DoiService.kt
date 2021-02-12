package eu.tib.orkg.prototype.statements.domain.model

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Optional
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class DoiService(
    private val literalService: LiteralService
) {

    fun registerDoi(doiData: String, credentials: String, url: String): Optional<String> {
        return try {
            val httpConnection = prepareHttpCall(url, credentials)
            try {
                doiRegisterRequest(doiData, httpConnection)
            } catch (e: Exception) {
                Optional.of(ResponseStatusException(NOT_FOUND, "Error creating DOI").toString())
            }
        } catch (e: Exception) {
            Optional.of(ResponseStatusException(NOT_FOUND, "Error establishing connection").toString())
        }
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
        val responseBody = BufferedReader(InputStreamReader(httpConnection.inputStream, "utf-8"))
            .readLines()
            .joinToString("\n", transform = String::trim)
        return Optional.of(responseBody)
    }

    fun getRelatedPapers(relatedResources: Set<ResourceId>): String {
        var doiList: MutableSet<String> = mutableSetOf()
        relatedResources.map { resourceId ->
            val doi = literalService.findDOIByContributionId(resourceId)
            if (doi.isPresent && !doiList.contains(doi.get().label)) {
                doiList.add(doi.get().label)
                }
        }

        return doiList.joinToString("\n", transform = { """<relatedIdentifier relationType="References" relatedIdentifierType="DOI">$it</relatedIdentifier>""" })
    }
}

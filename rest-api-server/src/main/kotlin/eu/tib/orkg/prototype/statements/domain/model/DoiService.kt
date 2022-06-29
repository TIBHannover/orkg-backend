package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import eu.tib.orkg.prototype.ResearchField
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
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
    private val literalService: LiteralUseCases
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
        val doiList: MutableSet<String> = mutableSetOf()
        relatedResources.map { resourceId ->
            val doi = literalService.findDOIByContributionId(resourceId)
            if (doi.isPresent && !doiList.contains(doi.get().label)) {
                doiList.add(doi.get().label)
                }
        }

        return doiList.joinToString("\n", transform = { """<relatedIdentifier relationType="References" relatedIdentifierType="DOI">$it</relatedIdentifier>""" })
    }

    fun prepareGetCall(doi: String): String {
        val url = URL("https://api.datacite.org/dois/$doi")

        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        val responseBody = BufferedReader(InputStreamReader(conn.inputStream, "utf-8"))
            .readLines()
            .joinToString("\n", transform = String::trim)

        return responseBody
        //val JSON = jacksonObjectMapper()
        //print(JSON.createParser(responseBody))
        //val test = JSON.readTree(responseBody)
        //var temp = test.get("data")
        //temp = temp.get("attributes").get("titles") //.findValue("relatedIdentifier")
        //println(temp[0].get("title"))
        //return ""
    }
}

package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.ID_DOI_PREDICATE
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Optional
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class DoiService(
    private val statementService: StatementService
) {

    fun registerDoi(doiData: String, credentials: String, url: String): Optional<String> {
        return try {
            var httpConnection = prepareHttpCall(url, credentials)
            try {
                doiRegisterRequest(doiData, httpConnection)
            } catch (e: Exception) {
                Optional.of(ResponseStatusException(NOT_FOUND, "Error creating DOI").toString())
            }
        } catch (e: Exception) {
            Optional.of(ResponseStatusException(NOT_FOUND, "Error establishing connection").toString())
        }
    }

    fun prepareHttpCall(url: String, credentials: String): HttpURLConnection {
        val dataciteUrl = URL(url)
        val con = dataciteUrl.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/vnd.api+json; utf-8")
        con.setRequestProperty("Authorization", "Basic $credentials")
        con.setRequestProperty("Accept", "application/json")
        con.doOutput = true
        return con
    }

    fun doiRegisterRequest(doiData: String, httpConnection: HttpURLConnection): Optional<String> {
        httpConnection.outputStream.write(doiData.toByteArray(charset("utf-8")))
        return Optional.of(BufferedReader(
                InputStreamReader(httpConnection.inputStream, "utf-8"))
                .readLines().map(String::trim).joinToString("\n"))
    }

    fun getRelatedPapers(relatedResources: Set<ResourceId>): String {
        val pagination = PageRequest.of(0, 1)
        var relatedIdentifiers = ""
        relatedResources.map { resourceId ->
            var statements = statementService.findAllByObject(resourceId.value, pagination)
            statements.map { statement ->
                var paper = refreshSubject(statement.subject)
                var result = statementService.findAllBySubjectAndPredicate(paper.id.toString(), PredicateId(
                    ID_DOI_PREDICATE
                ), pagination)
                result.forEach {
                    relatedIdentifiers += """<relatedIdentifier relationType="IsDerivedFrom" relatedIdentifierType="DOI">${refreshObject(it.`object`).label}</relatedIdentifier>"""
                }
            }
        }

        return relatedIdentifiers
    }

    private fun refreshSubject(thing: Thing): Resource {
        return when (thing) {
            is Resource -> thing
            else -> error("")
        }
    }

    private fun refreshObject(thing: Thing): Literal {
        return when (thing) {
            is Literal -> thing
            else -> error("")
        }
    }
}

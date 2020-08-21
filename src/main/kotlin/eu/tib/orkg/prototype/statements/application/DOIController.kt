package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.configuration.DataCiteConfiguration
import eu.tib.orkg.prototype.statements.domain.model.DoiService
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.domain.model.Thing
import java.time.LocalDate
import java.util.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dois/")
@CrossOrigin(origins = ["*"])
class DOIController(
    private val statementService: StatementService,
    private val doiService: DoiService
) {

    @Autowired
    private lateinit var dataciteConfiguration: DataCiteConfiguration

    @PostMapping("/")
    fun addDOI(@RequestBody doiData: CreateDOIRequest): String {
        var doiPrefix = dataciteConfiguration.doiPrefix!!
        var xmlMetadata = doiData.toXML(doiPrefix, getRelatedPapers(doiData.related_resources))
        var doiMetaData = """{
                "data": {
                "id": "$doiPrefix/${doiData.comparison_id}",
                "type": "dois",
                "attributes": {
                "event": "draft",
                "doi": "$doiPrefix/${doiData.comparison_id}",
                "url": "${doiData.url}",
                "xml": "${Base64.getEncoder().encodeToString((xmlMetadata).toByteArray())}"
            }
        }
    }"""

        return doiService.registerDoi(doiMetaData, dataciteConfiguration.encodeCredentials()!!, dataciteConfiguration.url!!)
    }

    fun getRelatedPapers(relatedResources: Set<ResourceId>): String {
        val pagination = PageRequest.of(0, 1)
        var relatedIdentifiers = ""
        relatedResources.map { resourceId ->
            var statements = statementService.findAllByObject(resourceId.value, pagination)
            statements.map { statement ->
                var paper = refreshSubject(statement.subject)
                var result = statementService.findAllBySubjectAndPredicate(paper.id.toString(), PredicateId(ID_DOI_PREDICATE), pagination)
                result.map {
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

//    fun registerDOI(DOIData: String): String {
//        val url = URL("https://api.test.datacite.org/dois")
//        try {
//            val con = url.openConnection() as HttpURLConnection
//            con.requestMethod = "POST"
//            con.setRequestProperty("Content-Type", "application/vnd.api+json; utf-8")
//            val credentials = dataciteConfiguration.encodeCredentials()
//            con.setRequestProperty("Authorization", "Basic $credentials")
//            con.setRequestProperty("Accept", "application/json")
//            con.doOutput = true
//            con.outputStream.use { os ->
//                val input = DOIData.toByteArray(charset("utf-8"))
//                os.write(input, 0, input.size)
//            }
//
//            BufferedReader(
//                InputStreamReader(con.inputStream, "utf-8")
//            ).use { br ->
//                val response = StringBuilder()
//                var responseLine: String? = null
//                while (br.readLine().also { responseLine = it } != null) {
//                    response.append(responseLine!!.trim { it <= ' ' })
//                }
//                return response.toString()
//            }
//        } catch (e: Exception) {
//                throw IOException("Error Creating DOI")
//        }
//    }

    data class CreateDOIRequest(
        val comparison_id: String,
        val title: String,
        val subject: String,
        val related_resources: Set<ResourceId>,
        val description: String,
        val authors: List<Creator>,
        val url: String
    ) {
        fun toXML(doiPrefix: String, relatedPapers: String): String {
            return """<?xml version="1.0" encoding="UTF-8"?>
        <resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-4" xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.3/metadata.xsd">
        <identifier identifierType="DOI">$doiPrefix/$comparison_id</identifier>
        <creators>
            ${authors.map(Creator::toXML).joinToString("\n")}
        </creators>
        <titles>
            <title xml:lang="en">$title</title>
        </titles>
        <publisher xml:lang="en">Open Research Knowledge Graph</publisher>
        <publicationYear>${LocalDate.now().year}</publicationYear>
        <subjects>
            <subject xml:lang="en">$subject</subject>
        </subjects>
        <language>en</language>
        <resourceType resourceTypeGeneral="Dataset">Comparison</resourceType>
        <relatedIdentifiers>
            $relatedPapers
        </relatedIdentifiers>
        <rightsList>
        <rights rightsURI="https://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License.</rights>
        </rightsList>
        <descriptions>
        <description descriptionType="Abstract">$description</description>
        </descriptions>
        </resource>"""
        }
    }

    data class Creator(val creator: String, val orcid: String) {
        fun toXML(): String {
            return """<creator>
                        <creatorName nameType="Personal">$creator</creatorName>
                        <nameIdentifier schemeURI="http://orcid.org/" nameIdentifierScheme="ORCID">$orcid</nameIdentifier>
                      </creator>"""
        }
    }
}

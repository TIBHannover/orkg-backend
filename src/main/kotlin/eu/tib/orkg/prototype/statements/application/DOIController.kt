package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.configuration.DataCiteConfiguration
import eu.tib.orkg.prototype.statements.domain.model.DoiService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import java.time.LocalDate
import java.util.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dois/")
class DOIController(
    private val statementService: StatementService,
    private val doiService: DoiService
) {

    @Autowired
    private lateinit var dataciteConfiguration: DataCiteConfiguration

    @PostMapping("/")
    fun addDOI(@RequestBody doiData: CreateDOIRequest): String {
        val doiPrefix = dataciteConfiguration.doiPrefix!!
        val xmlMetadata = doiData.toXML(doiPrefix, doiService.getRelatedPapers(doiData.relatedResources))
        // This structure is required by the DataCite API
        val doiMetaData = """{
                "data": {
                "id": "$doiPrefix/${doiData.comparisonId}",
                "type": "dois",
                "attributes": {
                "event": "${dataciteConfiguration.publish}",
                "doi": "$doiPrefix/${doiData.comparisonId}",
                "url": "${doiData.url}",
                "xml": "${Base64.getEncoder().encodeToString((xmlMetadata).toByteArray())}"
            }
        }
    }"""

        return doiService
            .registerDoi(doiMetaData, dataciteConfiguration.encodeCredentials()!!, dataciteConfiguration.url!!)
            .orElseThrow()
    }

    data class CreateDOIRequest(
        @JsonProperty("comparison_id")
        val comparisonId: String,
        val title: String,
        val subject: String,
        @JsonProperty("related_resources")
        val relatedResources: Set<ResourceId>,
        val description: String,
        val authors: List<Creator>,
        val url: String
    ) {
        fun toXML(doiPrefix: String, relatedPapers: String): String {
            return """<?xml version="1.0" encoding="UTF-8"?>
                        <resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-4" xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.3/metadata.xsd">
                            <identifier identifierType="DOI">$doiPrefix/$comparisonId</identifier>
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

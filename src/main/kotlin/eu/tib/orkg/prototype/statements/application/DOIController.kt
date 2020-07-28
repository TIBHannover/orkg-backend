package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import eu.tib.orkg.prototype.statements.domain.model.Thing
import java.time.LocalDate
import java.util.Base64
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
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
    private val service: OrganizationService,
    private val userService: UserService,
    private val resourceService: ResourceService,
    private val statementService: StatementService,
    private val literalService: LiteralService,
    private val observatoryService: ObservatoryService
) {
    @Value("\${datacite.test.username}")
    var dataciteTestUsername: String? = null

    @Value("\${datacite.test.password}")
    var dataciteTestPassword: String? = null

    @Value("\${datacite.test.DOIPrefix}")
    var dataciteTestDOIPrefix: String? = null

    @PostMapping("/")
    fun addDOI(@RequestBody doiData: CreateDOIRequest): String? {
        val base64 = Base64.getEncoder().encodeToString(("$dataciteTestUsername:$dataciteTestPassword").toByteArray())
        getCreatorsXml(doiData.authors)
        getRelatedPapers(doiData.relatedResources)
        return createXmlMetadata(doiData.comparisonId, doiData.description, doiData.title, getCreatorsXml(doiData.authors), getRelatedPapers(doiData.relatedResources), doiData.subject)
    }

    fun createXmlMetadata(comparisonId: String, description: String, title: String, creators: String, relatedIdentifiers: String, subject: String): String {
        var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<resource xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://datacite.org/schema/kernel-4\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.3/metadata.xsd\">\n" +
        "<identifier identifierType=\"DOI\">$dataciteTestDOIPrefix/$comparisonId</identifier>\n" +
        "<creators>\n" +
            "${creators}\n" +
        "</creators>\n" +
        "<titles>\n" +
            "<title xml:lang=\"en\">$title</title>\n" +
        "</titles>\n" +
        "<publisher xml:lang=\"en\">Open Research Knowledge Graph</publisher>\n" +
        "<publicationYear>${LocalDate.now().year}</publicationYear>\n" +
        "<subjects>\n" +
            "<subject xml:lang=\"en\">$subject</subject>\n" +
        "</subjects>\n" +
        "<language>en</language>\n" +
        "<resourceType resourceTypeGeneral=\"Dataset\">Comparison</resourceType>\n" +
        "<relatedIdentifiers>\n" +
            "${relatedIdentifiers}\n" +
        "</relatedIdentifiers>\n" +
        "<rightsList>\n" +
        "<rights rightsURI=\"https://creativecommons.org/licenses/by-sa/4.0/\">Creative Commons Attribution-ShareAlike 4.0 International License.</rights>\n" +
        "</rightsList>\n" +
        "<descriptions>\n" +
        "<description descriptionType=\"Abstract\">$description</description>\n" +
        "</descriptions>\n" +
        "</resource>\n"
        return xml
    }

    fun getCreatorsXml(authors: Set<Creator>): String {
        var authorsList = ""
        authors.forEach {
            authorsList += "<creator>\n" +
            "<creatorName nameType=\"Personal\">${it.creator}</creatorName>\n" +
            "<nameIdentifier schemeURI=\"http://orcid.org/\" nameIdentifierScheme=\"ORCID\">${it.ORCID}</nameIdentifier>\n" +
            "</creator\n"
        }
        return authorsList
    }

    fun getRelatedPapers(relatedResources: Set<ResourceId>): String {
        val pagination = PageRequest.of(0, 1)
        var relatedIdentifiers = ""
        relatedResources.forEach {
            var statementResult = statementService.findAllByObject(it.value, pagination)
            for (statement in statementResult) {
                var paper = refreshSubject(statement.subject)
                var result = statementService.findAllBySubjectAndPredicate(paper.id.toString(), PredicateId(ID_DOI_PREDICATE), pagination)
                for (res in result) {
                    relatedIdentifiers += "<relatedIdentifier relationType=\"IsDerivedFrom\" relatedIdentifierType=\"DOI\">${refreshObject(res.`object`).label}</relatedIdentifier>\n"
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

    data class CreateDOIRequest(
        val comparisonId: String,
        val title: String,
        val subject: String,
        val relatedResources: Set<ResourceId>,
        val description: String,
        val authors: Set<Creator>,
        val url: String
    )

    data class Creator(val creator: String, val ORCID: String)

    data class CreateOrganizationRequest(
        val organizationName: String,
        var organizationLogo: String,
        val createdBy: UUID,
        val url: String
    )
}

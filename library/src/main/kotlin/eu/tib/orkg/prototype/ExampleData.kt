package eu.tib.orkg.prototype

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.IndexService
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.io.InputStream
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("development", "docker")
class ExampleData(
    private val resourceService: ResourceUseCases,
    private val predicateService: CreatePredicateUseCase,
    private val statementService: StatementUseCases,
    private val classService: ClassUseCases,
    private val indexService: IndexService,
    private val flags: FeatureFlagService,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        // Create required constraints and indices
        if (flags.isNeo4jVersion3Enabled()) indexService.verifyIndices()

        if (statementsPresent())
            return

        //
        // Resources
        //
        val grubersDesign =
            resourceService.create("Gruber's design of ontologies").id
        val wilesProof =
            resourceService.create("Wiles's proof of Fermat's last theorem").id
        // val mathProof =
        resourceService.create("Mathematical proof").id
        val modularityTheorem =
            resourceService.create("Modularity theorem").id
        val fermatsLastTheorem =
            resourceService.create("Fermat's last theorem (conjecture)").id
        val tanimaConj =
            resourceService.create("Taniyama-Shimura-Weil conjecture").id
        val ontoDesignCriteria =
            resourceService.create("Design criteria for ontologies").id
        val knowledgeEngineering =
            resourceService.create("Knowledge Engineering").id
        val designOfOntologies =
            resourceService.create("Design of ontologies").id
        val caseStudies =
            resourceService.create("Case studies").id

        //
        // Predicates
        //
        val addresses = predicateService.create("addresses").id
        val yields = predicateService.create("yields").id
        val employs = predicateService.create("employs").id

        //
        // Statements
        //
        statementService.create(wilesProof, addresses, tanimaConj)
        statementService.create(wilesProof, addresses, fermatsLastTheorem)
        statementService.create(wilesProof, yields, modularityTheorem)

        statementService.create(grubersDesign, employs, caseStudies)
        statementService.create(grubersDesign, addresses, designOfOntologies)
        statementService.create(grubersDesign, addresses, knowledgeEngineering)
        statementService.create(grubersDesign, yields, ontoDesignCriteria)

        // Predicates (for DILS)
        predicateService.create("is a")
        predicateService.create("refers to")
        predicateService.create("uses")
        predicateService.create("author")
        predicateService.create("affiliation")
        predicateService.create("email")
        predicateService.create("ORCID")
        predicateService.create("DOI")
        predicateService.create("problem")
        predicateService.create("solution")
        predicateService.create("use case")
        predicateService.create("description")
        predicateService.create("implementation")
        predicateService.create("has")
        predicateService.create("has part")
        predicateService.create("part of")
        predicateService.create("input")
        predicateService.create("output")
        predicateService.create("programming language")
        predicateService.create("environment")
        predicateService.create("defines")
        predicateService.create("field")
        predicateService.create("web site")

        //
        // UI startup script (Allard's)
        //

        //
        // Predicates
        //
        predicateService.create("has doi")
        predicateService.create("has author")
        predicateService.create("has publication month")
        predicateService.create("has publication year")
        predicateService.create("has research field")
        predicateService.create("has contribution")
        predicateService.create("has research problem")
        // Demo Predicate Data
        predicateService.create("approach")
        predicateService.create("evaluation")
        predicateService.create("implementation")
        val subfieldPredicate = predicateService.create("has subfield").id

        //
        // Class
        //
        classService.create(CreateClassRequest(ThingId("Paper"), "Paper", null))

        //
        // Resource
        //
        resourceService.create("paper")
        val researchField = resourceService.create("Research field").id

        // Adding resources from the json file
        val mapper = jacksonObjectMapper()
        val inStream: InputStream? = javaClass.classLoader.getResourceAsStream("data/ResearchFields.json")
        val fields = mapper.readValue<List<ResearchField>>(inStream!!)
        for (field in fields) {
            val newField = resourceService.create(CreateResourceRequest(null, field.name,
                setOf(ThingId("ResearchField")))).id
            statementService.create(researchField, subfieldPredicate, newField)
            for (subfield in field.subfields) {
                val newSubfield = resourceService.create(CreateResourceRequest(null, subfield.name,
                    setOf(ThingId("ResearchField")))).id
                statementService.create(newField, subfieldPredicate, newSubfield)
                for (subSubfield in subfield.subfields) {
                    val newSubSubfield = resourceService.create(CreateResourceRequest(null, subSubfield.name,
                        setOf(ThingId("ResearchField")))).id
                    statementService.create(newSubfield, subfieldPredicate, newSubSubfield)
                }
            }
        }
    }

    private fun statementsPresent() =
        statementService.totalNumberOfStatements() > 0
}

data class ResearchField(val name: String, val subfields: List<ResearchField> = listOf())

fun escapeLiterals(literal: String): String {
    return literal
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("(\\r|\\n|\\r\\n)+".toRegex(), "\\\\n")
}

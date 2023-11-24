package org.orkg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
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
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
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
        predicateService.create("subClassOf")
        // Demo Predicate Data
        predicateService.create("approach")
        predicateService.create("evaluation")
        predicateService.create("implementation")
        val subfieldPredicate = predicateService.create("has subfield").id

        //
        // Class
        //
        classService.create(CreateClassUseCase.CreateCommand(id = "Paper", label = "Paper", uri = null))

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
            val newFieldCommand = CreateResourceUseCase.CreateCommand(
                label = field.name,
                classes = setOf(ThingId("ResearchField")),
            )
            val newField = resourceService.create(newFieldCommand)
            statementService.create(researchField, subfieldPredicate, newField)
            for (subfield in field.subfields) {
                val newSubFieldCommand = CreateResourceUseCase.CreateCommand(
                    label = subfield.name,
                    classes = setOf(ThingId("ResearchField")),
                )
                val newSubfield = resourceService.create(newSubFieldCommand)
                statementService.create(newField, subfieldPredicate, newSubfield)
                for (subSubfield in subfield.subfields) {
                    val newSubSubFieldCommand = CreateResourceUseCase.CreateCommand(
                        label = subSubfield.name,
                        classes = setOf(ThingId("ResearchField")),
                    )
                    val newSubSubfield = resourceService.create(newSubSubFieldCommand)
                    statementService.create(newSubfield, subfieldPredicate, newSubSubfield)
                }
            }
        }
    }

    private fun statementsPresent() =
        statementService.totalNumberOfStatements() > 0

    data class ResearchField(val name: String, val subfields: List<ResearchField> = listOf())
}

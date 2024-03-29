package org.orkg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
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
            resourceService.create("Gruber's design of ontologies")
        val wilesProof =
            resourceService.create("Wiles's proof of Fermat's last theorem")
        // val mathProof =
        resourceService.create("Mathematical proof")
        val modularityTheorem =
            resourceService.create("Modularity theorem")
        val fermatsLastTheorem =
            resourceService.create("Fermat's last theorem (conjecture)")
        val tanimaConj =
            resourceService.create("Taniyama-Shimura-Weil conjecture")
        val ontoDesignCriteria =
            resourceService.create("Design criteria for ontologies")
        val knowledgeEngineering =
            resourceService.create("Knowledge Engineering")
        val designOfOntologies =
            resourceService.create("Design of ontologies")
        val caseStudies =
            resourceService.create("Case studies")

        //
        // Predicates
        //
        val addresses = predicateService.create("addresses")
        val yields = predicateService.create("yields")
        val employs = predicateService.create("employs")

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
        val subfieldPredicate = predicateService.create("has subfield")

        //
        // Class
        //
        classService.create(CreateClassUseCase.CreateCommand(id = Classes.paper, label = "Paper", uri = null))

        //
        // Resource
        //
        resourceService.create("paper")
        val researchField = resourceService.create("Research field")

        // Adding resources from the json file
        val mapper = jacksonObjectMapper()
        val inStream: InputStream? = javaClass.classLoader.getResourceAsStream("data/ResearchFields.json")
        val fields = mapper.readValue<List<ResearchField>>(inStream!!)
        for (field in fields) {
            val newFieldCommand = CreateResourceUseCase.CreateCommand(
                label = field.name,
                classes = setOf(Classes.researchField),
            )
            val newField = resourceService.create(newFieldCommand)
            statementService.create(researchField, subfieldPredicate, newField)
            for (subfield in field.subfields) {
                val newSubFieldCommand = CreateResourceUseCase.CreateCommand(
                    label = subfield.name,
                    classes = setOf(Classes.researchField),
                )
                val newSubfield = resourceService.create(newSubFieldCommand)
                statementService.create(newField, subfieldPredicate, newSubfield)
                for (subSubfield in subfield.subfields) {
                    val newSubSubFieldCommand = CreateResourceUseCase.CreateCommand(
                        label = subSubfield.name,
                        classes = setOf(Classes.researchField),
                    )
                    val newSubSubfield = resourceService.create(newSubSubFieldCommand)
                    statementService.create(newSubfield, subfieldPredicate, newSubSubfield)
                }
            }
        }
    }

    private fun CreatePredicateUseCase.create(label: String): ThingId =
        create(CreatePredicateUseCase.CreateCommand(label = label))

    private fun CreateResourceUseCase.create(label: String): ThingId =
        create(CreateResourceUseCase.CreateCommand(label = label))

    private fun statementsPresent() =
        statementService.totalNumberOfStatements() > 0

    data class ResearchField(val name: String, val subfields: List<ResearchField> = listOf())
}

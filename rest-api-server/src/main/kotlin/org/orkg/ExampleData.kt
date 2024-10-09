package org.orkg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Profile("development", "docker")
@Order(2)
class ExampleData(
    private val resourceService: ResourceUseCases,
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
        // Statements
        //
        statementService.create(wilesProof, Predicates.addresses, tanimaConj)
        statementService.create(wilesProof, Predicates.addresses, fermatsLastTheorem)
        statementService.create(wilesProof, Predicates.yields, modularityTheorem)

        statementService.create(grubersDesign, Predicates.employs, caseStudies)
        statementService.create(grubersDesign, Predicates.addresses, designOfOntologies)
        statementService.create(grubersDesign, Predicates.addresses, knowledgeEngineering)
        statementService.create(grubersDesign, Predicates.yields, ontoDesignCriteria)

        //
        // Resource
        //
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
            statementService.create(researchField, Predicates.hasSubfield, newField)
            for (subfield in field.subfields) {
                val newSubFieldCommand = CreateResourceUseCase.CreateCommand(
                    label = subfield.name,
                    classes = setOf(Classes.researchField),
                )
                val newSubfield = resourceService.create(newSubFieldCommand)
                statementService.create(newField, Predicates.hasSubfield, newSubfield)
                for (subSubfield in subfield.subfields) {
                    val newSubSubFieldCommand = CreateResourceUseCase.CreateCommand(
                        label = subSubfield.name,
                        classes = setOf(Classes.researchField),
                    )
                    val newSubSubfield = resourceService.create(newSubSubFieldCommand)
                    statementService.create(newSubfield, Predicates.hasSubfield, newSubSubfield)
                }
            }
        }
    }

    private fun CreateResourceUseCase.create(label: String): ThingId =
        create(CreateResourceUseCase.CreateCommand(label = label))

    private fun statementsPresent() =
        statementService.totalNumberOfStatements() > 0

    data class ResearchField(val name: String, val subfields: List<ResearchField> = listOf())
}

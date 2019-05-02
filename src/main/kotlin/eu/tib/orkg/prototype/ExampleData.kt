package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.boot.*
import org.springframework.context.annotation.*
import org.springframework.stereotype.*

@Component
@Profile("development", "docker")
class ExampleData(
    private val resourceService: ResourceService,
    private val predicateService: PredicateService,
    private val statementService: StatementWithResourceService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        if (statementService.findAll().count() > 0)
            return

        //
        // Resources
        //
        val grubersDesign =
            resourceService.create("Gruber's design of ontologies").id!!
        val wilesProof =
            resourceService.create("Wiles's proof of Fermat's last theorem").id!!
        val mathProof =
            resourceService.create("Mathematical proof").id!!
        val modularityTheorem =
            resourceService.create("Modularity theorem").id!!
        val fermatsLastTheorem =
            resourceService.create("Fermat's last theorem (conjecture)").id!!
        val tanimaConj =
            resourceService.create("Taniyama-Shimura-Weil conjecture").id!!
        val ontoDesignCriteria =
            resourceService.create("Design criteria for ontologies").id!!
        val knowledgeEngineering =
            resourceService.create("Knowledge Engineering").id!!
        val designOfOntologies =
            resourceService.create("Design of ontologies").id!!
        val caseStudies =
            resourceService.create("Case studies").id!!

        //
        // Predicates
        //
        val addresses = predicateService.create("addresses").id!!
        val yields = predicateService.create("yields").id!!
        val employs = predicateService.create("employs").id!!

        //
        // Statements
        //
        statementService.create(wilesProof, employs, mathProof)
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

        // TODO: Rewrite script in Kotlin for start-up
        //Runtime.getRuntime().exec("python /app/startup.py");
    }
}

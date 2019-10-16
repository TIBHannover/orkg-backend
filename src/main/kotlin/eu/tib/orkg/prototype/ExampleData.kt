package eu.tib.orkg.prototype

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import eu.tib.orkg.prototype.statements.application.CreateClassRequest
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithLiteralService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
@Profile("development", "docker")
class ExampleData(
    private val resourceService: ResourceService,
    private val predicateService: PredicateService,
    private val statementWithResourceService: StatementWithResourceService,
    private val statementWithLiteralService: StatementWithLiteralService,
    private val classService: ClassService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        if (statementsPresent())
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
        statementWithResourceService.create(wilesProof, employs, mathProof)
        statementWithResourceService.create(wilesProof, addresses, tanimaConj)
        statementWithResourceService.create(wilesProof, addresses, fermatsLastTheorem)
        statementWithResourceService.create(wilesProof, yields, modularityTheorem)

        statementWithResourceService.create(grubersDesign, employs, caseStudies)
        statementWithResourceService.create(grubersDesign, addresses, designOfOntologies)
        statementWithResourceService.create(grubersDesign, addresses, knowledgeEngineering)
        statementWithResourceService.create(grubersDesign, yields, ontoDesignCriteria)

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
        val subfieldPredicate = predicateService.create("has subfield").id!!

        //
        // Class
        //
        classService.create(CreateClassRequest(ClassId("Paper"), "Paper", null))

        //
        // Resource
        //
        resourceService.create("paper")
        val researchField = resourceService.create("Research field").id!!

        // Adding resources from the json file
        val mapper = jacksonObjectMapper()
        val inStream: InputStream? = javaClass.classLoader.getResourceAsStream("data/ResearchFields.json")
        val fields = mapper.readValue<List<ResearchField>>(inStream!!)
        for (field in fields) {
            val newField = resourceService.create(field.name).id!!
            statementWithResourceService.create(researchField, subfieldPredicate, newField)
            for (subfield in field.subfields) {
                val newSubfield = resourceService.create(subfield.name).id!!
                statementWithResourceService.create(newField, subfieldPredicate, newSubfield)
                for (subSubfield in subfield.subfields) {
                    val newSubSubfield = resourceService.create(subSubfield.name).id!!
                    statementWithResourceService.create(newSubfield, subfieldPredicate, newSubSubfield)
                }
            }
        }
    }

    private fun statementsPresent() =
        statementWithResourceService.totalNumberOfStatements() > 0 || statementWithLiteralService.totalNumberOfStatements() > 0
}

fun createPageable(page: Int?, items: Int?, sortBy: String?, desc: Boolean): Pageable {
    val sort = if (sortBy != null) Sort.by(sortBy) else Sort.unsorted()
    var size = items ?: 10 ; if (size < 1) size = 1
    val pageNr = if (page != null) page - 1 else 0
    return PageRequest.of(pageNr, size, if (desc) { sort.descending() } else { sort })
}

data class ResearchField(val name: String, val subfields: List<ResearchField> = listOf())

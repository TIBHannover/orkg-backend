package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.statements.domain.model.Object
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ExampleData(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
    private val statementRepository: StatementRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        //
        // Resources
        //
        val caseStudies = ResourceId("89AB")
        val designOfOntologies = ResourceId("789a")
        val fermatsLastTheorem = ResourceId("3456")
        val grubersDesign = ResourceId("b0b0b0")
        val knowledgeEngineering = ResourceId("6789")
        val mathProof = ResourceId("1234")
        val modularityTheorem = ResourceId("2345")
        val ontoDesignCriteria = ResourceId("5678")
        val tanimaConj = ResourceId("4567")
        val wilesProof = ResourceId("0a0a0a")

        resourceRepository.add(
            Resource(grubersDesign, "Gruber's design of ontologies")
        )
        resourceRepository.add(
            Resource(
                wilesProof,
                "Wiles's proof of Fermat's last theorem"
            )
        )
        resourceRepository.add(
            Resource(mathProof, "Mathematical proof")
        )
        resourceRepository.add(
            Resource(modularityTheorem, "Modularity theorem")
        )
        resourceRepository.add(
            Resource(
                fermatsLastTheorem,
                "Fermat's last theorem (conjecture)"
            )
        )
        resourceRepository.add(
            Resource(tanimaConj, "Taniyama-Shimura-Weil conjecture")
        )
        resourceRepository.add(
            Resource(
                ontoDesignCriteria,
                "Design criteria for ontologies"
            )
        )
        resourceRepository.add(
            Resource(knowledgeEngineering, "Knowledge Engineering")
        )
        resourceRepository.add(
            Resource(designOfOntologies, "Design of ontologies")
        )
        resourceRepository.add(
            Resource(caseStudies, "Case studies")
        )

        //
        // Predicates
        //
        val addresses = PredicateId("P123")
        val employs = PredicateId("Pabc")
        val yields = PredicateId("P234")

        predicateRepository.add(Predicate(addresses, "addresses"))
        predicateRepository.add(Predicate(yields, "yields"))
        predicateRepository.add(Predicate(employs, "employs"))

        //
        // Statements
        //
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                wilesProof,
                employs,
                Object.Resource(mathProof)
            )
        )
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                wilesProof,
                addresses,
                Object.Resource(tanimaConj)
            )
        )
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                wilesProof,
                addresses,
                Object.Resource(fermatsLastTheorem)
            )
        )
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                wilesProof,
                yields,
                Object.Resource(modularityTheorem)
            )
        )

        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                grubersDesign,
                employs,
                Object.Resource(caseStudies)
            )
        )
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                grubersDesign,
                addresses,
                Object.Resource(designOfOntologies)
            )
        )
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                grubersDesign,
                addresses,
                Object.Resource(knowledgeEngineering)
            )
        )
        statementRepository.add(
            Statement(
                statementRepository.nextIdentity(),
                grubersDesign,
                yields,
                Object.Resource(ontoDesignCriteria)
            )
        )
    }
}

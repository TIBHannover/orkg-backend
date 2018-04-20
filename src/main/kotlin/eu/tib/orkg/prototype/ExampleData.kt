package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ExampleData(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        //
        // Resources
        //
        resourceRepository.add(
            Resource(
                ResourceId("1234"),
                "Mathematical proof"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("2345"),
                "Modularity theorem"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("3456"),
                "Fermat's last theorem (conjecture)"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("4567"),
                "Taniyama-Shimura-Weil conjecture"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("5678"),
                "Design criteria for ontologies"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("6789"),
                "Knowledge Engineering"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("789a"),
                "Design of ontologies"
            )
        )
        resourceRepository.add(
            Resource(
                ResourceId("89AB"),
                "Case studies"
            )
        )
        //
        // Predicates
        //
        predicateRepository.add(
            Predicate(
                PredicateId("P123"),
                "addresses"
            )
        )
        predicateRepository.add(
            Predicate(
                PredicateId("P234"),
                "yields"
            )
        )
        predicateRepository.add(
            Predicate(
                PredicateId("Pabc"),
                "employs"
            )
        )
        predicateRepository.add(
            Predicate(
                PredicateId("PDEF"),
                "addresses"
            )
        )
    }
}

package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.graphdb.indexing.domain.model.IndexService
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryServiceNeo
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationServiceNeo
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("development", "docker")
class ObservatoriesMigration(
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService,
    private val organizationServiceNeo: OrganizationServiceNeo,
    private val observatoryService: ObservatoryService,
    private val observatoryServiceNeo: ObservatoryServiceNeo,
    private val predicateService: PredicateService,
    private val statementService: StatementService,
    private val classService: ClassService,
    private val indexService: IndexService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        if (organizationServiceNeo.listOrganizations().isNotEmpty())
            return

        val list = organizationService.listOrganizations()
        list.map { organizationServiceNeo.create(it.id!!.value, it.name!!, it.createdBy!!, it.homepage!!) }

        val observatoryList = observatoryService.listObservatories()
        observatoryList.map {
            val observatoryResponse =
                observatoryServiceNeo.create(it.id!!.value, it.name!!, it.description!!, it.researchField!!.id!!)
            for (organizationId in it.organizationIds) {
                observatoryServiceNeo.createRelationInObservatoryOrganization(organizationId.value, observatoryResponse.id!!.value)
            }
        }
    }
}

package org.orkg

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Component
@Profile("development", "docker", "production")
class InitialDataSetup(
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val statementService: StatementUseCases,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.init.setup.directory}")
    private var directory: String? = null

    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments) {
        logger.info("Begin setting up initial data...")

        createClasses(readFile<List<RequiredClassCommand>>("$directory/classes.json"))
        createPredicates(readFile<List<RequiredPredicateCommand>>("$directory/predicates.json"))
        createResources(readFile<List<RequiredResourceCommand>>("$directory/resources.json"))
        createResearchFields(readFile<RequiredResearchFieldCommand>("$directory/research_fields.json"))

        logger.info("End of initial data setup...")
    }

    private fun createClasses(classes: List<RequiredClassCommand>) {
        classes.forEach { (id, label, uri) ->
            val classByURI = uri?.let { classRepository.findByUri(it.toString()) } ?: Optional.empty()
            val classById = classRepository.findById(id)

            if (classById.isPresent && classByURI.isPresent && classById.get().id != classByURI.get().id) {
                logger.warn("""Class with URI "$uri" is already assigned to class with id "${classById.get().id}".""")
                return@forEach
            }

            if (classById.isEmpty) {
                classRepository.save(Class(id, label, uri, OffsetDateTime.now(clock)))
            }
        }
    }

    private fun createPredicates(predicates: List<RequiredPredicateCommand>) {
        predicates.forEach { (id, label) ->
            if (predicateRepository.findById(id).isEmpty) {
                predicateRepository.save(Predicate(id, label, OffsetDateTime.now(clock)))
            }
        }
    }

    private fun createResources(resources: List<RequiredResourceCommand>) {
        resources.forEach { (id, label, classes) ->
            if (resourceRepository.findById(id).isEmpty) {
                resourceRepository.save(Resource(id, label, OffsetDateTime.now(clock), classes))
            }
        }
    }

    private fun createResearchFields(researchFields: RequiredResearchFieldCommand) {
        if (resourceRepository.findAll(PageRequests.SINGLE, includeClasses = setOf(Classes.researchField)).isEmpty) {
            createResearchFields(listOf(researchFields))
        } else {
            logger.info("Skipping research field creation because at least one research field is already present.")
        }
    }

    private fun createResearchFields(researchFields: List<RequiredResearchFieldCommand>) {
        researchFields.forEach { researchField ->
            if (resourceRepository.findById(researchField.id).isEmpty) {
                val resource = Resource(
                    id = researchField.id,
                    label = researchField.label,
                    classes = setOf(Classes.researchField),
                    createdAt = OffsetDateTime.now(clock)
                )
                resourceRepository.save(resource)
            }
            if (researchField.subfields.isNotEmpty()) {
                createResearchFields(researchField.subfields)
            }
            researchField.subfields.forEach { subField ->
                statementService.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = ContributorId.UNKNOWN,
                        subjectId = researchField.id,
                        predicateId = Predicates.hasSubfield,
                        objectId = subField.id
                    )
                )
            }
        }
    }

    private inline fun <reified T> readFile(file: String): T =
        objectMapper.readValue<T>(this::class.java.classLoader.getResource(file)!!.openStream())

    data class RequiredClassCommand(
        val id: ThingId,
        val label: String,
        val uri: ParsedIRI?,
    )

    data class RequiredPredicateCommand(
        val id: ThingId,
        val label: String,
    )

    data class RequiredResourceCommand(
        val id: ThingId,
        val label: String,
        val classes: Set<ThingId>,
    )

    data class RequiredResearchFieldCommand(
        val id: ThingId,
        val label: String,
        val subfields: List<RequiredResearchFieldCommand> = emptyList(),
    )
}

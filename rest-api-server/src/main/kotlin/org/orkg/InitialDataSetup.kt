package org.orkg

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import javax.validation.constraints.NotBlank
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@ComponentScan("org.orkg.configuration")
@Profile("development", "docker")
@Order(1)
class InitialDataSetup(
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.init.setup.entities-file}")
    @NotBlank
    private var entitiesFile: String? = null

    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments?) {
        logger.info("Begin setting up initial data...")

        val mainCommand: CreateMainCommand = objectMapper.readValue(
            this::class.java.classLoader.getResource(entitiesFile)!!.openStream(),
            CreateMainCommand::class.java
        )

        createClasses(mainCommand.classes)
        createPredicates(mainCommand.predicates)
        createResources(mainCommand.resources)

        logger.info("End of initial data setup...")
    }

    /**
     * Create Classes
     */
    private fun createClasses(classList: List<CreateClassCommand>) {
        classList.forEach { command ->
            val classByURI = command.uri?.let { classRepository.findByUri(it.toString()) } ?: Optional.empty()
            val classById = classRepository.findById(command.id)

            if (classById.isPresent && classByURI.isPresent && classById.get().id != classByURI.get().id)
                throw Exception("ID mismatch for class ID: ${classById.get().id}")

            if (classById.isEmpty) {
                classRepository.save(
                    Class(
                        id = command.id,
                        label = command.label,
                        uri = command.uri,
                        createdAt = OffsetDateTime.now(clock)
                    )
                )
            }
        }
    }

    /**
     * Create Predicates
     */
    private fun createPredicates(predicateList: List<CreatePredicatesCommand>) {
        predicateList.forEach { (id, label) ->
            if (predicateRepository.findById(id).isEmpty) {
                predicateRepository.save(
                    Predicate(
                        id = id,
                        label = label,
                        createdAt = OffsetDateTime.now(clock)
                    )
                )
            }
        }
    }

    /**
     * Create Resources
     */
    private fun createResources(resourceList: List<CreateResourcesCommand>) {
        resourceList.forEach { (id, label, classes) ->
            if (resourceRepository.findById(id).isEmpty) {
                resourceRepository.save(
                    Resource(
                        id = id,
                        label = label,
                        classes = classes,
                        createdAt = OffsetDateTime.now(clock)
                    )
                )
            }
        }
    }

    data class CreateClassCommand(val id: ThingId, val label: String, val uri: ParsedIRI?)
    data class CreatePredicatesCommand(val id: ThingId, val label: String)
    data class CreateResourcesCommand(val id: ThingId, val label: String, val classes: Set<ThingId>)
    data class CreateMainCommand(
        val classes: List<CreateClassCommand>,
        val predicates: List<CreatePredicatesCommand>,
        val resources: List<CreateResourcesCommand>
    )
}

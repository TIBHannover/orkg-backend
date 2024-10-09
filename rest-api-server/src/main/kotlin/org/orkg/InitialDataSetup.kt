package org.orkg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
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
@Profile("development", "docker", "production")
@Order(1)
class InitialDataSetup(
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.init.setup.directory}")
    private var directory: String? = null

    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments?) {
        logger.info("Begin setting up initial data...")

        createClasses(readFile<List<RequiredClassDefinition>>("$directory/classes.json"))
        createPredicates(readFile<List<RequiredPredicateDefinition>>("$directory/predicates.json"))
        createResources(readFile<List<RequiredResourceDefinition>>("$directory/resources.json"))

        logger.info("End of initial data setup...")
    }

    private fun createClasses(classes: List<RequiredClassDefinition>) {
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

    private fun createPredicates(predicates: List<RequiredPredicateDefinition>) {
        predicates.forEach { (id, label) ->
            if (predicateRepository.findById(id).isEmpty) {
                predicateRepository.save(Predicate(id, label, OffsetDateTime.now(clock)))
            }
        }
    }

    private fun createResources(resources: List<RequiredResourceDefinition>) {
        resources.forEach { (id, label, classes) ->
            if (resourceRepository.findById(id).isEmpty) {
                resourceRepository.save(Resource(id, label, OffsetDateTime.now(clock), classes))
            }
        }
    }

    private inline fun <reified T> readFile(file: String): T =
        objectMapper.readValue<T>(this::class.java.classLoader.getResource(file)!!)

    data class RequiredClassDefinition(
        val id: ThingId,
        val label: String,
        val uri: ParsedIRI?
    )

    data class RequiredPredicateDefinition(
        val id: ThingId,
        val label: String
    )

    data class RequiredResourceDefinition(
        val id: ThingId,
        val label: String,
        val classes: Set<ThingId>
    )
}

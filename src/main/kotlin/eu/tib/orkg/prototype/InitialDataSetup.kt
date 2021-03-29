package eu.tib.orkg.prototype

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.configuration.InputInjection
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.IndexService
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementService
import java.io.FileReader
import java.net.URI
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@ComponentScan("eu.tib.orkg.prototype.configuration")
@Profile("development", "docker")
class DataInitializer(
    private val classService: ClassService,
    private val predicateService: PredicateService,
    private val statementService: StatementService,
    private val resourceService: ResourceService,
    private val config: InputInjection,
    private val indexService: IndexService
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)
    /**
     * Creating new classes and predicates only
     * if they don't exist
     */
    override fun run(args: ApplicationArguments?) {
        logger.info("Begin setting up initial data...")

        val fileParser =
            EntityConfigurationParser(FileReader(
                this::class.java.classLoader.getResource(config.entitiesFile).file))
        val mainCommand: CreateMainCommand = fileParser.parseInitialData()

        createClasses(mainCommand.classList)
        createPredicates(mainCommand.predicateList)

        logger.info("End of initial data setup...")

        indexService.verifyIndices()

        val jsonContent =
            String(this::class.java.classLoader.getResourceAsStream(config.subResearchFieldsFile).readAllBytes())

        createSubResearchFields(jsonContent)
    }

    /**
     * Create Classes
     */
    private fun createClasses(classList: List<CreateClassCommand>) {
        classList.forEach { createClassCommand ->
            val classURI: URI? = createClassCommand.uri?.let { URI.create(it) }

            classService.createIfNotExists(
                ClassId(createClassCommand.id),
                createClassCommand.label,
                classURI
            )
        }
    }

    /**
     * Create Predicates
     */
    private fun createPredicates(predicateList: List<CreatePredicatesCommand>) {
        logger.info("Creating Predicates...")
        predicateList.forEach { createPredicateCommand ->
            logger.info("Creating Predicates...$createPredicateCommand.id")
            predicateService.createIfNotExists(
                PredicateId(createPredicateCommand.id),
                createPredicateCommand.label)
        }
    }

    /**
     * Create Sub Research Fields
     */
    fun createSubResearchFields(jsonContent: String) {
        logger.info("Creating Sub Research Fields...")
        var subfieldPredicate: PredicateId? = predicateService.findById(PredicateId("P36")).get().id
        val arrResult = Json.decodeFromString<ArrayList<SubResearchFields>>(jsonContent)

        arrResult.map {
            if (subfieldPredicate != null)
                unmarshall(it.name, it.subfields, subfieldPredicate)
        }

        logger.info("Completed creation of Sub Research Fields...")
    }

    private fun unmarshall(parent: String?, arrSubResearchFields: ArrayList<SubResearchFields>?, predicateId: PredicateId) {
        if (arrSubResearchFields?.isNotEmpty() == true) {
            arrSubResearchFields.map {
                if (it.subfields != null) {
                    unmarshall(it.name, it.subfields, predicateId)
                } else {
                    if (parent != null) {
                        val parentResourceIdValue = getResourceId(parent)?.value
                        val childResourceIdValue = getResourceId(it.name as String)?.value
                        if (parentResourceIdValue != null && childResourceIdValue != null)
                            statementService.create(parentResourceIdValue, predicateId, childResourceIdValue)
                    }
                }
            }
        }
    }
    private fun getResourceId(label: String): ResourceId? {
        return if (resourceService.getResourceByLabel(label).isEmpty) {
            resourceService.create(label).id
        } else {
            resourceService.getResourceByLabel(label).get().resourceId
        }
    }
}

@Serializable
data class SubResearchFields(
    @JsonProperty("name")
    var name: String? = null,
    @JsonProperty("subfields")
    var subfields: ArrayList<SubResearchFields>? = null
)

data class CreateClassCommand(val id: String, val label: String, val uri: String?)
data class CreatePredicatesCommand(val id: String, val label: String)
data class CreateMainCommand(val classList: List<CreateClassCommand>, val predicateList: List<CreatePredicatesCommand>)

fun escapeLiterals(literal: String): String {
    return literal
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("(\\r|\\n|\\r\\n)+".toRegex(), "\\\\n")
}

package eu.tib.orkg.prototype

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import java.io.Reader
import org.slf4j.LoggerFactory

/**
 * Class to read the file containing
 * classes and predicates
 */
class FileParser(private val reader: Reader) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    /**
     * Return a CreateMainCommand instance
     * comprising of a list of classes and
     * a list of predicates
     */
    fun parseInitialData(): CreateMainCommand {
        logger.info("Reading file content for initial data setup...")

        val fileContent = reader.readText()
        val configFactory = ConfigFactory.parseString(fileContent)

        val classes: ConfigList = configFactory.getList("classes")
        val predicates: ConfigList = configFactory.getList("predicates")

        return CreateMainCommand(
            getClassCommandList(classes),
            getPredicateCommandList(predicates)
        )
    }

    /**
     * Return a list of CreateClassCommand
     */
    @Suppress("UNCHECKED_CAST")
    private fun getClassCommandList(classes: ConfigList): List<CreateClassCommand> {
        val listClass = mutableListOf<CreateClassCommand>()

        classes.forEach {
            val classMap = it.unwrapped() as Map<String, String>

            val classId: String =
                classMap["id"] ?: throw Exception("A null value was found for id while importing classes")
            val classLabel: String =
                classMap["label"] ?: throw Exception("A null value was found for label while importing classes")

            val oClass = CreateClassCommand(
                classId,
                classLabel,
                classMap["uri"]
            )
            listClass.add(oClass)
        }
        return listClass
    }

    /**
     * Return a list of CreatePredicatesCommand
     */
    @Suppress("UNCHECKED_CAST")
    private fun getPredicateCommandList(predicates: ConfigList): List<CreatePredicatesCommand> {
        val listPredicates = mutableListOf<CreatePredicatesCommand>()

        predicates.forEach {
            val predicateMap = it.unwrapped() as Map<String, String>
            val predicateId: String =
                predicateMap["id"] ?: throw Exception("A null value was found for id while importing predicates")
            val predicateLabel: String =
                predicateMap["label"] ?: throw Exception("A null value was found for label while importing predicates")

            val oPredicate = CreatePredicatesCommand(
                predicateId,
                predicateLabel
            )
            listPredicates.add(oPredicate)
        }
        return listPredicates
    }
}

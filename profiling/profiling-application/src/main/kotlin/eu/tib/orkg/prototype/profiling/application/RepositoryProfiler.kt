package eu.tib.orkg.prototype.profiling.application

import eu.tib.orkg.prototype.profiling.domain.FunctionResult
import eu.tib.orkg.prototype.profiling.domain.RepeatedMeasurement
import eu.tib.orkg.prototype.profiling.spi.ProfilingResultWriterFactory
import eu.tib.orkg.prototype.profiling.spi.ValueGenerator
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.functions
import kotlin.system.measureTimeMillis
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ConfigurableApplicationContext

private const val repetitions = 3

abstract class RepositoryProfiler(
    private val context: ConfigurableApplicationContext,
    private val resultWriterFactory: ProfilingResultWriterFactory,
    valueGenerators: List<ValueGenerator<*>>
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private val generators = valueGenerators.associateBy { valueGenerator ->
        valueGenerator::class.allSupertypes.first { it.classifier == ValueGenerator::class }.arguments.first().type?.classifier!!
    }

    override fun run(args: ApplicationArguments?) {
        val functionMap = repositories.functionsToProfile()
        val repositoryCount = functionMap.keys.size
        val outputFile = File(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv")
        resultWriterFactory(outputFile).use { writer ->
            logger.info("Profiling $repositoryCount repositories with ${functionMap.values.flatten().size} functions")
            val repositories =  functionMap.entries.toList().sortedBy { it.key.simpleName }
            repositories.forEachIndexed { repositoryIndex, (repository, functions) ->
                logger.info("[${repositoryIndex + 1}/$repositoryCount] Profiling ${repository.simpleName}")
                val adapter = context.getBean(repository.java)
                val functionCount = functions.size
                val repositoryName = repository.simpleName!!
                functions.forEachIndexed { functionIndex, function ->
                    logger.info("[${repositoryIndex + 1}/$repositoryCount] [${functionIndex + 1}/$functionCount] Profiling function ${repository.simpleName}.${function.name}")
                    writer(profileFunction(adapter, repositoryName, function))
                }
            }
        }
        context.close()
    }

    private fun profileFunction(adapter: Any, repositoryName: String, function: KFunction<*>): FunctionResult {
        val random = Random("$repositoryName.$function".hashCode())
        val combinations = function.parameters
            .drop(1)
            .map { it.name!! to randomInstances(random, it.name!!, it.type) }
            .allCombinations()
        clearQueryCache()
        val measurements = combinations.map { parameters ->
            val millis = (0 until repetitions).map {
                measureTimeMillis {
                    function.call(adapter, *parameters.values.toTypedArray())
                }
            }
            RepeatedMeasurement(millis, parameters)
        }
        return FunctionResult(
            repositoryName = repositoryName,
            functionName = function.name,
            measurements = measurements
        )
    }

    private fun randomInstances(random: Random, name: String, type: KType): List<Any> =
        generators[type.classifier]
            ?.let { it(random, name, type, ::randomInstances) }
            ?: throw RuntimeException("No value generator for type $type")

    private fun List<KClass<out Any>>.functionsToProfile() =
        associateWith { repository ->
            repository.functions
                .filter { doProfileFunction(it) }
                .sortedBy { it.name }
        }

    private fun List<Pair<String, List<Any>>>.allCombinations(): List<Map<String, Any>> {
        if (isEmpty()) {
            return listOf(emptyMap())
        }
        val states = map { it.second.size }.reduce { a, b -> a * b }
        val results: MutableList<MutableMap<String, Any>> = MutableList(states) { mutableMapOf() }
        forEach { (parameter, arguments) ->
            results.forEachIndexed { index, map ->
                map[parameter] = arguments[index % arguments.size]
            }
        }
        return results
    }

    protected abstract val repositories: List<KClass<out Any>>

    protected abstract fun clearQueryCache()

    protected abstract fun doProfileFunction(function: KFunction<*>): Boolean
}

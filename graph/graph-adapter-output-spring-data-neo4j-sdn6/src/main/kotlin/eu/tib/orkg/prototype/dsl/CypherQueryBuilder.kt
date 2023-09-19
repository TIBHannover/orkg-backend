package eu.tib.orkg.prototype.dsl

import java.util.*
import java.util.function.BiFunction
import java.util.stream.Collectors
import kotlin.reflect.KClass
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.ExposesReturning
import org.neo4j.cypherdsl.core.Functions.count
import org.neo4j.cypherdsl.core.Functions.countDistinct
import org.neo4j.cypherdsl.core.ResultStatement
import org.neo4j.cypherdsl.core.Statement
import org.neo4j.cypherdsl.core.StatementBuilder.BuildableStatement
import org.neo4j.cypherdsl.core.StatementBuilder.TerminalExposesSkip
import org.neo4j.driver.Record
import org.neo4j.driver.summary.ResultSummary
import org.neo4j.driver.types.TypeSystem
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.data.neo4j.core.Neo4jClient

interface QueryCache {
    fun getOrPut(key: Any, valueSupplier: () -> String): String
}

class CypherQueryBuilder(
    private val neo4jClient: Neo4jClient,
    private val queryCache: QueryCache = DefaultQueryCache
) {
    fun <T : Statement> withQuery(
        query: () -> BuildableStatement<T>
    ): SingleQueryBuilder.ExposesWithParameterAndFetchAsAndRun =
        SingleQueryBuilderImpl.WithParameterAndFetchAsAndRunBuilder(neo4jClient, queryCache, query)

    fun <T> withCommonQuery(commonQuery: () -> T): PagedQueryBuilder.ExposesPagedWithQuery<T> =
        PagedQueryBuilderImpl.PagedWithQueryBuilder(neo4jClient, queryCache, commonQuery)

    companion object {
        private object DefaultQueryCache : QueryCache {
            private val cache: MutableMap<Any, String> = mutableMapOf()

            override fun getOrPut(key: Any, valueSupplier: () -> String): String =
                cache.getOrPut(key, valueSupplier)
        }
    }
}

object SingleQueryBuilder {
    interface ExposesWithParameters {
        fun withParameters(vararg parameters: Pair<String, Any?>): ExposesFetchAsAndRun =
            withParameters(parameters.toMap())

        fun withParameters(parameters: Map<String, Any?>): ExposesFetchAsAndRun
    }

    interface ExposesRun {
        fun run(): ResultSummary
    }

    interface ExposesFetchAs {
        fun <T : Any> fetchAs(targetClass: KClass<T>): ExposesMappedByAndFetch<T>
    }

    interface ExposesWithParameterAndFetchAsAndRun : ExposesWithParameters, ExposesFetchAs, ExposesRun

    interface ExposesFetchAsAndRun : ExposesFetchAs, ExposesRun

    interface ExposesMappedBy<T> {
        fun mappedBy(mappingFunction: (TypeSystem, Record) -> T): ExposesFetch<T>

        fun mappedBy(mappingFunction: BiFunction<TypeSystem, Record, T>): ExposesFetch<T> =
            mappedBy(mappingFunction::apply)
    }

    interface ExposesFetch<T> {
        fun fetch(): Neo4jClient.RecordFetchSpec<T>

        fun one(): Optional<T> = fetch().one()

        fun first(): Optional<T> = fetch().first()

        fun all(): MutableCollection<T> = fetch().all()
    }

    interface ExposesMappedByAndFetch<T> : ExposesMappedBy<T>, ExposesFetch<T>

    inline fun <reified T : Any> ExposesFetchAs.fetchAs(): ExposesMappedByAndFetch<T> = fetchAs(T::class)

    inline fun <reified T : Any> ExposesFetchAs.mappedBy(noinline mappingFunction: (TypeSystem, Record) -> T): ExposesFetch<T> =
        fetchAs(T::class).mappedBy(mappingFunction)

    inline fun <reified T : Any> ExposesFetchAs.mappedBy(mappingFunction: BiFunction<TypeSystem, Record, T>): ExposesFetch<T> =
        fetchAs(T::class).mappedBy(mappingFunction)
}

class SingleQueryBuilderImpl {
    data class WithParameterAndFetchAsAndRunBuilder<T : Statement>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>
    ) : SingleQueryBuilder.ExposesWithParameterAndFetchAsAndRun {
        override fun withParameters(
            parameters: Map<String, Any?>
        ): SingleQueryBuilder.ExposesFetchAsAndRun =
            FetchAsAndRunBuilder(neo4jClient, queryCache, query, parameters)

        override fun <T : Any> fetchAs(targetClass: KClass<T>): SingleQueryBuilder.ExposesMappedByAndFetch<T> =
            withParameters(emptyMap()).fetchAs(targetClass)

        override fun run(): ResultSummary =
            withParameters(emptyMap()).run()
    }

    data class FetchAsAndRunBuilder<T : Statement>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>,
        private val parameters: Map<String, Any?>
    ) : SingleQueryBuilder.ExposesFetchAsAndRun {
        override fun <T : Any> fetchAs(targetClass: KClass<T>): SingleQueryBuilder.ExposesMappedByAndFetch<T> =
            MappedByBuilderAndFetch(neo4jClient, queryCache, query, parameters, targetClass)

        override fun run(): ResultSummary =
            neo4jClient.query(queryCache.getOrPut(query) { query().build().cypher })
                .bindAll(parameters)
                .run()
    }

    data class MappedByBuilderAndFetch<T : Statement, R : Any>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>,
        private val parameters: Map<String, Any?>,
        private val targetClass: KClass<R>
    ) : SingleQueryBuilder.ExposesMappedByAndFetch<R> {
        override fun mappedBy(mappingFunction: (TypeSystem, Record) -> R): SingleQueryBuilder.ExposesFetch<R> =
            FetchBuilder(neo4jClient, queryCache, query, parameters, targetClass, mappingFunction)

        override fun fetch(): Neo4jClient.RecordFetchSpec<R> =
            FetchBuilder(neo4jClient, queryCache, query, parameters, targetClass).fetch()
    }

    data class FetchBuilder<T : Statement, R : Any>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>,
        private val parameters: Map<String, Any?>,
        private val targetClass: KClass<R>,
        private val mappingFunction: ((TypeSystem, Record) -> R)? = null
    ) : SingleQueryBuilder.ExposesFetch<R> {
        override fun fetch(): Neo4jClient.RecordFetchSpec<R> =
            neo4jClient.query(queryCache.getOrPut(query) { query().build().cypher })
                .bindAll(parameters)
                .fetchAs(targetClass.java)
                .let { if (mappingFunction != null) it.mappedBy(mappingFunction) else it }
    }
}

object PagedQueryBuilder {
    interface ExposesPagedWithQuery<T> {
        fun withQuery(query: (T) -> TerminalExposesSkip): ExposesPagedWithCountQuery<T>
    }

    interface ExposesPagedWithCountQuery<T> {
        fun withCountQuery(
            countQuery: (T) -> BuildableStatement<ResultStatement>
        ): ExposesPagedWithParametersAndFetchAs
    }

    interface ExposesPagedWithParameters {
        fun withParameters(vararg parameters: Pair<String, Any?>): ExposesPagedFetchAs =
            withParameters(parameters.toMap())

        fun withParameters(parameters: Map<String, Any?>): ExposesPagedFetchAs
    }

    interface ExposesPagedFetchAs {
        fun <T : Any> fetchAs(targetClass: KClass<T>): ExposesPagedMappedByAndFetch<T>
    }

    interface ExposesPagedWithParametersAndFetchAs : ExposesPagedWithParameters, ExposesPagedFetchAs

    interface ExposesPagedMappedBy<T> {
        fun mappedBy(mappingFunction: (TypeSystem, Record) -> T): ExposesPagedFetch<T>

        fun mappedBy(mappingFunction: BiFunction<TypeSystem, Record, T>): ExposesPagedFetch<T> =
            mappedBy(mappingFunction::apply)
    }

    interface ExposesPagedFetch<T> {
        fun fetch(pageable: Pageable): Page<T>
    }

    interface ExposesPagedMappedByAndFetch<T : Any> : ExposesPagedMappedBy<T>, ExposesPagedFetch<T>

    inline fun <reified T : Any> ExposesPagedFetchAs.fetchAs(): ExposesPagedMappedByAndFetch<T> = fetchAs(T::class)

    inline fun <reified T : Any> ExposesPagedFetchAs.mappedBy(noinline mappingFunction: (TypeSystem, Record) -> T): ExposesPagedFetch<T> =
        fetchAs(T::class).mappedBy(mappingFunction)

    inline fun <reified T : Any> ExposesPagedFetchAs.mappedBy(mappingFunction: BiFunction<TypeSystem, Record, T>): ExposesPagedFetch<T> =
        fetchAs(T::class).mappedBy(mappingFunction)

    fun <T : ExposesReturning> ExposesPagedWithCountQuery<T>.countOver(variable: String): ExposesPagedWithParametersAndFetchAs =
        withCountQuery { commonQuery -> commonQuery.returning(count(name(variable))) }

    fun <T : ExposesReturning> ExposesPagedWithCountQuery<T>.countDistinctOver(variable: String): ExposesPagedWithParametersAndFetchAs =
        withCountQuery { commonQuery -> commonQuery.returning(countDistinct(name(variable))) }
}

class PagedQueryBuilderImpl {
    data class PagedWithQueryBuilder<T>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T
    ) : PagedQueryBuilder.ExposesPagedWithQuery<T> {
        override fun withQuery(
            query: (T) -> TerminalExposesSkip
        ): PagedQueryBuilder.ExposesPagedWithCountQuery<T> =
            PagedWithCountQueryBuilder(neo4jClient, queryCache, commonQuery, query)
    }

    data class PagedWithCountQueryBuilder<T>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T,
        private val query: (T) -> TerminalExposesSkip
    ) : PagedQueryBuilder.ExposesPagedWithCountQuery<T> {
        override fun withCountQuery(
            countQuery: (T) -> BuildableStatement<ResultStatement>
        ): PagedQueryBuilder.ExposesPagedWithParametersAndFetchAs =
            PagedWithParametersAndFetchAsBuilder(neo4jClient, queryCache, commonQuery, query, countQuery)
    }

    data class PagedWithParametersAndFetchAsBuilder<T>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T,
        private val query: (T) -> TerminalExposesSkip,
        private val countQuery: (T) -> BuildableStatement<ResultStatement>
    ) : PagedQueryBuilder.ExposesPagedWithParametersAndFetchAs {
        override fun withParameters(
            parameters: Map<String, Any?>
        ): PagedQueryBuilder.ExposesPagedFetchAs =
            PagedFetchAsBuilder(neo4jClient, queryCache, commonQuery, query, countQuery, parameters)

        override fun <T : Any> fetchAs(
            targetClass: KClass<T>
        ): PagedQueryBuilder.ExposesPagedMappedByAndFetch<T> =
            withParameters(emptyMap()).fetchAs(targetClass)
    }

    data class PagedFetchAsBuilder<T>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T,
        private val query: (T) -> TerminalExposesSkip,
        private val countQuery: (T) -> BuildableStatement<ResultStatement>,
        private val parameters: Map<String, Any?>
    ) : PagedQueryBuilder.ExposesPagedFetchAs {
        override fun <R : Any> fetchAs(
            targetClass: KClass<R>
        ): PagedQueryBuilder.ExposesPagedMappedByAndFetch<R> =
            PagedMappedByAndFetchBuilder(
                neo4jClient, queryCache, commonQuery, query, countQuery, parameters, targetClass
            )
    }

    data class PagedMappedByAndFetchBuilder<T, R : Any>(
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T,
        private val query: (T) -> TerminalExposesSkip,
        private val countQuery: (T) -> BuildableStatement<ResultStatement>,
        private val parameters: Map<String, Any?>,
        private val targetClass: KClass<R>,
        private val mappingFunction: ((TypeSystem, Record) -> R)? = null
    ) : PagedQueryBuilder.ExposesPagedMappedByAndFetch<R> {
        override fun mappedBy(
            mappingFunction: (TypeSystem, Record) -> R
        ): PagedQueryBuilder.ExposesPagedFetch<R> =
            PagedMappedByAndFetchBuilder(
                neo4jClient, queryCache, commonQuery, query, countQuery, parameters, targetClass, mappingFunction
            )

        override fun fetch(pageable: Pageable): Page<R> {
            val contentQuery = queryCache.getOrPut(commonQuery to query) {
                query(commonQuery())
                    .skip(parameter("sdnSkip"))
                    .limit(parameter("sdnLimit"))
                    .build()
                    .cypher
            }
            val content = neo4jClient.query(contentQuery.sortedWith(pageable.sort))
                .bindAll(parameters + ("sdnSkip" to pageable.offset) + ("sdnLimit" to pageable.pageSize))
                .fetchAs(targetClass.java)
                .let { if (mappingFunction != null) it.mappedBy(mappingFunction) else it }
                .all()
                .toList()
            val countQuery = queryCache.getOrPut(commonQuery to countQuery) {
                countQuery(commonQuery())
                    .build()
                    .cypher
            }
            val count = neo4jClient.query(countQuery)
                .bindAll(parameters)
                .fetchAs(Long::class.java)
                .one()
                .orElse(0)
            return PageImpl(content, pageable, count)
        }

        private fun String.sortedWith(sort: Sort): String =
            if (sort.isUnsorted) {
                this
            } else {
                StringBuilder(this)
                    .insert(lastIndexOf("SKIP"),"ORDER BY ${sort.toNeo4jSnippet()} ")
                    .toString()
            }

        private fun Sort.toNeo4jSnippet(): String =
            stream().map { it.toNeo4jSnippet() }.collect(Collectors.joining(", "))

        private fun Order.toNeo4jSnippet(): String = buildString {
            if (isIgnoreCase) {
                append("toLower(").append(property).append(")")
            } else {
                append(property)
            }
            append(" ").append(direction)
        }
    }
}

package org.orkg.common.neo4jdsl

import java.util.*
import java.util.function.BiFunction
import kotlin.reflect.KClass
import org.neo4j.cypherdsl.core.Cypher.count
import org.neo4j.cypherdsl.core.Cypher.countDistinct
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.ExposesReturning
import org.neo4j.cypherdsl.core.ResultStatement
import org.neo4j.cypherdsl.core.Statement
import org.neo4j.cypherdsl.core.StatementBuilder.BuildableStatement
import org.neo4j.cypherdsl.core.StatementBuilder.TerminalExposesSkip
import org.neo4j.cypherdsl.core.renderer.Configuration
import org.neo4j.cypherdsl.core.renderer.Renderer
import org.neo4j.driver.Record
import org.neo4j.driver.summary.ResultSummary
import org.neo4j.driver.types.TypeSystem
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient

interface QueryCache {
    fun getOrPut(key: Any, valueSupplier: () -> ConfigurationAwareStatement): ConfigurationAwareStatement

    object Uncached : QueryCache {
        override fun getOrPut(key: Any, valueSupplier: () -> ConfigurationAwareStatement): ConfigurationAwareStatement =
            valueSupplier()
    }
}

data class CypherQueryBuilderFactory(
    private val configuration: Configuration,
    private val neo4jClient: Neo4jClient,
) {
    fun newBuilder(queryCache: QueryCache = DefaultQueryCache): CypherQueryBuilder =
        CypherQueryBuilder(configuration, neo4jClient, queryCache)

    companion object {
        private object DefaultQueryCache : QueryCache {
            private val cache: MutableMap<Any, ConfigurationAwareStatement> = mutableMapOf()

            override fun getOrPut(key: Any, valueSupplier: () -> ConfigurationAwareStatement): ConfigurationAwareStatement =
                cache.getOrPut(key, valueSupplier)
        }
    }
}

data class CypherQueryBuilder(
    private val configuration: Configuration,
    private val neo4jClient: Neo4jClient,
    private val queryCache: QueryCache
) {
    fun <T : Statement> withQuery(
        query: () -> BuildableStatement<T>
    ): SingleQueryBuilder.ExposesWithParameterAndFetchAsAndRun =
        SingleQueryBuilderImpl.WithParameterAndFetchAsAndRunBuilder(configuration, neo4jClient, queryCache, query)

    fun <T> withCommonQuery(commonQuery: () -> T): PagedQueryBuilder.ExposesPagedWithQuery<T> =
        PagedQueryBuilderImpl.PagedWithQueryBuilder(configuration, neo4jClient, queryCache, commonQuery)
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
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>
    ) : SingleQueryBuilder.ExposesWithParameterAndFetchAsAndRun {
        override fun withParameters(
            parameters: Map<String, Any?>
        ): SingleQueryBuilder.ExposesFetchAsAndRun =
            FetchAsAndRunBuilder(configuration, neo4jClient, queryCache, query, parameters)

        override fun <T : Any> fetchAs(targetClass: KClass<T>): SingleQueryBuilder.ExposesMappedByAndFetch<T> =
            withParameters(emptyMap()).fetchAs(targetClass)

        override fun run(): ResultSummary =
            withParameters(emptyMap()).run()
    }

    data class FetchAsAndRunBuilder<T : Statement>(
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>,
        private val parameters: Map<String, Any?>
    ) : SingleQueryBuilder.ExposesFetchAsAndRun {
        override fun <T : Any> fetchAs(targetClass: KClass<T>): SingleQueryBuilder.ExposesMappedByAndFetch<T> =
            MappedByBuilderAndFetch(configuration, neo4jClient, queryCache, query, parameters, targetClass)

        override fun run(): ResultSummary =
            neo4jClient.query(queryCache.getOrPut(cacheKey()) { query().build(configuration) }.cypher)
                .bindAll(parameters)
                .run()

        private fun cacheKey(): Pair<() -> BuildableStatement<T>, Configuration> = query to configuration
    }

    data class MappedByBuilderAndFetch<T : Statement, R : Any>(
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>,
        private val parameters: Map<String, Any?>,
        private val targetClass: KClass<R>
    ) : SingleQueryBuilder.ExposesMappedByAndFetch<R> {
        override fun mappedBy(mappingFunction: (TypeSystem, Record) -> R): SingleQueryBuilder.ExposesFetch<R> =
            FetchBuilder(configuration, neo4jClient, queryCache, query, parameters, targetClass, mappingFunction)

        override fun fetch(): Neo4jClient.RecordFetchSpec<R> =
            FetchBuilder(configuration, neo4jClient, queryCache, query, parameters, targetClass).fetch()
    }

    data class FetchBuilder<T : Statement, R : Any>(
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val query: () -> BuildableStatement<T>,
        private val parameters: Map<String, Any?>,
        private val targetClass: KClass<R>,
        private val mappingFunction: ((TypeSystem, Record) -> R)? = null
    ) : SingleQueryBuilder.ExposesFetch<R> {
        override fun fetch(): Neo4jClient.RecordFetchSpec<R> =
            neo4jClient.query(queryCache.getOrPut(cacheKey()) { query().build(configuration) }.cypher)
                .bindAll(parameters)
                .fetchAs(targetClass.java)
                .let { if (mappingFunction != null) it.mappedBy(mappingFunction) else it }

        private fun cacheKey(): Pair<() -> BuildableStatement<T>, Configuration> = query to configuration
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
        fun mappedBy(mappingFunction: (TypeSystem, Record) -> T): ExposesPagedFetchAndCount<T>

        fun mappedBy(mappingFunction: BiFunction<TypeSystem, Record, T>): ExposesPagedFetchAndCount<T> =
            mappedBy(mappingFunction::apply)
    }

    interface ExposesPagedFetchAndCount<T> {
        fun fetch(pageable: Pageable, appendSort: Boolean = true): Page<T>

        fun count(): Long
    }

    interface ExposesPagedMappedByAndFetch<T : Any> : ExposesPagedMappedBy<T>, ExposesPagedFetchAndCount<T>

    inline fun <reified T : Any> ExposesPagedFetchAs.fetchAs(): ExposesPagedMappedByAndFetch<T> = fetchAs(T::class)

    inline fun <reified T : Any> ExposesPagedFetchAs.mappedBy(noinline mappingFunction: (TypeSystem, Record) -> T): ExposesPagedFetchAndCount<T> =
        fetchAs(T::class).mappedBy(mappingFunction)

    inline fun <reified T : Any> ExposesPagedFetchAs.mappedBy(mappingFunction: BiFunction<TypeSystem, Record, T>): ExposesPagedFetchAndCount<T> =
        fetchAs(T::class).mappedBy(mappingFunction)

    fun <T : ExposesReturning> ExposesPagedWithCountQuery<T>.countOver(variable: String): ExposesPagedWithParametersAndFetchAs =
        withCountQuery { commonQuery -> commonQuery.returning(count(name(variable))) }

    fun <T : ExposesReturning> ExposesPagedWithCountQuery<T>.countDistinctOver(variable: String): ExposesPagedWithParametersAndFetchAs =
        withCountQuery { commonQuery -> commonQuery.returning(countDistinct(name(variable))) }
}

class PagedQueryBuilderImpl {
    data class PagedWithQueryBuilder<T>(
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T
    ) : PagedQueryBuilder.ExposesPagedWithQuery<T> {
        override fun withQuery(
            query: (T) -> TerminalExposesSkip
        ): PagedQueryBuilder.ExposesPagedWithCountQuery<T> =
            PagedWithCountQueryBuilder(configuration, neo4jClient, queryCache, commonQuery, query)
    }

    data class PagedWithCountQueryBuilder<T>(
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T,
        private val query: (T) -> TerminalExposesSkip
    ) : PagedQueryBuilder.ExposesPagedWithCountQuery<T> {
        override fun withCountQuery(
            countQuery: (T) -> BuildableStatement<ResultStatement>
        ): PagedQueryBuilder.ExposesPagedWithParametersAndFetchAs =
            PagedWithParametersAndFetchAsBuilder(configuration, neo4jClient, queryCache, commonQuery, query, countQuery)
    }

    data class PagedWithParametersAndFetchAsBuilder<T>(
        private val configuration: Configuration,
        private val neo4jClient: Neo4jClient,
        private val queryCache: QueryCache,
        private val commonQuery: () -> T,
        private val query: (T) -> TerminalExposesSkip,
        private val countQuery: (T) -> BuildableStatement<ResultStatement>
    ) : PagedQueryBuilder.ExposesPagedWithParametersAndFetchAs {
        override fun withParameters(
            parameters: Map<String, Any?>
        ): PagedQueryBuilder.ExposesPagedFetchAs =
            PagedFetchAsBuilder(configuration, neo4jClient, queryCache, commonQuery, query, countQuery, parameters)

        override fun <T : Any> fetchAs(
            targetClass: KClass<T>
        ): PagedQueryBuilder.ExposesPagedMappedByAndFetch<T> =
            withParameters(emptyMap()).fetchAs(targetClass)
    }

    data class PagedFetchAsBuilder<T>(
        private val configuration: Configuration,
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
                configuration, neo4jClient, queryCache, commonQuery, query, countQuery, parameters, targetClass
            )
    }

    data class PagedMappedByAndFetchBuilder<T, R : Any>(
        private val configuration: Configuration,
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
        ): PagedQueryBuilder.ExposesPagedFetchAndCount<R> =
            PagedMappedByAndFetchBuilder(
                configuration, neo4jClient, queryCache, commonQuery, query, countQuery, parameters, targetClass, mappingFunction
            )

        override fun fetch(pageable: Pageable, appendSort: Boolean): Page<R> {
            val contentQuery = queryCache.getOrPut(cacheKey(query)) {
                query(commonQuery())
                    .skip(parameter("skip"))
                    .limit(parameter("limit"))
                    .build(configuration)
            }
            val content = neo4jClient.query(contentQuery.cypher.let { if (appendSort) it.sortedWith(pageable.sort) else it })
                .bindAll(parameters + ("skip" to pageable.offset) + ("limit" to pageable.pageSize) + contentQuery.catalog.parameters)
                .fetchAs(targetClass.java)
                .let { if (mappingFunction != null) it.mappedBy(mappingFunction) else it }
                .all()
                .toList()
            return PageImpl(content, pageable, count())
        }

        override fun count(): Long {
            val countQuery = queryCache.getOrPut(cacheKey(countQuery)) {
                countQuery(commonQuery())
                    .build(configuration)
            }
            val count = neo4jClient.query(countQuery.cypher)
                .bindAll(parameters + countQuery.catalog.parameters)
                .fetchAs(Long::class.java)
                .one()
                .orElse(0)
            return count
        }

        private fun cacheKey(query: Any) =
            Triple(commonQuery, query, configuration)
    }
}

data class ConfigurationAwareStatement(
    private val statement: Statement,
    private val configuration: Configuration
) : Statement by statement {
    private val queryString by lazy { Renderer.getRenderer(configuration).render(statement) }

    override fun getCypher(): String = synchronized(this) { queryString }
}

private fun <T : Statement> BuildableStatement<T>.build(configuration: Configuration): ConfigurationAwareStatement =
    ConfigurationAwareStatement(build(), configuration)

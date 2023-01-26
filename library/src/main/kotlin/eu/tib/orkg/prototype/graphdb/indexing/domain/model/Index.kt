package eu.tib.orkg.prototype.graphdb.indexing.domain.model

import java.util.*

interface Neo4jIndex {
    fun toCypherQuery(): String
}

data class UniqueIndex(private val label: String, private val property: String) :
    Neo4jIndex {
    override fun toCypherQuery() = """CREATE CONSTRAINT ON (n:$label) ASSERT n.$property IS UNIQUE;"""
}

data class PropertyIndex(val label: String, val property: String) :
    Neo4jIndex {
    override fun toCypherQuery() = """CREATE INDEX ON :$label($property);"""
}

data class FullTextIndex(val label: String, val property: String) :
    Neo4jIndex {
    override fun toCypherQuery(): String {
        val indexName = "full_${label}_$property".lowercase(Locale.ENGLISH)
        return """CALL db.index.fulltext.createNodeIndex("$indexName", ["$label"], ["$property"])"""
    }
}

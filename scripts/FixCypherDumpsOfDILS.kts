#!/usr/bin/env kscript

import java.io.*

interface CypherStatement {
    fun toCypher(): String
}

data class Node(val type: String, val id: Long, val label: String) :
    CypherStatement {
    override fun toCypher() =
        """create (_$id:`$type` {`label`:"$label", `${type.toLowerCase()}_id`:$id})"""
}

data class Relationship(
    val from: Long,
    val to: Long,
    val id: Long,
    val type: String,
    val statementId: Long
) : CypherStatement {
    override fun toCypher() =
        """create (_$from)-[:`$type` {`predicate_id`:$id, `statement_id`:$statementId}]->(_$to)"""
}

data class Line(val line: String) : CypherStatement {
    override fun toCypher() = line
}

val nodes = mutableMapOf<Long, Node>()
val predicates = mutableMapOf<Long, Node>()
val statements = mutableMapOf<Long, Relationship>()

var counter = 0L

val node_regex =
    Regex("""^create \(_(\d+):`(Predicate|Resource|Literal)` \{`label`:"(.+)"\}\)""")

val rel_regex =
    Regex("""^create \(_(\d+)\)-\[:`(.+)` \{`predicate_id`:(\d+)\}\]->\(_(\d+)\)""")

fun String.toNode(offset: Long = 0): CypherStatement {
    val (id, type, label) = node_regex.find(this)!!.destructured
    val nodeId = id.toLong() + offset
    val node = Node(type, nodeId, label)
    when (type) {
        "Resource", "Literal" -> nodes[nodeId] = node
        "Predicate" -> predicates[nodeId] = node
    }
    return node
}

fun String.toRelationship(offset: Long = 0): CypherStatement {
    val (sub, type, pred, obj) = rel_regex.find(this)!!.destructured
    val subjectId = sub.toLong() + offset
    val predicateId = pred.toLong() + offset
    val objectId = obj.toLong() + offset
    val statementId = counter + offset
    val relationship =
        Relationship(subjectId, objectId, predicateId, type, statementId)
    // println("// $counter: ${nodes[subjectId.toLong()]!!.label} -- ${predicates[predicateId.toLong()]!!.label} -> ${nodes[objectId.toLong()]!!.label}")
    return relationship
}

val filename : String = args[0]
val offset: Long = args.getOrNull(1)?.toLong() ?: 0

File(filename).forEachLine { line: String ->
    val result: CypherStatement = when {
        node_regex.matches(line) -> {
            line.toNode(offset)
        }
        rel_regex.matches(line) -> {
            counter++
            line.toRelationship(offset)
        }
        else -> Line(line)
    }
    println(result.toCypher())
}

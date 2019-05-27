package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.LiteralObject
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.LiteralIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Literal")
data class Neo4jLiteral(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {
    @Property("label")
    @Required
    var label: String? = null

    @Property("literal_id")
    @Required
    @Convert(LiteralIdGraphAttributeConverter::class)
    var literalId: LiteralId? = null

    @Relationship(type = "HAS_VALUE_OF")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatementWithLiteral> = mutableSetOf()

    @JsonIgnore
    private var labels: MutableList<String> = mutableListOf()

    val classes: Set<ClassId>
        get() = labels.map(::ClassId).toSet()

    constructor(label: String, literalId: LiteralId) : this(null) {
        this.label = label
        this.literalId = literalId
    }

    fun toLiteral() = Literal(literalId, label!!, createdAt!!)

    fun toObject() = LiteralObject(literalId, label!!, createdAt!!, classes)
}

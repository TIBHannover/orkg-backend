package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceObject
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ResourceIdGraphAttributeConverter
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Labels
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.lang.StringBuilder

@NodeEntity(label = "Resource")
data class Neo4jResource(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {

    @Property("label")
    @Required
    var label: String? = null

    @Property("resource_id")
    @Required
    @Convert(ResourceIdGraphAttributeConverter::class)
    var resourceId: ResourceId? = null

    @Relationship(type = "RELATES_TO")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatementWithResource> = mutableSetOf()

    @Relationship(type = "RELATES_TO")
    @JsonIgnore
    var literals: MutableSet<Neo4jStatementWithLiteral> = mutableSetOf()

    @Relationship(type = "RELATES_TO", direction = Relationship.INCOMING)
    @JsonIgnore
    var objectOf: MutableSet<Neo4jStatementWithResource> = mutableSetOf()

    /**
     * List of node labels. Labels other than the `Resource` label are mapped to classes.
     */
    @Labels
    private var labels: MutableList<String> = mutableListOf()

    /**
     * The list of classes that this node belongs to.
     */
    val classes: Set<ClassId>
        get() = labels.map(::ClassId).toSet()

    constructor(label: String, resourceId: ResourceId) : this(null) {
        this.label = label
        this.resourceId = resourceId
    }

    fun toResource() = Resource(resourceId, label!!, createdAt, classes, objectOf.size, toRdfModel())

    fun toObject() = ResourceObject(resourceId, label!!, createdAt, classes)

    fun toObject(shared: Int) = ResourceObject(resourceId, label!!, createdAt, classes, shared)

    /**
     * Assign a class to this `Resource` node.
     */
    fun assignTo(clazz: String) = labels.add(clazz)

    fun toNTripleWithPrefix(): String {
        val cPrefix = "http://orkg.org/orkg/vocab/class/"
        val rPrefix = "http://orkg.org/orkg/vocab/resource/"
        val sb = StringBuilder()
        sb.append("<$rPrefix$resourceId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
        classes.forEach { sb.append("<$rPrefix$resourceId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
        sb.append("<$rPrefix$resourceId> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string> .")
        return sb.toString()
    }

    fun toRdfModel(): Model {
        var builder = ModelBuilder()
            .setNamespace("r", "http://orkg.org/orkg/vocab/resource/")
            .setNamespace("p", "http://orkg.org/orkg/vocab/predicate/")
            .setNamespace("c", "http://orkg.org/orkg/vocab/class/")
            .setNamespace(RDF.NS)
            .setNamespace(RDFS.NS)
        builder = builder.subject("r:$resourceId")
            .add(RDFS.LABEL, label)
            .add(RDF.TYPE, "c:Resource")
        classes.forEach { builder = builder.add(RDF.TYPE, "c:${it.value}") }
        resources.forEach { builder = builder.add("p:${it.predicateId}", "r:${it.`object`!!.resourceId}") }
        literals.forEach { builder = builder.add("p:${it.predicateId}", "\"${it.`object`!!.label}\"") }
        return builder.build()
    }
}

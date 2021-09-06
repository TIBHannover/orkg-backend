package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ClassIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.util.escapeLiterals
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship
import java.net.URI

@Node(primaryLabel = "Class")
class Neo4jClass() : Neo4jThing() {
    @Relationship(type = "RELATED", direction = Relationship.Direction.OUTGOING)
    @JsonIgnore
    var subjectOf: MutableSet<Neo4jClass> = mutableSetOf()

    @Property("class_id")
    var classId: ClassId? = null

    @Property("label")
    override var label: String? = null

    @Property("uri")
    var uri: String? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    constructor(label: String, classId: ClassId, createdBy: ContributorId = ContributorId.createUnknownContributor(), uri: URI?) : this() {
        this.label = label
        this.classId = classId
        this.uri = uri?.toString()
        this.createdBy = createdBy
    }

    fun toClass(): Class {
        val aURI: URI? = if (uri != null) URI.create(uri!!) else null
        val clazz = Class(classId!!, label!!, aURI, createdAt!!, createdBy = createdBy)
        clazz.rdf = toRdfModel()
        if (subjectOf.isNotEmpty())
            clazz.description = subjectOf.firstOrNull { it.classId?.value == "description" }?.label
        return clazz
    }

    fun toNTriple(): String {
        val cPrefix = RdfConstants.CLASS_NS
        val sb = StringBuilder()
        sb.append("<$cPrefix$classId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .\n")
        if (uri != null && !uri.isNullOrEmpty())
            sb.append("<$cPrefix$classId> <http://www.w3.org/2002/07/owl#equivalentClass> <$uri> .\n")
        sb.append("<$cPrefix$classId> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string> .")
        return sb.toString()
    }

    fun toRdfModel(): Model {
        var builder = ModelBuilder()
            .setNamespace("c", RdfConstants.CLASS_NS)
            .setNamespace(RDF.NS)
            .setNamespace(RDFS.NS)
            .setNamespace(OWL.NS)
            .subject("c:$classId")
            .add(RDFS.LABEL, label)
            .add(RDF.TYPE, "owl:Class")
        if (uri != null)
            builder = builder.add(OWL.EQUIVALENTCLASS, uri)
        return builder.build()
    }

    override val thingId: String?
        get() = classId?.value

    override fun toThing() = toClass()
}

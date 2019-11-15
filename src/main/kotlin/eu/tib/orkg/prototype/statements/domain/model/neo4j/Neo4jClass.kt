package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ClassIdGraphAttributeConverter
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.lang.StringBuilder
import java.net.URI

@NodeEntity(label = "Class")
data class Neo4jClass(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {
    @Property("class_id")
    @Required
    @Convert(ClassIdGraphAttributeConverter::class)
    var classId: ClassId? = null

    @Property("label")
    @Required
    var label: String? = null

    var uri: String? = null

    constructor(label: String, classId: ClassId) : this(null) {
        this.label = label
        this.classId = classId
    }

    fun toClass(): Class {
        val aURI: URI? = if (uri != null) URI.create(uri!!) else null
        return Class(classId!!, label!!, aURI, createdAt!!, toRdfModel())
    }

    fun toNTripleWithPrefix(): String {
        val cPrefix = "http://orkg.org/orkg/vocab/class/"
        val sb = StringBuilder()
        sb.append("<$cPrefix$classId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .\n")
        if (uri != null && !uri.isNullOrEmpty())
            sb.append("<$cPrefix$classId> <http://www.w3.org/2002/07/owl#equivalentClass> <$uri> .\n")
        sb.append("<$cPrefix$classId> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string> .")
        return sb.toString()
    }

    fun toRdfModel(): Model {
        var builder = ModelBuilder()
            .setNamespace("c", "http://orkg.org/orkg/vocab/class/")
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
}

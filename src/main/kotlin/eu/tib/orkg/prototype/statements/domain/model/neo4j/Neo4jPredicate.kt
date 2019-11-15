package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdGraphAttributeConverter
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Predicate")
data class Neo4jPredicate(
    @Id
    @GeneratedValue
    private var id: Long? = null,

    @Property("label")
    @Required
    var label: String? = null,

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    private var predicateId: PredicateId? = null
) : AuditableEntity() {
    fun toPredicate() = Predicate(predicateId, label!!, createdAt!!, toRdfModel())

    fun toNTripleWithPrefix(): String {
        val cPrefix = "https://orkg.org/c/"
        val pPrefix = "https://orkg.org/p/"
        return "<$pPrefix$predicateId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Predicate> .\n" +
            "<$pPrefix$predicateId> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string> ."
    }

    fun toRdfModel(): Model {
        val builder = ModelBuilder()
            .setNamespace("p", "http://orkg.org/orkg/vocab/property/")
            .setNamespace("c", "http://orkg.org/orkg/vocab/class/")
            .setNamespace(RDF.NS)
            .setNamespace(RDFS.NS)
            .subject("p:$predicateId")
            .add(RDFS.LABEL, label)
            .add(RDF.TYPE, "c:Predicate")
        return builder.build()
    }
}

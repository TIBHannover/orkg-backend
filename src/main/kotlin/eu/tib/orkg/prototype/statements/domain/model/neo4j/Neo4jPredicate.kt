package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdGraphAttributeConverter
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.UUIDGraphAttributeConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.util.UUID

@NodeEntity(label = "Predicate")
data class Neo4jPredicate(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Property("label")
    @Required
    override var label: String? = null,

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    private var predicateId: PredicateId? = null,

    @Property("created_by")
    @Convert(UUIDGraphAttributeConverter::class)
    var createdBy: UUID = UUID(0, 0)
) : Neo4jThing, AuditableEntity() {

    fun toPredicate(): Predicate {
        val pred = Predicate(predicateId, label!!, createdAt!!, createdBy = createdBy)
        pred.rdf = toRdfModel()
        return pred
    }

    override val thingId: String?
        get() = predicateId?.value

    override fun toThing() = toPredicate()

    fun toNTriple(): String {
        val cPrefix = RdfConstants.CLASS_NS
        val pPrefix = RdfConstants.PREDICATE_NS
        return "<$pPrefix$predicateId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Predicate> .\n" +
            "<$pPrefix$predicateId> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string> ."
    }

    fun toRdfModel(): Model {
        val builder = ModelBuilder()
            .setNamespace("p", RdfConstants.PREDICATE_NS)
            .setNamespace("c", RdfConstants.CLASS_NS)
            .setNamespace(RDF.NS)
            .setNamespace(RDFS.NS)
            .subject("p:$predicateId")
            .add(RDFS.LABEL, label)
            .add(RDF.TYPE, "c:Predicate")
        return builder.build()
    }
}

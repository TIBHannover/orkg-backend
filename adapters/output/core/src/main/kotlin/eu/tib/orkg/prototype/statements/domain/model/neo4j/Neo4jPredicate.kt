package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdConverter
import eu.tib.orkg.prototype.util.escapeLiterals
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship

@Node(primaryLabel = "Predicate")
class Neo4jPredicate() : Neo4jThing() {
    @Property("label")
    override var label: String? = null

    @Property("predicate_id")
    private var predicateId: PredicateId? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Relationship(type = "RELATED", direction = Relationship.Direction.OUTGOING)
    @JsonIgnore
    var subjectOf: MutableSet<Neo4jStatement> = mutableSetOf()

    constructor(label: String, predicateId: PredicateId, createdBy: ContributorId = ContributorId.createUnknownContributor()) : this() {
        this.label = label
        this.predicateId = predicateId
        this.createdBy = createdBy
    }

    fun toPredicate(): Predicate {
        val pred = Predicate(predicateId, label!!, createdAt!!, createdBy = createdBy)
        pred.rdf = toRdfModel()
        if (subjectOf.isNotEmpty())
            pred.description = subjectOf.firstOrNull { it.predicateId?.value == "description" }?.`object`?.label
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

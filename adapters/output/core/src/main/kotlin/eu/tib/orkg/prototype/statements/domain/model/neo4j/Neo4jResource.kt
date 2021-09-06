package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ObservatoryIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OrganizationIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ResourceIdConverter
import eu.tib.orkg.prototype.util.escapeLiterals
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING
import org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING

@Node(primaryLabel = "Resource")
class Neo4jResource() : Neo4jThing() {

    @Property("label")
    override var label: String? = null

    @Property("resource_id")
    var resourceId: ResourceId? = null

    @Relationship(type = "RELATED", direction = OUTGOING)
    @JsonIgnore
    var resources: MutableSet<Neo4jStatement> = mutableSetOf()

    @Relationship(type = "RELATED", direction = INCOMING)
    @JsonIgnore
    var objectOf = mutableSetOf<Neo4jStatement>()

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("observatory_id")
    var observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory()

    @Property("extraction_method")
    var extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN

    @Property("verified")
    var verified: Boolean? = null

    @Property("organization_id")
    var organizationId: OrganizationId = OrganizationId.createUnknownOrganization()

    /**
     * List of node labels. Labels other than the `Resource` label are mapped to classes.
     */
    @DynamicLabels
    private var labels: MutableList<String> = mutableListOf()

    /**
     * The list of classes that this node belongs to.
     */
    var classes: Set<ClassId>
        get() = labels.map(::ClassId).toSet()
        set(value) {
            labels = value.map { it.value }.toMutableList()
        }

    constructor(
        label: String,
        resourceId: ResourceId,
        createdBy: ContributorId = ContributorId.createUnknownContributor(),
        observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
        extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
        organizationId: OrganizationId = OrganizationId.createUnknownOrganization()
    ) : this() {
        this.label = label
        this.resourceId = resourceId
        this.createdBy = createdBy
        this.observatoryId = observatoryId
        this.extractionMethod = extractionMethod
        this.organizationId = organizationId
    }

    fun toResource(): Resource {
        val resource = Resource(
            resourceId,
            label!!,
            createdAt!!,
            classes,
            objectOf.size,
            createdBy = createdBy,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId
        )
        resource.rdf = toRdfModel()
        return resource
    }

    override val thingId: String?
        get() = resourceId?.value

    override fun toThing() = toResource()

    /**
     * Assign a class to this `Resource` node.
     */
    fun assignTo(clazz: String) = labels.add(clazz)


    fun toNTriple(): String {
        val cPrefix = RdfConstants.CLASS_NS
        val rPrefix = RdfConstants.RESOURCE_NS
        val sb = StringBuilder()
        sb.append("<$rPrefix$resourceId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
        classes.forEach { sb.append("<$rPrefix$resourceId> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
        sb.append("<$rPrefix$resourceId> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string> .")
        return sb.toString()
    }

    fun toRdfModel(): Model {
        var builder = ModelBuilder()
            .setNamespace("r", RdfConstants.RESOURCE_NS)
            .setNamespace("p", RdfConstants.PREDICATE_NS)
            .setNamespace("c", RdfConstants.CLASS_NS)
            .setNamespace(RDF.NS)
            .setNamespace(RDFS.NS)
        builder = builder.subject("r:$resourceId")
            .add(RDFS.LABEL, label)
            .add(RDF.TYPE, "c:Resource")
        classes.forEach { builder = builder.add(RDF.TYPE, "c:${it.value}") }
        resources.forEach {
            builder = if (it.`object` is Neo4jLiteral)
                builder.add("p:${it.predicateId}", "\"${it.`object`!!.label}\"")
            else
                builder.add("p:${it.predicateId}", "r:${it.`object`!!.thingId}")
        }
        return builder.build()
    }
}

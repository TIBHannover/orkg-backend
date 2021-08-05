package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import org.springframework.data.neo4j.core.convert.Neo4jPersistentPropertyConverter

class ClassIdConverter : Neo4jPersistentPropertyConverter<ClassId> {
    override fun read(source: Value): ClassId = ClassId(source.asString())
    override fun write(source: ClassId): Value = Values.value(source.toString())
}

class ContributorIdConverter : Neo4jPersistentPropertyConverter<ContributorId> {
    override fun read(source: Value): ContributorId = ContributorId(source.asString())
    override fun write(source: ContributorId): Value = Values.value(source.toString())
}

class LiteralIdConverter : Neo4jPersistentPropertyConverter<LiteralId> {
    override fun write(source: LiteralId): Value = Values.value(source.toString())
    override fun read(source: Value): LiteralId = LiteralId(source.asString())
}

class ObservatoryIdConverter : Neo4jPersistentPropertyConverter<ObservatoryId> {
    override fun read(source: Value): ObservatoryId = ObservatoryId(source.asString())
    override fun write(source: ObservatoryId): Value = Values.value(source.toString())
}

class OrganizationIdConverter : Neo4jPersistentPropertyConverter<OrganizationId> {
    override fun read(source: Value): OrganizationId = OrganizationId(source.asString())
    override fun write(source: OrganizationId): Value = Values.value(source.toString())
}

class PredicateIdConverter : Neo4jPersistentPropertyConverter<PredicateId> {
    override fun read(source: Value): PredicateId = PredicateId(source.asString())
    override fun write(source: PredicateId): Value = Values.value(source.toString())
}

class ResourceIdConverter : Neo4jPersistentPropertyConverter<ResourceId> {
    override fun read(source: Value): ResourceId = ResourceId(source.asString())
    override fun write(source: ResourceId): Value = Values.value(source.toString())
}

class StatementIdConverter : Neo4jPersistentPropertyConverter<StatementId> {
    override fun read(source: Value): StatementId = StatementId(source.asString())
    override fun write(source: StatementId): Value = Values.value(source.toString())
}

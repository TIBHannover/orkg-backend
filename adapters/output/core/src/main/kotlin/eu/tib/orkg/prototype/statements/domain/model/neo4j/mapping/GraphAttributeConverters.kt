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
import org.springframework.data.neo4j.core.convert.Neo4jPersistentPropertyConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class OffsetDateTimeConverter : Neo4jPersistentPropertyConverter<OffsetDateTime> {
    override fun read(source: Value): OffsetDateTime = OffsetDateTime.parse(source.asString(), ISO_OFFSET_DATE_TIME)
    override fun write(source: OffsetDateTime): Value = Values.value(source.format(ISO_OFFSET_DATE_TIME))
}

class StatementIdConverter : Neo4jPersistentPropertyConverter<StatementId> {
    override fun read(source: Value): StatementId = StatementId(source.asString())
    override fun write(source: StatementId): Value = Values.value(source.toString())
}

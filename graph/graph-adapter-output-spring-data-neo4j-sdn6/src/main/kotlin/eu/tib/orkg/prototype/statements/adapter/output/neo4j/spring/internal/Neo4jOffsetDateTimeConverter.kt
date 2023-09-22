package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.springframework.data.neo4j.core.convert.Neo4jPersistentPropertyConverter

class Neo4jOffsetDateTimeConverter : Neo4jPersistentPropertyConverter<OffsetDateTime> {
    override fun write(source: OffsetDateTime?): Value =
        Values.value(source?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))

    override fun read(source: Value): OffsetDateTime? =
        OffsetDateTime.parse(source.asString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

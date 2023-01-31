package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair

class ClassIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(ClassId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, ClassId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (ClassId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): ClassId? = source?.asString()?.let { ClassId(it) }
}

class ContributorIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(ContributorId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, ContributorId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (ContributorId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): ContributorId? = source?.asString()?.let { ContributorId(it) }
}

class OffsetDateTimeConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(OffsetDateTime::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, OffsetDateTime::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (OffsetDateTime::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source as OffsetDateTime)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: OffsetDateTime?): Value? =
        Values.value(source?.format(ISO_OFFSET_DATE_TIME))

    private fun convertFromNeo4jValue(source: Value?): OffsetDateTime? =
        OffsetDateTime.parse(source?.asString(), ISO_OFFSET_DATE_TIME)
}

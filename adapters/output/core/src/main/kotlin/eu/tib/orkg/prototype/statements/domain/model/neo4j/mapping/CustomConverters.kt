package eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
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

class LiteralIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(LiteralId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, LiteralId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (LiteralId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): LiteralId? = source?.asString()?.let { LiteralId(it) }
}

class ObservatoryIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(ObservatoryId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, ObservatoryId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (ObservatoryId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): ObservatoryId? = source?.asString()?.let { ObservatoryId(it) }
}

class OrganizationIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(OrganizationId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, OrganizationId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (OrganizationId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): OrganizationId? = source?.asString()?.let { OrganizationId(it) }
}

class PredicateIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(PredicateId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, PredicateId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (PredicateId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): PredicateId? = source?.asString()?.let { PredicateId(it) }
}

class ResourceIdConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(ResourceId::class.java, Value::class.java),
        ConvertiblePair(Value::class.java, ResourceId::class.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (ResourceId::class.java.isAssignableFrom(sourceType.type)) {
            convertToNeo4jValue(source)
        } else {
            convertFromNeo4jValue(source as Value)
        }
    }

    private fun convertToNeo4jValue(source: Any?): Value? = Values.value(source?.toString())

    private fun convertFromNeo4jValue(source: Value?): ResourceId? = source?.asString()?.let { ResourceId(it) }
}

package org.orkg.graph.adapter.output.neo4j.internal

import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import kotlin.reflect.KClass

class AttributeConverter<T : Any>(
    private val kClass: KClass<T>,
    private val deserializer: (String) -> T,
    private val serializer: (Any) -> String = Any::toString,
) : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<ConvertiblePair> = mutableSetOf(
        ConvertiblePair(kClass.java, Value::class.java),
        ConvertiblePair(Value::class.java, kClass.java)
    )

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? = if (kClass.java.isAssignableFrom(sourceType.type)) {
        Values.value(source?.let(serializer))
    } else {
        (source as? Value)?.asString()?.let(deserializer)
    }
}

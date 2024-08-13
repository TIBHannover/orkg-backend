package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

class SimCompJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(Thing::class.java, ThingDeserializer())
            addDeserializer(Literal::class.java, LiteralDeserializer())
            addDeserializer(Class::class.java, ClassDeserializer())
            addDeserializer(Resource::class.java, ResourceDeserializer())
            addDeserializer(Predicate::class.java, PredicateDeserializer())
        })
        context?.setMixInAnnotations(GeneralStatement::class.java, StatementMixin::class.java)
        context?.setMixInAnnotations(PublishedContentType::class.java, PublishedContentTypeMixin::class.java)
    }
}

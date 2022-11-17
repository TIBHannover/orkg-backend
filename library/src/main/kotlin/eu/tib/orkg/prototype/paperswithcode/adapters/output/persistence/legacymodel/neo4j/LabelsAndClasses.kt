package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.LabelAndClassService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyLabelsAndClasses : LabelAndClassService {
    override val benchmarkClass: String = BENCHMARK_CLASS
    override val benchmarkPredicate: String = BENCHMARK_PREDICATE
    override val datasetClass: String = DATASET_CLASS
    override val datasetPredicate: String = DATASET_PREDICATE
    override val sourceCodePredicate: String = SOURCE_CODE_PREDICATE
    override val modelClass: String = MODEL_CLASS
    override val modelPredicate: String = MODEL_PREDICATE
    override val quantityClass: String = "" // Illegal, to prevent use. Spring fails if we throw an error here.
    override val quantityPredicate: String = QUANTITY_PREDICATE
    override val metricClass: String = METRIC_CLASS
    override val metricPredicate: String = METRIC_PREDICATE
    override val quantityValueClass: String = "" // Illegal, to prevent use. Spring fails if we throw an error here.
    override val quantityValuePredicate: String = QUANTITY_VALUE_PREDICATE
    override val numericValuePredicate: String = "" // Illegal, to prevent use. Spring fails if we throw an error here.
}

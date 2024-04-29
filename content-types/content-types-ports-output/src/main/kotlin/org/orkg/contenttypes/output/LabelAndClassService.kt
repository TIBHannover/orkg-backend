package org.orkg.contenttypes.output

/**
 * Simple helper class to delegate to different labels and class name, e.g. in the integration tests.
 *
 * In concrete implementations, those should be accessed directly.
 */
interface LabelAndClassService {
     val benchmarkClass: String
     val benchmarkPredicate: String
     val datasetClass: String
     val datasetPredicate: String
     val sourceCodePredicate: String
     val modelClass: String
     val modelPredicate: String
     val quantityClass: String
     val quantityPredicate: String
     val metricClass: String
     val metricPredicate: String
     val quantityValueClass: String
     val quantityValuePredicate: String
     val numericValuePredicate: String
}

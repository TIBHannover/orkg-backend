package org.orkg.contenttypes.domain

import com.fasterxml.jackson.annotation.JsonProperty

interface PaperTotal {
    @get:JsonProperty("total_papers")
    val totalPapers: Int
}

interface CodeTotal {
    @get:JsonProperty("total_codes")
    val totalCodes: Int
}

interface ModelTotal {
    @get:JsonProperty("total_models")
    val totalModels: Int
}

interface DatasetTotal {
    @get:JsonProperty("total_datasets")
    val totalDatasets: Int
}

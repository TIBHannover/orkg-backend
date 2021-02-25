package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeBenchmarkQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import org.springframework.stereotype.Component

@Component
// FIXME: Substitute with real implementation (Neo4j)
class DummyBenchmarkAdapter : SummarizeBenchmarkQuery {
    override fun byResearchProblem(id: ResourceId): Optional<BenchmarkSummary> {
        return when (id.toString()) {
            // For RF1:
            "RP1234" ->
                Optional.of(BenchmarkSummary(ResearchProblem(ResourceId("RP1234"), "Sorting"), 3, 17, 5))
            // For RF100:
            "RP2345" -> Optional.of(
                BenchmarkSummary(
                    ResearchProblem(ResourceId("RP2345"), "Question Answering"),
                    20,
                    10,
                    11
                )
            )
            "RP3456" -> Optional.of(
                BenchmarkSummary(
                    ResearchProblem(ResourceId("RP3456"), "Named Entity Recognition"),
                    1,
                    5,
                    9
                )
            )
            "RP4567" -> Optional.of(
                BenchmarkSummary(
                    ResearchProblem(
                        ResourceId("RP4567"),
                        "Semantic Question Answering"
                    ), 2, 1, 0
                )
            )
            "RP5678" -> Optional.of(
                BenchmarkSummary(
                    ResearchProblem(ResourceId("RP5678"), "Machine Translation"),
                    250,
                    30,
                    140
                )
            )
            "RP6789" -> Optional.of(
                BenchmarkSummary(
                    ResearchProblem(ResourceId("RP6789"), "Relation Extraction"),
                    200,
                    40,
                    86
                )
            )
            else -> Optional.empty()
        }
    }
}

@Component
class DummyDatasetAdapter : FindDatasetsQuery, SummarizeDatasetQuery {
    override fun forResearchProblem(id: ResourceId): Optional<List<Dataset>> =
        when (id.toString()) {
            "RP2345" -> Optional.of(
                listOf(
                    Dataset(ResourceId("DS1"), "SQUAD", 5, 7, 2),
                    Dataset(ResourceId("DS2"), "YahooCQA", 5, 7, 2)
                )
            )
            else -> Optional.empty()
        }

    override fun by(id: ResourceId): Optional<List<DatasetSummary>> {
        return when (id.toString()) {
            "DS1" -> Optional.of(
                listOf(
                    DatasetSummary(
                        "BERT",
                        70,
                        "F-Score",
                        "BERT: Pre-training of Deep Bidirectional Transformers for Language Understanding",
                        "https://github.com/google-research/bert"
                    ),
                    DatasetSummary(
                        "SciBERT",
                        80,
                        "F-Score",
                        "SciBERT: Pretrained Language Model for Scientific Text",
                        "https://github.com/allenai/scibert"
                    )
                )
            )
            else -> Optional.empty()
        }
    }
}

@Component
class DummyResearchProblemAdapter : FindResearchProblemQuery {
    override fun allByResearchField(id: ResourceId): List<ResearchProblem> {
        return when (id.toString()) {
            "RF1" -> listOf(ResearchProblem(ResourceId("RP1234"), "Sorting"))
            "RF100" -> listOf(
                ResearchProblem(ResourceId("RP2345"), "Question Answering"),
                ResearchProblem(ResourceId("RP3456"), "Named Entity Recognition"),
                ResearchProblem(ResourceId("RP4567"), "Semantic Question Answering"),
                ResearchProblem(ResourceId("RP5678"), "Machine Translation"),
                ResearchProblem(ResourceId("RP6789"), "Relation Extraction")
            )
            else -> emptyList()
        }
    }
}

@Component
class DummyResearchFieldAdapter : FindResearchFieldsQuery {
    override fun withBenchmarks(): List<ResearchField> = listOf(
        ResearchField("RF1", "Computer Science"),
        ResearchField("RF100", "Artificial Intelligence")
    )
}

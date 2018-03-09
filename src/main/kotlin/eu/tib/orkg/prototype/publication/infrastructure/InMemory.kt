package eu.tib.orkg.prototype.publication.infrastructure

import eu.tib.orkg.prototype.publication.domain.model.Article
import eu.tib.orkg.prototype.publication.domain.model.ArticleRepository
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.net.URI


@Repository
@Profile("demo")
class InMemoryArticleRepository :
    ArticleRepository {
    override fun findAll(): Collection<Article> {
        return listOf(
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#A_Probabilistic-Logical_Framework_for_Ontology_Matching")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#A_semantic_web_middleware_for_virtual_data_integration_on_the_web")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Accessing_and_Documenting_Relational_Databases_through_OWL_Ontologies")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Adaptive_Integration_of_Distributed_Semantic_Web_Data")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#AgreementMaker:_Ecient_Matching_for_Large_Real-World_Schemas_and_Ontologies")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#ANAPSID:_An_Adaptive_Query_Processing_Engine_for_SPARQL_Endpoints")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Avalanche:_Putting_the_spirit_of_the_web_back_into_semantic_web_querying")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Cross:_an_OWL_wrapper_for_teasoning_on_relational_databases")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#D2RQ_–_Treating_Non-RDF_Databases_as_Virtual_RDF_Graphs")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#DataMaster_–_a_Plug-in_for_Importing_Schemas_and_Data_from_Relational_Databases_into_Protégé")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Discovering_and_Maintaining_Links_on_the_Web_of_Data")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#FedX:_a_federation_layer_for_distributed_query_processing_on_linked_open_data")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#From_Relational_Data_to_RDFS_Models")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#KnoFuss:_A_Comprehensive_Architecture_for_Knowledge_Fusion")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#LIMES_-_A_Time-Efficient_Approach_for_Large-Scale_Link_Discovery_on_the_Web_of_Data")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#LogMap:Logic-based_and_Scalable_Ontology_Matching")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Optimizing_SPARQL_queries_over_disparate_RDF_data_sources_through_distributed_semi-joins")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Querying_Distributed_RDF_Data_Sources_with_SPARQL")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Querying_the_Web_of_Data_with_Graph_Theory-based_Techniques")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Querying_the_web_of_interlinked_datasets_using_void_descriptions")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#RDB2ONT:_A_Tool_for_Generating_OWL_Ontologies_From_Relational_Database_Systems")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Relational.OWL_-_A_Data_and_Schema_Representation_Format_Based_on_OWL")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#SERIMI_-_Resource_Description_Similarity,_RDF_Instance_Matching_and_Interlinking.")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#SLINT:_A_Schema-Independent_Linked_Data_Interlinking_System")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#SPLENDID:_SPARQL_Endpoint_Federation_Exploiting_VOID_Descriptions")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Unveiling_the_hidden_bride:_deep_annotation_for_mapping_and_migrating_legacy_data_to_the_Semantic_Web")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Updating_Relational_Data_via_SPARQL/Update")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Use_of_OWL_and_SWRL_for_Semantic_Relational_Database_Translation")
            ),
            Article(
                uri = URI("http://vocab.cs.uni-bonn.de/unistruct#Zhishi.links_Results_for_OAEI_2011")
            )
        )
    }
}

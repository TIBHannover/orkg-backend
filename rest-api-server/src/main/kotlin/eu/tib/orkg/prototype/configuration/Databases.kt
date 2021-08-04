package eu.tib.orkg.prototype.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.integration.spring.SpringLiquibase
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration {

    // JDBC configuration

    @Bean
    @ConfigurationProperties(prefix = "orkg.datasources.neo4j")
    fun neo4jConfig(): HikariConfig = HikariConfig()

    @Bean
    @ConfigurationProperties(prefix = "orkg.datasources.postgres")
    fun postgresConfig(): HikariConfig = HikariConfig()

    // Data Sources

    @Bean
    fun neo4jDataSource(): DataSource = HikariDataSource(neo4jConfig())

    @Primary // TODO: needed?
    @Bean
    fun postgresDataSource(): DataSource = HikariDataSource(postgresConfig())

    // Liquibase Configuration

    @Bean
    @ConfigurationProperties(prefix = "orkg.datasources.neo4j.liquibase")
    fun neo4jLiquibaseProperties(): LiquibaseProperties = LiquibaseProperties()

    @Bean
    fun neo4jSpringLiquibase() = createSpringLiquibase(neo4jDataSource(), neo4jLiquibaseProperties())

    @Bean
    @ConfigurationProperties(prefix = "orkg.datasources.postgres.liquibase")
    fun postgresLiquibaseProperties(): LiquibaseProperties = LiquibaseProperties()

    @Bean
    fun postgresSpringLiquibase() = createSpringLiquibase(postgresDataSource(), postgresLiquibaseProperties())

    // Helper method

    private fun createSpringLiquibase(datasource: DataSource, properties: LiquibaseProperties) =
        SpringLiquibase().apply {
            dataSource = datasource
            changeLog = properties.changeLog
            contexts = properties.contexts
            defaultSchema = properties.defaultSchema
            isDropFirst = properties.isDropFirst
            setShouldRun(properties.isEnabled)
            labels = properties.labels
            setChangeLogParameters(properties.parameters)
            setRollbackFile(properties.rollbackFile)
        }
}

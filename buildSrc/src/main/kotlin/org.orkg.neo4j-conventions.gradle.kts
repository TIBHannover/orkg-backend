plugins {
    kotlin("jvm")
    id("org.orkg.neo4j-conventions")
}

dependencies {
    implementation("org.springframework.data:spring-data-commons") {
        constraints {
            version {
                strictly("2.7.16")
            }
        }
    }
    implementation("org.springframework.data:spring-data-neo4j") {
        constraints {
            version {
                strictly("6.3.16")
            }
        }
        exclude(group = "org.springframework.data", module = "spring-data-commons")
    }
}

plugins {
    `java-platform`
}

javaPlatform {
    // Allow dependencies to include other BOMs / platforms
    allowDependencies()
}

dependencies {
    // Extend existing platforms
    api(enforcedPlatform(kotlin("bom", "1.8.22")))
    // api(enforcedPlatform(libs.jackson.bom))
    api(platform(libs.forkhandles.bom))
    api(platform(libs.spring.boot.bom)) {
        // We want to use a more recent Kotlin and JUnit version
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.junit.platform")
        exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
        exclude(group = "org.springframework.data", module = "spring-data-commons") // TODO: remove after upgrade to 2.7
        exclude(group = "org.neo4j.driver", module = "neo4j-java-driver") // TODO: remove after upgrade to 2.7
        exclude(group = "org.hibernate", module = "hibernate-core") // TODO: remove after upgrade to 2.7
        exclude(group = "org.slf4j") // TODO: remove after upgrade to 2.7
        // We need a bugfix, so we exclude it here and set a constraint later
        exclude(group = "org.liquibase", module = "liquibase-core")
    }
    api(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
    api(enforcedPlatform(libs.kotlinx.coroutines.bom)) // Required for Kotest. TODO: can be removed after upgrade to Spring Boot 2.7

    // Declare constraints on all components that need alignment (aka. our modules)
    constraints {
        rootProject.subprojects.filterNot { it.name == name }.forEach { subproject ->
            api(subproject)
        }

        /*
        // Security-related adjustments
        implementation("org.apache.httpcomponents:httpclient") {
            version {
                require("4.5.13")
                //strictly("[4.5.13,5.0[")
                because("Vulnerable to CVE-2020-13956")
            }
        }
        testImplementation("junit:junit") {
            version {
                strictly("[4.13.1,5.0[")
                because("Vulnerable to CVE-2020-15250")
            }
        }
        implementation("commons-beanutils:commons-beanutils") {
            exclude(group = "commons-collections", module = "commons-collections")
            version {
                strictly("[1.9.4,2[")
                because("Vulnerable to CVE-2019-10086, CVE-2014-0114")
            }
        }
        implementation("org.apache.commons:commons-collections4") {
            // group is either common-collections or org.apache.commons
            version {
                strictly("[4.3,5.0[")
                because("Vulnerable to Cx78f40514-81ff, CWE-674")
            }
        }
        constraints {
            implementation("org.postgresql:postgresql") {
                version {
                    require("42.2.25")
                }
            }
        }
        */
    }
}

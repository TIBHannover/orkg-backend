plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka")
  id("com.diffplug.spotless")

  id("org.springframework.boot") apply false
}

dependencies {
  api(platform(project(":platform")))

  implementation(platform(kotlin("bom")))
  implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
  implementation(platform("org.testcontainers:testcontainers-bom:1.14.3"))

  implementation(kotlin("stdlib"))

  api("org.springframework:spring-test") {
    because("Provides Spring Test annotations, e.g. @DynamicPropertySource")
  }
  api("org.springframework.boot:spring-boot-test") {
    because("Provides Spring Boot Test annotations, e.g. @SpringBootTest")
  }
  api("org.junit.jupiter:junit-jupiter-api") {
    because("Provides annotations for test life-cycle management, e.g. @TestInstance")
  }
  api("org.testcontainers:junit-jupiter") {
    because("Provides TestContainers annotations, e.g. @TestContainer and @Container")
  }
  api("org.testcontainers:neo4j") { because("Provides Neo4jContainer implementation") }
}

tasks { dokka { configuration { includes = listOf("packages.md") } } }

spotless {
  val ktfmtVersion = "0.21"
  kotlin { ktfmt(ktfmtVersion) }
  kotlinGradle { ktfmt(ktfmtVersion) }
}

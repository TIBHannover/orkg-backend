plugins {
  kotlin("jvm")
  jacoco
  id("com.diffplug.spotless")
  // id("io.gitlab.arturbosch.detekt")
}

repositories { jcenter() }

dependencies {
  api(platform(project(":platform")))

  api("com.michael-bull.kotlin-result:kotlin-result:1.1.11")

  val kotestVersion = "4.4.3"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

tasks.withType<Test> { useJUnitPlatform() }

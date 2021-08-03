plugins  {
    id("org.orkg.kotlin-conventions")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":application:core"))

    implementation(project(":application:shared"))

    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
    `java-library`
    `maven-publish`
}

group = "com.github.ashu-choudhury"
version = "1.2.3"

dependencies {
    api("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")

    implementation("org.jsoup:jsoup:1.18.1")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:3.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "indian-rail-kotlin"
        }
    }
}
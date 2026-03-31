plugins {
    kotlin("jvm")
    application
}

group = "com.github.ashu-choudhury"
version = "1.2.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    
    // Ktor for demonstration if needed, but lib handles its own
}

application {
    mainClass.set("com.github.ashuchoudhury.example.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

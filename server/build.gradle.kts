plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    application
}

group = "com.reas.tracker2"
version = "1.0.0"
application {
    mainClass.set("com.reas.tracker2.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.serialization.json)
    implementation(libs.logging)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.h2)
    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.kotlin.test.junit)
}

tasks.register<Exec>("buildWebApp") {
    inputs.dir(rootProject.relativePath("webApp"))
    outputs.dir(rootProject.relativePath("webApp/dist"))
    workingDir(rootProject.file("server/webApp"))
    commandLine("npx", "vite", "build")
}
tasks.register<Delete>("cleanWebApp") {
    delete(rootProject.file("server/webApp/dist"))
}
tasks.named("processResources") {
    dependsOn("buildWebApp")
}
tasks.named("clean") {
    dependsOn("cleanWebApp")
}
plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.xerial:sqlite-jdbc:3.45.3.0")

    implementation("org.slf4j:slf4j-simple:2.0.12")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("Main")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform()
}

tasks.run.get().jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
tasks.run.get().standardInput = System.`in`

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "Main",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}


plugins {
    `java-library`
    `maven-publish`
}

group = "io.kerosene.contracts"
version = "0.3.0"
description = "Kerosene shared contracts for KFE/Core boundaries"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

sourceSets {
    test {
        resources {
            srcDir("test-vectors")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:deprecation"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    options {
        (this as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            charSet = "UTF-8"
            addStringOption("Xdoclint:none", "-quiet")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("java") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "localBuild"
            url = layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
        maven {
            name = "GitHubPackages"
            url = uri(
                providers.environmentVariable("CONTRACTS_MAVEN_URL")
                    .orElse("https://maven.pkg.github.com/Daniel-Astrofer/contracts")
                    .get()
            )
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}

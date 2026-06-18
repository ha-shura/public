import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "10.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.jooq:jooq-meta-extensions:3.19.32")
    jooqGenerator("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
jooq {
    version.set("3.19.32")

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:mem:jooq-codegen;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
                    user = "sa"
                    password = ""
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        inputSchema = "PUBLIC"
                        excludes = "flyway_schema_history"
                        properties.add(org.jooq.meta.jaxb.Property().apply {
                            key = "scripts"
                            value = "src/main/resources/db/migration/V1__create_tables.sql"
                        })
                        properties.add(org.jooq.meta.jaxb.Property().apply {
                            key = "sort"
                            value = "semantic"
                        })
                        properties.add(org.jooq.meta.jaxb.Property().apply {
                            key = "defaultNameCase"
                            value = "lower"
                        })
                    }

                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = false
                        isFluentSetters = true
                    }

                    target.apply {
                        packageName = "com.example.bookmanagement.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("build/generated-src/jooq/main")
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


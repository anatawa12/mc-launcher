plugins {
    java
    kotlin("jvm") version "1.3.60"
}

group = "com.anatawa12"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.3")

    testCompile("junit", "junit", "4.12")
    testImplementation("io.mockk:mockk:1.9.3")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

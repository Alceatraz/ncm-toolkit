rootProject.name = "ncm-toolkit-uni"

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage") repositories {
    mavenCentral()
  }
}

pluginManagement {
  plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.graalvm.buildtools.native") version "0.11.4"
  }
}

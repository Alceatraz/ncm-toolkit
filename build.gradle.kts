val enableStatic: String by project
val isWindows = System.getProperty("os.name").lowercase().contains("win")

group = "top.btswork"
version = "1.0.0-SNAPSHOT"

plugins {
  application
  id("org.jetbrains.kotlin.jvm")
  id("org.graalvm.buildtools.native")
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(25)
}

application {
  mainClass.set("top.btswork.ncmtoolkit.MainKt")
}

tasks.test {
  useJUnitPlatform()
}

graalvmNative {
  binaries {
    named("main") {
      imageName.set("ncm-toolkit")
      if (isWindows.not() && enableStatic.toBoolean()) {
        buildArgs.add("--static")
        buildArgs.add("--libc=musl")
      }
      buildArgs.add("-march=x86-64-v3")
      buildArgs.add("-H:+ReportExceptionStackTraces")
      buildArgs.add("--enable-url-protocols=http,https")
    }
  }
}

tasks.register<Jar>("fatJar") {
  archiveClassifier.set("all")
  from(sourceSets.main.get().output)
  dependsOn(configurations.runtimeClasspath)
  from({
         configurations.runtimeClasspath.get()
           .filter {
             it.name.endsWith("jar")
           }.map {
             zipTree(it)
           }
       })
  manifest {
    attributes["Main-Class"] = "top.btswrok.ncmtoolkit.MainKt"
  }
}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: build.gradle.kts
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

import java.io.FileInputStream
import java.util.regex.Pattern;
import java.util.*

plugins {
	java
	`java-library`
	eclipse
	distribution
}

repositories {
	mavenCentral()
    maven {
        url = uri("https://datura.econoffice.ch/maven2")
    }
}

var env = Properties()
env.load(FileInputStream(File(project.rootDir, "build.properties")))

var flavour = currentFlavour()
var flavourVersion = currentFlavourVersion()
var flavourJavaVersion : JavaVersion = project.extra["${flavour}JavaVersion"] as JavaVersion

eclipse {
	project {
    	name = "openMDX ${flavourVersion} ~ Test Core"
    }
    jdt {
		sourceCompatibility = flavourJavaVersion
    	targetCompatibility = flavourJavaVersion
    	javaRuntimeName = "JavaSE-$flavourJavaVersion"
    }
}

fun currentFlavour(): String {
    val taskRequestsStr = gradle.startParameter.taskRequests.toString()
    val pattern = Pattern.compile("\\w*([Oo]penmdx[2-4])\\w*")
    val matcher = pattern.matcher(taskRequestsStr)
    val flavour = if (matcher.find()) {
        matcher.group(1).lowercase()
    } else {
        "main"
    }
    return flavour
}

fun currentFlavourVersion(): String {
    val taskRequestsStr = gradle.startParameter.taskRequests.toString()
    val pattern = Pattern.compile("\\w*[Oo]penmdx([2-4])\\w*")
    val matcher = pattern.matcher(taskRequestsStr)
    val flavourVersion = if (matcher.find()) {
        matcher.group(1)
    } else {
        "2"
    }
    return flavourVersion
}

fun getProjectImplementationVersion(): String {
	return "${flavourVersion}.${project.version}";
}

fun getDeliverDir(): File {
	return File(project.rootDir, "${flavour}/${project.name}");
}

fun touch(file: File) {
	ant.withGroovyBuilder { "touch"("file" to file, "mkdirs" to true) }
}

project.configurations.maybeCreate("openmdxBootstrap")
val openmdxBootstrap by configurations

dependencies {
    // main
    implementation(project(":core"))
    implementation(libs.javax.javaee.api)
    implementation(libs.javax.jdo.api)
    implementation(libs.javax.cache.api)
    implementation(libs.junit.jupiter.api)
    // test
    testImplementation(project(":core"))
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
 	testRuntimeOnly(libs.postgresql)
	testRuntimeOnly(libs.javax.servlet.api)
	testRuntimeOnly(libs.atomikos.transaction.jta)
	testRuntimeOnly(libs.atomikos.transaction.jdbc)
    // openmdxBootstrap
    openmdxBootstrap(project(":core"))
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir(layout.buildDirectory.dir("generated/sources/main/java"))
        }
        resources {
        	srcDir("src/main/resources")
            srcDir(layout.buildDirectory.dir("generated/resources/main"))
        }
    }
    test {
        java {
            srcDir("src/test/java")
            srcDir(layout.buildDirectory.dir("generated/sources/test/java"))
        }
        resources {
        	srcDir("src/test/resources")
        }
    }
    create("openmdxDatatype") {
    	java {
        	srcDir("src/model/java")
    	}
    }
}

tasks.named<AbstractCompile>("compileOpenmdxDatatypeJava") {
    classpath = configurations["openmdxBootstrap"]
}

tasks.withType<Test> {
    this.classpath.forEach { println(it) }
}
tasks.test {
    useJUnitPlatform()
    maxHeapSize = "4G"
}

project.tasks.named("processResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
project.tasks.named("processTestResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<org.openmdx.gradle.GenerateModelsTask>("generate-model") {
	dependsOn("openmdxDatatypeClasses")
    inputs.dir("$projectDir/src/model/emf")
    inputs.dir("$projectDir/src/main/resources")
    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
    classpath(configurations["openmdxBootstrap"])
    classpath(sourceSets["openmdxDatatype"].runtimeClasspath)
	args = listOf(
		"--pathMapSymbol=openMDX 2 ~ Core (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "core/src/model/emf") + "/",
		"--pathMapSymbol=openMDX 2 ~ Security (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "security/src/model/emf") + "/",
		"--pathMapSymbol=openMDX 2 ~ Portal (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "portal/src/model/emf") + "/",
		"--url=file:src/model/emf/models.uml",
		"--xmi=emf",
		"--out=" + File(project.getBuildDir(), "generated/sources/model/openmdx-${project.name}-models.zip"),
		"--openmdxjdo=" + File(project.projectDir, "src/main/resources"),
		"--dataproviderVersion=2",
		"--format=xmi1",
	    "--format=test.openmdx.application.mof.mapping.java.PrimitiveTypeMapper_1(cci2)",
	    "--format=test.openmdx.application.mof.mapping.java.PrimitiveTypeMapper_1(jmi1)",
	    "--format=test.openmdx.application.mof.mapping.java.PrimitiveTypeMapper_1(jpa3)",
		"--format=mof1",            
		"%"
	)
    doFirst {
    }
    doLast {
        copy {
            from(
                zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
            )
            into(layout.buildDirectory.dir("generated/sources/main/java"))
            include(
                "**/*.java"
            )
        }
        copy {
            from(
                zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
            )
            into(layout.buildDirectory.dir("generated/resources/main"))
        }
    }
}

tasks.compileJava {
    dependsOn("generate-model")
//	    options.release.set(Integer.valueOf(flavourJavaVersion.majorVersion))
}


tasks {
	assemble {
		dependsOn(
        )
	}
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-${project.name}-jre-" + flavourJavaVersion)
        contents {
        	// test-core
        	from(".") { into(project.name); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into(project.name + "/src") }
            // etc
            from("etc") { into(project.name + "/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-...
            var path = "openmdx-$flavourJavaVersion/${project.name}/lib"
            from("../$path") { into(path) }
            path = "openmdx-$flavourJavaVersion/gradle/repo"
            from("../$path") { into(path) }
        }
    }
}

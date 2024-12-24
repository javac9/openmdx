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
import java.util.*

plugins {
	java
    id("systems.manifold.manifold-gradle-plugin") version "0.0.2-alpha"
}

allprojects {
    group = "org.openmdx"
    version = "20.0"
    ext {
        extra["mainGroup"] = "org.openmdx"
        extra["mainProjectImplementationVersion"] = "2.${version}"
        extra["mainJavaLanguageVersion"] = JavaLanguageVersion.of(8)
		extra["mainJavaVersion"] = JavaVersion.VERSION_1_8
        extra["openmdx2Group"] = "org.openmdx"
        extra["openmdx2ProjectImplementationVersion"] = "2.${version}"
        extra["openmdx2JavaLanguageVersion"] = JavaLanguageVersion.of(8)
		extra["openmdx2JavaVersion"] = JavaVersion.VERSION_1_8
        extra["openmdx3Group"] = "org.openmdx.v3"
        extra["openmdx3ProjectImplementationVersion"] = "3.${version}"
        extra["openmdx2JavaLanguageVersion"] = JavaLanguageVersion.of(8)
		extra["openmdx3JavaVersion"] = JavaVersion.VERSION_1_8
        extra["openmdx4Group"] = "org.openmdx.v4"
        extra["openmdx4ProjectImplementationVersion"] = "4.${version}"
        extra["openmdx4JavaLanguageVersion"] = JavaLanguageVersion.of(21)
		extra["openmdx4JavaVersion"] = JavaVersion.VERSION_21
    }
}

tasks.clean {
    doLast {
        getPlatformDir().deleteRecursively();
    }
}

//var env = Properties()
//env.load(FileInputStream(File(project.rootDir, "build.properties")))
//val targetPlatform = JavaVersion.valueOf(env.getProperty("target.platform"))

fun getPlatformDir(): File {
	return File(project.rootDir, "openmdx3");
}

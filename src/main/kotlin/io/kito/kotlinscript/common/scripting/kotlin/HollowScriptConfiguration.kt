/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.kito.kotlinscript.common.scripting.kotlin

import cpw.mods.modlauncher.TransformingClassLoader
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import io.kito.kotlinscript.KotlinScriptForForge
import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.classpathFromClassloader

class HollowScriptConfiguration : AbstractHollowScriptConfiguration({})

@KotlinScript(
    "HollowScript", "ks.kts", compilationConfiguration = HollowScriptConfiguration::class
)
abstract class HollowScript

class AbstractHollowScriptHost : ScriptingHostConfiguration({
    getScriptingClass(JvmGetScriptingClass())
    classpathFromClassloader(TransformingClassLoader.getSystemClassLoader())
})

abstract class AbstractHollowScriptConfiguration(body: Builder.() -> Unit) : ScriptCompilationConfiguration({
    body()

    jvm {
        compilerOptions(
            "-opt-in=kotlin.time.ExperimentalTime,kotlin.ExperimentalStdlibApi",
            "-jvm-target=17",
            "-Xadd-modules=ALL-MODULE-PATH" //Loading kotlin from shadowed jar
        )

        //Скорее всего в этом случае этот класс был загружен через IDE, поэтому получить моды и classpath автоматически нельзя
        if (!FMLLoader.isProduction() && FMLLoader.launcherHandlerName() == null) {
            dependenciesFromCurrentContext(wholeClasspath = true)
            return@jvm
        }

        val stdLib = ModList.get().getModFileById(KotlinScriptForForge.MODID).file.filePath.toFile().absolutePath
        System.setProperty("kotlin.java.stdlib.jar", stdLib)

        val files = HashSet<File>()

        files.addAll(ModList.get().mods.map { File(it.owningFile.file.filePath.absolutePathString()) })
        FMLLoader.getGamePath().resolve("mods").toFile().listFiles()?.forEach(files::add)
        files.addAll(
            FMLLoader.getLaunchHandler().minecraftPaths.otherModPaths.flatten().map { File(it.absolutePathString()) })
        files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.otherArtifacts.map { File(it.absolutePathString()) })
        files.addAll(FMLLoader.getLaunchHandler().minecraftPaths.minecraftPaths.map { File(it.absolutePathString()) })

        if (FMLLoader.getDist().isDedicatedServer) {
            listFilesRecursively(FMLLoader.getGamePath().resolve("libraries").toFile(), files)
        }

        dependenciesFromClassContext(HollowScriptConfiguration::class, wholeClasspath = true)

        files.removeIf { it.isDirectory }
        updateClasspath(files.distinct().sortedBy { it.absolutePath }.onEach { KotlinScriptForForge.LOGGER.info(it.absolutePath) })
    }

    defaultImports(
        Import::class
    )

    refineConfiguration {
        onAnnotations(Import::class, handler = HollowScriptConfigurator())
    }

    ide { acceptedLocations(ScriptAcceptedLocation.Everywhere) }
})

private fun listFilesRecursively(directory: File, fileList: HashSet<File>) {
    directory.listFiles()?.forEach {
        if (it.isDirectory) listFilesRecursively(it, fileList)
        else fileList.add(it)
    }
}

class HollowScriptConfigurator : RefineScriptCompilationConfigurationHandler {
    override operator fun invoke(context: ScriptConfigurationRefinementContext) = processAnnotations(context)

    private fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val annotations = context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile

        val importedSources = annotations.flatMap {
            (it as? Import)?.paths?.map { sourceName ->
                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            } ?: emptyList()
        }

        return ScriptCompilationConfiguration(context.compilationConfiguration) {
            if (importedSources.isNotEmpty()) importScripts.append(importedSources)
        }.asSuccess()
    }
}
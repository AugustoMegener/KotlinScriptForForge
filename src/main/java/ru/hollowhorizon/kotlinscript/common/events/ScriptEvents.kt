package ru.hollowhorizon.kotlinscript.common.events

import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event
import java.io.File

open class ScriptEvent(val file: File) : Event()

@Cancelable
class ScriptErrorEvent(file: File, val type: ErrorType, val error: List<ScriptError>) : ScriptEvent(file)

class ScriptCompiledEvent(file: File) : ScriptEvent(file)

class ScriptError(
    val severity: Severity,
    val message: String,
    val source: String,
    val line: Int,
    val column: Int,
    val exception: Throwable?
)

enum class Severity {
    DEBUG, INFO, WARNING, ERROR, FATAL
}

enum class ErrorType {
    COMPILATION_ERROR, RUNTIME_ERROR
}
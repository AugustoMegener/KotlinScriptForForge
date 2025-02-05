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

package io.kito.kotlinscript.common.events

import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event
import java.io.File

open class ScriptEvent(val file: File) : Event()

@Cancelable
class ScriptErrorEvent(file: File, val type: ErrorType, val error: List<ScriptError>) : ScriptEvent(file)

class ScriptCompiledEvent(file: File) : ScriptEvent(file)
class ScriptStartedEvent(file: File) : ScriptEvent(file)

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
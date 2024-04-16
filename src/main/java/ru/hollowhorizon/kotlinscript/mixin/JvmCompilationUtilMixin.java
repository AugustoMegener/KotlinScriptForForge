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

package ru.hollowhorizon.kotlinscript.mixin;

import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.kotlin.backend.common.output.OutputFile;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.JvmCompilationUtilKt;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.kotlinscript.common.scripting.mappings.ASMRemapper;

@Mixin(value = JvmCompilationUtilKt.class, remap = false)
public class JvmCompilationUtilMixin {

    @Inject(method = "makeCompiledModule", at = @At("HEAD"))
    private static void makeCacheForClassLoading(GenerationState generationState, CallbackInfoReturnable<KJvmCompiledModuleInMemoryImpl> cir) {
        if (!FMLEnvironment.production) return;
        generationState.getFactory().asList().forEach(file -> ASMRemapper.CLASS_CACHE.put(file.getRelativePath(), file.asByteArray()));
    }

    @Inject(method = "makeCompiledModule", at = @At("TAIL"))
    private static void clearCacheForClassLoading(GenerationState generationState, CallbackInfoReturnable<KJvmCompiledModuleInMemoryImpl> cir) {
        if (!FMLEnvironment.production) return;
        ASMRemapper.CLASS_CACHE.clear();
    }

    @Redirect(method = "makeCompiledModule", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/backend/common/output/OutputFile;asByteArray()[B"))
    private static byte[] makeCompiledModule(OutputFile instance) {
        if (!instance.getRelativePath().endsWith(".class") || !FMLEnvironment.production) return instance.asByteArray();
        return ASMRemapper.INSTANCE.remap(instance.asByteArray());
    }
}

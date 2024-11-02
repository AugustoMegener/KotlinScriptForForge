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

package io.kito.kotlinscript.mixin;

import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.kotlin.load.java.structure.impl.classFiles.BinaryJavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import io.kito.kotlinscript.common.scripting.mappings.HollowMappings;

@Mixin(value = BinaryJavaClass.class, remap = false)
public abstract class BinaryJavaClassMixin {

    @ModifyArg(method = "visitMethod", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryJavaMethodBase$Companion;create(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lorg/jetbrains/kotlin/load/java/structure/JavaClass;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/ClassifierResolutionContext;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryClassSignatureParser;)Lkotlin/Pair;"), index = 0)
    private String onMethodCreating(String name) {
        if (!FMLEnvironment.production) return name;
        return name.startsWith("m_") ? HollowMappings.MAPPINGS.methodDeobf(name) : name;
    }

    @ModifyArg(method = "visitField", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/name/Name;identifier(Ljava/lang/String;)Lorg/jetbrains/kotlin/name/Name;"), index = 0)
    private String onFieldCreating(String name) {
        if (!FMLEnvironment.production) return name;
        return name.startsWith("f_") ? HollowMappings.MAPPINGS.fieldDeobf(name) : name;
    }
}

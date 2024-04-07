package ru.hollowhorizon.kotlinscript.mixin;

import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerImplsKt;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerIsolated;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ScriptJvmCompilerImplsKt.class)
public class ScriptingJvmCompilerIsolatedMixin {
    //@Inject()
    private void onCompile() {

    }
}

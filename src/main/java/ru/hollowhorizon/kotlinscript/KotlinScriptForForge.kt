package ru.hollowhorizon.kotlinscript

import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


@Mod(KotlinScriptForForge.MODID)
object KotlinScriptForForge {
    val LOGGER: Logger = LogManager.getLogger()
    const val MODID: String = "kotlinscript"
}

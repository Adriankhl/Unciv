package com.unciv.app.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.HdpiMode
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.unciv.UncivGame
import com.unciv.models.translations.tr
import java.io.File
import kotlin.concurrent.thread
import kotlin.system.exitProcess


internal object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {

        packImages()

        val config = Lwjgl3ApplicationConfiguration()
        // Don't activate GL 3.0 because it causes problems for MacOS computers
        config.setWindowIcon(Files.FileType.Internal, "ExtraImages/Icon.png")
        config.setTitle("Unciv")
        config.setHdpiMode(HdpiMode.Logical)

        val versionFromJar = DesktopLauncher.javaClass.`package`.specificationVersion

        val game = UncivGame(if (versionFromJar != null) versionFromJar else "Desktop", null){exitProcess(0)}

        Lwjgl3Application(game, config)
    }

    private fun packImages() {
        val startTime = System.currentTimeMillis()

        val settings = TexturePacker.Settings()
        // Apparently some chipsets, like NVIDIA Tegra 3 graphics chipset (used in Asus TF700T tablet),
        // don't support non-power-of-two texture sizes - kudos @yuroller!
        // https://github.com/yairm210/UnCiv/issues/1340
        settings.maxWidth = 2048
        settings.maxHeight = 2048
        settings.combineSubdirectories = true
        settings.pot = true
        settings.fast = true

        // This is so they don't look all pixelated
        settings.filterMag = Texture.TextureFilter.MipMapLinearLinear
        settings.filterMin = Texture.TextureFilter.MipMapLinearLinear

        if (File("../Images").exists()) // So we don't run this from within a fat JAR
            TexturePacker.process(settings, "../Images", ".", "game")

        // pack for mods as well
        val modDirectory = File("mods")
        if(modDirectory.exists()) {
            for (mod in modDirectory.listFiles()!!){
                if (!mod.isHidden && File(mod.path + "/Images").exists())
                    TexturePacker.process(settings, mod.path + "/Images", mod.path, "game")
            }
        }

        val texturePackingTime = System.currentTimeMillis() - startTime
        println("Packing textures - "+texturePackingTime+"ms")
    }
}

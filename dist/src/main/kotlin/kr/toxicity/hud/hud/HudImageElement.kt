package kr.toxicity.hud.hud

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.manager.ImageManager
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class HudImageElement(parent: HudImpl, private val image: ImageLayout, gui: GuiLocation, pixel: PixelLocation) {

    private val chars = run {
        val hud = image.image
        val finalPixel = image.location + pixel

        val shader = HudShader(
            gui,
            image.renderScale,
            image.layer,
            image.outline,
            finalPixel.opacity,
            image.property
        )

        val list = ArrayList<PixelComponent>()
        if (hud.listener != null) {
            list.add(EMPTY_PIXEL_COMPONENT)
        }
        val negativeSpace = parent.getOrCreateSpace(-1)
        hud.image.forEach { pair ->
            val fileName = "$NAME_SPACE_ENCODED:${pair.name}"
            val height = (pair.image.image.height.toDouble() * image.scale).roundToInt()
            val scale = height.toDouble() / pair.image.image.height
            val ascent = finalPixel.y.coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT)
            val shaderGroup = ShaderGroup(shader, fileName, image.scale, ascent)

            val component = ImageManager.getImage(shaderGroup) ?: run {
                val c = (++parent.imageChar).parseChar()
                val comp = Component.text()
                    .font(parent.imageKey)
                val finalWidth = WidthComponent(
                    if (BOOTSTRAP.useLegacyFont()) comp.content(c).append(NEGATIVE_ONE_SPACE_COMPONENT.component) else comp.content("$c$negativeSpace"),
                    (pair.image.image.width.toDouble() * scale).roundToInt()
                )
                parent.jsonArray?.let { array ->
                    HudImpl.createBit(shader, ascent) { y ->
                        array.add(jsonObjectOf(
                            "type" to "bitmap",
                            "file" to fileName,
                            "ascent" to y,
                            "height" to height,
                            "chars" to jsonArrayOf(c)
                        ))
                    }
                }
                ImageManager.setImage(shaderGroup, finalWidth)
                finalWidth
            }

            list.add(component.toPixelComponent(finalPixel.x + (pair.image.xOffset * scale).roundToInt()))
        }
        val renderer = ImageRenderer(
            hud,
            image.color,
            image.space,
            image.stack,
            image.maxStack,
            list,
            image.follow,
            image.cancelIfFollowerNotExists,
            image.conditions.and(image.image.conditions)
        )
        renderer.max() to renderer.getComponent(UpdateEvent.EMPTY)
    }

    val max = chars.first

    fun getComponent(hudPlayer: HudPlayer): PixelComponent = chars.second(hudPlayer, (hudPlayer.tick % Int.MAX_VALUE).toInt())

}
package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import java.io.File
import kotlin.math.ceil
import kotlin.math.round

class HudTextElement(name: String, file: File, private val text: TextLayout, index: Int, x: Double, y: Double, animation: List<ImageLocation>) {
    companion object {
        private val spaceComponent = 4.toSpaceComponent()
    }

    private val key = animation.mapIndexed { index2, imageLocation ->
        imageLocation.x.toSpaceComponent() to Style.style(text.color).font(Key.key("$NAME_SPACE:hud/$name/text/text_${index + 1}_${index2 + 1}"))
    }
    private val xComponent = text.location.x.toSpaceComponent()
    private val sComponent = text.space.toSpaceComponent()

    init {
        val shader = HudShader(
            GuiLocation(x, y),
            text.layer + index,
            text.outline
        )

        animation.forEachIndexed { index2, imageLocation ->
            val array = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type", "space")
                    add("advances", JsonObject().apply {
                        addProperty(" ", 4)
                    })
                })
            }
            text.text.array.forEach {
                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:text/${text.text.fontName}/${it.file}")
                    addProperty("ascent", Hud.createBit((text.location.y + imageLocation.y).coerceAtLeast(-Hud.ADD_HEIGHT).coerceAtMost(Hud.ADD_HEIGHT), shader))
                    addProperty("height", round(text.text.height * text.scale).toInt())
                    add("chars", it.chars)
                })
            }
            JsonObject().apply {
                add("providers", array)
            }.save(File(file, "text_${index + 1}_${index2 + 1}.json"))
        }
    }

    fun getText(player: HudPlayer): WidthComponent {
        val getKey = key[(player.tick % key.size).toInt()]
        var comp = EMPTY_WIDTH_COMPONENT
        val original = text.getText(player)
        if (original == "") return comp
        original.forEachIndexed { index, char ->
            if (char == ' ') {
                comp += spaceComponent
            } else {
                text.text.charWidth[char]?.let { width ->
                    comp += WidthComponent(Component.text(char).style(getKey.second), ceil(width.toDouble() * text.scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                }
            }
            if (index < original.lastIndex) comp += sComponent
        }
        when (text.align) {
            TextLayout.Align.LEFT -> {
                comp = (comp.width.toDouble() / 2).toInt().toSpaceComponent() + comp
            }
            TextLayout.Align.RIGHT -> {
                comp = (-comp.width / 2).toSpaceComponent() + comp
            }
            else -> {}
        }
        return xComponent + getKey.first + comp
    }
}
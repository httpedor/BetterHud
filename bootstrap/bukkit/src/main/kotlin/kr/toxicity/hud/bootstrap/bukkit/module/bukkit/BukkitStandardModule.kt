package kr.toxicity.hud.bootstrap.bukkit.module.bukkit

import kr.toxicity.hud.api.bukkit.event.CustomPopupEvent
import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.module.BukkitModule
import kr.toxicity.hud.bootstrap.bukkit.util.*
import kr.toxicity.hud.util.ifNull
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attribute
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffectType
import java.util.function.Function

class BukkitStandardModule: BukkitModule {
    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
        get() = mapOf(
            "custom" to {
                val n = it.get("name")?.asString().ifNull("name value not set.")
                createBukkitTrigger(CustomPopupEvent::class.java, { e ->
                    if (e.name == n) e.player.uniqueId else null
                })
            },
            "death" to {
                createBukkitTrigger(PlayerDeathEvent::class.java, {
                    it.entity.uniqueId
                })
            }
        )
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "health" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.health / p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
                    }
                }
            },
            "food" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.foodLevel / 20.0
                    }
                }
            },
            "armor" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.armor / 20.0
                    }
                }
            },
            "air" to { _ ->
                {
                    HudListener { p ->
                        (p.bukkitPlayer.remainingAir.toDouble() / p.bukkitPlayer.maximumAir).coerceAtLeast(0.0)
                    }
                }
            },
            "exp" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.exp.toDouble()
                    }
                }
            },
            "absorption" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.absorptionAmount / p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
                    }
                }
            },
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "empty_space" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.emptySpace
                }
            },
            "health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.health
                }
            },
            "food" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.foodLevel
                }
            },
            "armor" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.armor
                }
            },
            "air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.remainingAir
                }
            },
            "max_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
                }
            },
            "max_health_with_absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value + p.bukkitPlayer.absorptionAmount
                }
            },
            "health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.health / p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * 100.0
                }
            },
            "max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.maximumAir
                }
            },
            "level" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.level
                }
            },
            "hotbar_slot" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.inventory.heldItemSlot
                }
            },
            "number" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        p.variableMap[args[0]]?.toDoubleOrNull() ?: 0.0
                    }
                }
            },
            "potion_effect_duration" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    val potion = (runCatching {
                        NamespacedKey.fromString(args[0])?.let { key ->
                            Registry.EFFECT.get(key)
                        }
                    }.onFailure {
                        @Suppress("DEPRECATION")
                        PotionEffectType.getByName(args[0])
                    }.getOrNull() ?: throw RuntimeException("this potion effect doesn't exist: ${args[0]}"))
                    return Function { p ->
                        p.bukkitPlayer.getPotionEffect(potion)?.duration ?: 0
                    }
                }
            },
            "total_amount" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    val item = Material.valueOf(args[0].uppercase())
                    return Function { p ->
                        p.bukkitPlayer.totalAmount(item)
                    }
                }
            },
            "storage" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    val item = Material.valueOf(args[0].uppercase())
                    return Function { p ->
                        p.bukkitPlayer.storage(item)
                    }
                }
            },
            "air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.remainingAir
                }
            },
            "absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.absorptionAmount
                }
            },
            "max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.maximumAir
                }
            },
            "food" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.foodLevel
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "name" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.name
                }
            },
            "gamemode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.gameMode.name
                }
            },
            "string" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    return Function { p ->
                        p.variableMap[args[0]] ?: "<none>"
                    }
                }
            },
            "custom_variable" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val key = args[0]
                    return reason.unwrap { e: CustomPopupEvent ->
                        Function { _ ->
                            e.variables[key] ?: "<none>"
                        }
                    }
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "dead" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.isDead
                }
            }
        )
}
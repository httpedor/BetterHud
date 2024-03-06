package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PopupManager
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.popup.PopupImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.forEachAllYaml
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.warn

object PopupManagerImpl: MythicHudManager, PopupManager {
    private val popupMap = HashMap<String, PopupImpl>()
    override fun start() {

    }

    override fun reload(resource: GlobalResource) {
        popupMap.clear()
        val save = resource.font.subFolder("popup")
        DATA_FOLDER.subFolder("popups").forEachAllYaml { file, s, configurationSection ->
            runCatching {
                popupMap[s] = PopupImpl(save, s, configurationSection)
            }.onFailure { e ->
                warn("Unable to load this popup: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
    }

    override fun getPopup(name: String): Popup? = popupMap[name]
    override fun end() {
    }
}
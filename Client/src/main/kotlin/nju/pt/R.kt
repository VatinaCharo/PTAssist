package nju.pt

import nju.pt.client.Config
import nju.pt.client.RoundType
import nju.pt.client.RuleType
import nju.pt.client.WorkMode

object R {
    const val VERSION = "1.0.0"

    val DEFAULT_CSS_PATH: String = R::class.java.getResource("assets/Element.css")!!.toExternalForm()
    val SPECIAL_CSS_PATH: String = R::class.java.getResource("assets/Special.css")!!.toExternalForm()
    val MAIN_IMAGE_PATH: String = R::class.java.getResource("assets/senta_tell.jpg")!!.toExternalForm()
    val LOGO_PATH: String = R::class.java.getResource("assets/logo.png")!!.toExternalForm()
    val TEXT_LOGO_PATH: String = R::class.java.getResource("assets/text_logo.png")!!.toExternalForm()

    const val SETTING_JSON_PATH = "config/setting.json"
    const val DATA_JSON_PATH = "data/data.json"
    const val CACHE_JSON_PATH = "data/cache.json"

    val DEFAULT_CONFIG = Config("127.0.0.1", 7890, 0, 1, 5, RoundType.NORMAL, RuleType.CUPT, WorkMode.OFFLINE)
    const val NJU_LINK = "https://www.nju.edu.cn"
    const val AUTHOR1_LINK = "https://github.com/VatinaCharo"
    const val AUTHOR2_LINK = "https://github.com/EnjoyXu"
}
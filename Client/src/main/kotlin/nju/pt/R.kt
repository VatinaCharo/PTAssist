package nju.pt

import nju.pt.client.Config

object R {
    val DEFAULT_CSS_PATH: String = R::class.java.getResource("assets/Element.css")!!.toExternalForm()
    val SPECIAL_CSS_PATH: String = R::class.java.getResource("assets/Special.css")!!.toExternalForm()
    val MAIN_IMAGE_PATH: String = R::class.java.getResource("assets/senta_tell.jpg")!!.toExternalForm()
    val LOGO_PATH: String = R::class.java.getResource("assets/logo.png")!!.toExternalForm()
    val TEXT_LOGO_PATH: String = R::class.java.getResource("assets/text_logo.png")!!.toExternalForm()

    const val SETTING_JSON_PATH = "setting.json"

    val DEFAULT_CONFIG = Config("127.0.0.1", 7890, 5)
}
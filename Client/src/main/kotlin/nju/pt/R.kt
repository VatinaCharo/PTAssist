package nju.pt

object R {
    val DEFAULT_CSS_PATH = R::class.java.getResource("assets/Element.css")?.toExternalForm()
    val SPECIAL_CSS_PATH = R::class.java.getResource("assets/Special.css")?.toExternalForm()
    val MAIN_IMAGE_PATH = R::class.java.getResource("assets/senta_tell.jpg")?.toExternalForm()
    val LOGO_PATH = R::class.java.getResource("assets/logo.png")?.toExternalForm()
    val TEXT_LOGO_PATH = R::class.java.getResource("assets/text_logo.png")?.toExternalForm()
}
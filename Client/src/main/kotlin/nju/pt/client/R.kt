package nju.pt.client

object R {
    const val LABEL_FONT_SIZE = 15.0
    val DEFAULT_CSS_PATH = R::class.java.getResource("Element.css")?.toExternalForm()
    val SPECIAL_CSS_PATH = R::class.java.getResource("Special.css")?.toExternalForm()
    val MAIN_IMAGE_PATH = R::class.java.getResource("senta_tell.jpg")?.toExternalForm()
    val LOGO_PATH = R::class.java.getResource("logo.png")?.toExternalForm()
    val TEXT_LOGO_PATH = R::class.java.getResource("text_logo.png")?.toExternalForm()
}
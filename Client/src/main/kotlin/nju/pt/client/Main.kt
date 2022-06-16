package nju.pt.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

fun main() {
    Application.launch(App::class.java)
}

class App : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.apply {
            scene = Scene(StartView.build()).apply {
                stylesheets.add(R.DEFAULT_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist"
        }.show()
    }

}
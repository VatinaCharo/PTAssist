package nju.pt.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import nju.pt.databaseassist.TeamData

fun main() {
    Application.launch(AppUI::class.java)
}

class AppUI: Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.apply {
            scene = Scene(StartView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist"
        }.show()
    }
}
class App(data:List<TeamData>){

}
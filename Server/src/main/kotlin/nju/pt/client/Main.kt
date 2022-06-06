package nju.pt.client

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage

fun main() {
    Application.launch(App::class.java)
}

class App : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.apply {
            scene = Scene(Group())
            title = "PTAssist-Server"
            width = 800.0
            height = 600.0
        }.show()



    }



}
package nju.pt.server

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
import org.slf4j.LoggerFactory

fun main() {
    Application.launch(App::class.java)
}

class App : Application() {
    private val logger = LoggerFactory.getLogger(App::class.java)
    override fun start(primaryStage: Stage) {
        primaryStage.apply {
            scene = Scene(Group())
            title = "PTAssist-Server"
            width = 800.0
            height = 600.0
        }.show()
        logger.info("完成UI构建，展示界面")
    }


}
package nju.pt.server

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

fun main() {
    Application.launch(App::class.java)
}

class App : Application() {
    private val logger = LoggerFactory.getLogger(App::class.java)
    override fun start(primaryStage: Stage) {
        var data = Data(listOf(), mapOf(), mapOf())
        try {
            data = JsonHelper.fromJson(R.DATA_JSON_PATH)
        } catch (e: FileNotFoundException) {
            logger.error("未找到JSON文件 ${e.message}")
        }

        primaryStage.apply {
            scene = Scene(MainView.build(data)).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "Match-Server ${R.VERSION}"
            minWidth = 800.0
            minHeight = 800.0
        }.show()
        MainView.apply {
            teamListView.selectionModel.selectedItemProperty().addListener { _, _, newSelectedTeamName ->
                val selectedTeamData = data.teamDataList.first { it.name == newSelectedTeamName }
                loadData(selectedTeamData.playerDataList, selectedTeamData.recordDataList)
            }
        }
        logger.info("完成UI构建，展示界面")
    }


}
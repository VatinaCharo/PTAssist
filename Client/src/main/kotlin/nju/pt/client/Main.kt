package nju.pt.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.serialization.json.Json
import nju.pt.R
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.TeamData
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    Application.launch(AppUI::class.java)
}

class AppUI : Application() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun start(primaryStage: Stage) {
        val configFile = File(R.SETTING_JSON_PATH)
        if (!configFile.exists()) {
            logger.info("未找到配置文件 ${configFile.absolutePath}，创建默认配置文件")
            JsonHelper.toJson(R.DEFAULT_CONFIG, R.SETTING_JSON_PATH)
        }
        var config = JsonHelper.fromJson<Config>(R.SETTING_JSON_PATH)
        primaryStage.apply {
            scene = Scene(StartView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist"
        }.show()

        val settingStage = Stage().apply {
            initOwner(primaryStage)
            initModality(Modality.WINDOW_MODAL)
            scene = Scene(SettingView.build(config)).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist-Setting"
            isResizable = false
        }

        StartView.apply {
            startBtn.setOnAction {
                println("show MatchView")
//                MatchView.build()
            }
            settingBtn.setOnAction {
                logger.info("打开设置界面 $settingStage")
                settingStage.show()
            }
        }
        SettingView.apply {
            saveBtn.setOnAction {
                logger.info("保存配置文件")
                config = saveConfig()
                logger.info("JsonHelper.toJson(config, R.SETTING_JSON_PATH)")
                logger.info("newConfig = $config")
                logger.info("R.SETTING_JSON_PATH = ${R.SETTING_JSON_PATH}")
                JsonHelper.toJson(config, R.SETTING_JSON_PATH)
                settingStage.close()
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class Config(
    val ip: String,
    val port: Int,
    val judgeCount: Int
)

class Match(data: List<TeamData>) {

}
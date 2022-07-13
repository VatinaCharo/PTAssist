package nju.pt.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.kotlin.ext.mkdirIfEmpty
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    Application.launch(AppUI::class.java)
}

class AppUI : Application() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val configFile = File(R.SETTING_JSON_PATH).mkdirIfEmpty()
    private val questionFile = File(R.QUESTION_JSON_PATH).mkdirIfEmpty()
    private val dataFile = File(R.DATA_JSON_PATH).mkdirIfEmpty()
    private var data: Data? = null

    override fun start(primaryStage: Stage) {
        if (!configFile.exists()) {
            logger.info("未找到配置文件 ${configFile.absolutePath}，创建默认配置文件")
            JsonHelper.toJson(R.DEFAULT_CONFIG, R.SETTING_JSON_PATH)
        }
        var config = JsonHelper.fromJson<Config>(R.SETTING_JSON_PATH)
        // 构建启动页Stage
        primaryStage.apply {
            scene = Scene(StartView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist"
        }.show()

        // 构建下载页Stage
        val downloadStage = Stage().apply {
            initOwner(primaryStage)
            initModality(Modality.WINDOW_MODAL)
            scene = Scene(DownloadView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist-Download"
            isResizable = false
        }

        // 构建设置页Stage
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
                if (dataFile.exists()) {
                    data = JsonHelper.fromJson(dataFile.absolutePath)
                    primaryStage.scene = Scene(MatchView.build(config.judgeCount, data!!.questionMap))
                } else {
                    logger.warn("未找到比赛数据文件，无法开始比赛，请先尝试下载比赛数据文件")
                    // TODO: 2022/7/13 @Eur3ka popup一个提示信息
                }
            }
            downloadBtn.setOnAction {
                logger.info("打开下载界面 $downloadStage")
                downloadStage.show()
            }
            settingBtn.setOnAction {
                logger.info("打开设置界面 $settingStage")
                settingStage.show()
            }
        }
        MatchView.apply {
            // TODO: 2022/7/13 @Eur3ka 处理比赛逻辑
        }
        DownloadView.apply {
            // TODO: 2022/7/12 @Eur3ka 处理下载文件逻辑，并显示结果到infoLabel上
        }
        SettingView.apply {
            saveBtn.setOnAction {
                logger.info("保存配置文件")
                config = saveConfig()
                logger.info("JsonHelper.toJson(config, R.SETTING_JSON_PATH)")
                logger.info("newConfig = $config")
                logger.info("R.SETTING_JSON_PATH = ${R.SETTING_JSON_PATH}")
                JsonHelper.toJson(config, R.SETTING_JSON_PATH)
                logger.info("关闭设置界面")
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
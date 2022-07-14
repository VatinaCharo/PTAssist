package nju.pt.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.RadioButton
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.PlayerData
import nju.pt.kotlin.ext.mkdirIfEmpty
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    Application.launch(AppUI::class.java)
}

enum class MatchState {
    QUESTION,
    MATCH
}

class AppUI : Application() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val configFile = File(R.SETTING_JSON_PATH).mkdirIfEmpty()
    private val dataFile = File(R.DATA_JSON_PATH).mkdirIfEmpty()
    private val cacheFile = File(R.CACHE_JSON_PATH).mkdirIfEmpty()
    private lateinit var data: Data
    private lateinit var cache: Cache
    private val usedQuestionIDList = mutableListOf<Int>()
    private val refuseQuestionIDList = mutableListOf<Int>()
    private val roundPlayerRecordList = mutableListOf<PlayerData>()
    private var state = MatchState.QUESTION

    override fun start(primaryStage: Stage) {
        // 获取配置文件
        var config = getConfig()
        // 根据配置文件选择规则
        val rule = when (config.rule) {
            RuleType.CUPT -> CUPTRule
            RuleType.JSYPT -> JSYPTRule
        }
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

        // 构建Popup信息页
        val popupStage = Stage().apply {
            initOwner(primaryStage)
            initModality(Modality.WINDOW_MODAL)
            scene = Scene(PopupView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist-Info"
            isResizable = false
        }

        StartView.apply {
//            启动比赛界面
            startBtn.setOnAction {
                // 载入数据JSON文件 和Cache
                if (dataFile.exists()) {
                    data = JsonHelper.fromJson(dataFile.absolutePath)
                    cache = getCache(data)
                    primaryStage.apply {
                        scene = Scene(MatchView.build(config.judgeCount)).apply {
                            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
                        }
                        title = "PTAssist-Match"
                    }
                } else {
                    logger.warn("未找到比赛数据文件，无法开始比赛，请先尝试下载比赛数据文件")
                    PopupView.info("未找到比赛数据文件，无法开始比赛，请先尝试下载比赛数据文件")
                    popupStage.show()
                }

                val repTeamRecordDataList = data.teamDataList[cache.teamIDMatchList[0]].recordDataList
                val repPlayerDataList = data.teamDataList[cache.teamIDMatchList[0]].playerDataList
                val oppTeamRecordDataList = data.teamDataList[cache.teamIDMatchList[1]].recordDataList
                val oppPlayerDataList = data.teamDataList[cache.teamIDMatchList[1]].playerDataList
                val revTeamRecordDataList = data.teamDataList[cache.teamIDMatchList[2]].recordDataList
                val revPlayerDataList = data.teamDataList[cache.teamIDMatchList[2]].playerDataList

                // 加载比赛队伍
                MatchView.loadTeam(data.teamDataList.map { it.id to it.name }
                    .sortedBy { cache.teamIDMatchList.indexOf(it.first) }.map { it.second })
                // 加载可选题
                MatchView.loadOptionalQuestions(
                    repTeamRecordDataList,
                    oppTeamRecordDataList,
                    usedQuestionIDList,
                    data.questionMap,
                    rule
                )
                // 加载正方队员
                MatchView.loadValidPlayer(
                    TeamType.REPORTER,
                    roundPlayerRecordList,
                    repTeamRecordDataList,
                    repPlayerDataList,
                    rule
                )
                // 加载反方队员
                MatchView.loadValidPlayer(
                    TeamType.OPPONENT,
                    roundPlayerRecordList,
                    oppTeamRecordDataList,
                    oppPlayerDataList,
                    rule
                )
                // 加载评方队员
                MatchView.loadValidPlayer(
                    TeamType.REVIEWER,
                    roundPlayerRecordList,
                    revTeamRecordDataList,
                    revPlayerDataList,
                    rule
                )
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
            // 拒题
//            本次比赛无需拒题，先略去
//            refuseBtn.setOnAction {
//                when (config.roundType) {
//                    RoundType.NORMAL -> {
//                        when(state){
//                            MatchState.QUESTION -> {
//                                val refuseQuestionID = group.userData as Int
//                                refuseQuestionIDList.add(refuseQuestionID)
//                                optionalQuestionsVBox.children.removeIf { ((it as RadioButton).userData as Int) == refuseQuestionID }
//                                logger.info("拒绝选题 $refuseQuestionID ${questionViewLabel.text}")
//                                logger.info("refuseQuestionIDList = $refuseQuestionIDList")
//                                PopupView.info("拒绝选题${questionViewLabel.text}")
//                                popupStage.show()
//                                questionViewLabel.text = ""
//                            }
//                            MatchState.MATCH -> {
//                                logger.info("选题已锁定，无法拒题")
//                                PopupView.info("选题已锁定，无法拒题")
//                                popupStage.show()
//                            }
//                        }
//                    }
//                    RoundType.SPECIAL -> {
//                        logger.info("自选题环节，无法拒题")
//                        PopupView.info("自选题环节，无法拒题")
//                        popupStage.show()
//                    }
//                }
//            }
            // 选题锁定
            confirmBtn.setOnAction {
                if (group.userData as Int != -1) {
                    when (state) {
                        MatchState.QUESTION -> {
                            val usedQuestionId = group.userData as Int
                            logger.info("usedQuestionIdList.add(usedQuestionId)")
                            usedQuestionIDList.add(usedQuestionId)
                            logger.info("usedQuestionId = $usedQuestionId")
                            logger.info("锁定选题 $usedQuestionId ${questionViewLabel.text}")
                            logger.info("usedQuestionIdList = $usedQuestionIDList")
                            PopupView.info("锁定选题${questionViewLabel.text}")
                            popupStage.show()
                            // 切换成比赛状态
                            state = MatchState.MATCH
                        }
                        else -> {
                            logger.info("选题已锁定，无法重复锁定")
                            PopupView.info("选题已锁定，无法重复锁定")
                            popupStage.show()
                        }
                    }
                } else {
                    logger.info("双方未选题，无法锁定")
                    PopupView.info("双方未选题，无法锁定")
                    popupStage.show()
                }
            }
            // 选题面板锁定，通过MatchState来作为锁定功能的标志信号
            group.selectedToggleProperty().addListener { _ ->
                if (group.selectedToggle != null) {
                    // 这里的selectedToggle就是被选中的RadioButton
                    val questionID = group.selectedToggle.userData as Int
                    when (state) {
                        MatchState.QUESTION -> {
                            logger.info("选中的题目编号为$questionID")
                            questionViewLabel.text = data.questionMap[questionID]
                            group.userData = questionID
                        }
                        else -> {
                            if (questionID != group.userData as Int) {
                                group.toggles.first { it.userData as Int == group.userData }.isSelected = true
                            }
                        }
                    }
                }
            }
            // 提交分数到本地数据库
            submitBtn.setOnAction {
                when (state) {
                    MatchState.MATCH -> {
                        // TODO: 2022/7/14 提交分数到本地以及发送文件到服务器端
                    }
                    else -> {
                        logger.info("选题信息未锁定，无法提交评分")
                        PopupView.info("选题信息未锁定，无法提交评分")
                        popupStage.show()
                    }
                }
            }
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

    private fun getConfig(): Config {
        // 获取配置文件
        if (!configFile.exists()) {
            logger.info("未找到配置文件 ${configFile.absolutePath}，创建默认配置文件")
            JsonHelper.toJson(R.DEFAULT_CONFIG, R.SETTING_JSON_PATH)
        }
        var config = R.DEFAULT_CONFIG
        try {
            config = JsonHelper.fromJson(R.SETTING_JSON_PATH)
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.error("配置文件读取失败，可能是文件损坏，已重置为默认配置文件：${e.message}")
            JsonHelper.toJson(R.DEFAULT_CONFIG, R.SETTING_JSON_PATH)
        }
        return config
    }

    private fun getCache(data: Data): Cache {
        val defaultCache = Cache(1, data.teamDataList.size, data.teamDataList.map { it.id })
        // 获取缓存文件
        if (!cacheFile.exists()) {
            logger.info("未找到缓存文件 ${configFile.absolutePath}，创建默认缓存文件")
            JsonHelper.toJson(defaultCache, R.CACHE_JSON_PATH)
        }
        var cache = defaultCache
        try {
            cache = JsonHelper.fromJson(R.CACHE_JSON_PATH)
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.error("缓存文件读取失败，可能是文件损坏，已重置为默认缓存文件：${e.message}")
            JsonHelper.toJson(defaultCache, R.CACHE_JSON_PATH)
        }
        return cache
    }
}

@kotlinx.serialization.Serializable
data class Config(
    val ip: String,
    val port: Int,
    val judgeCount: Int,
    val roundType: RoundType,
    val rule: RuleType
)

@kotlinx.serialization.Serializable
data class Cache(
    val phase: Int,
    val endPhase: Int,
    val teamIDMatchList: List<Int>
) {
    fun save() {
        JsonHelper.toJson(this, R.CACHE_JSON_PATH)
    }
}
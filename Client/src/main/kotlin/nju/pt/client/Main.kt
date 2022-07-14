package nju.pt.client

import RuleInterface
import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData
import nju.pt.kotlin.ext.mkdirIfEmpty
import nju.pt.kotlin.ext.rotate
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
    private lateinit var config: Config
    private val usedQuestionIDList = mutableListOf<Int>()
    private val refusedQuestionIDList = mutableListOf<Int>()
    private val roundPlayerRecordList = mutableListOf<PlayerData>()
    private var state = MatchState.QUESTION

    override fun start(primaryStage: Stage) {
        // 获取配置文件
        config = getConfig()
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
                    matchUILoad(rule)
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
            }
            downloadBtn.setOnAction {
                logger.info("开始下载数据文件")
                // TODO: 2022/7/15 下载数据文件
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
                        val isPlayerSelected =
                            informationVBox.children.dropLast(1).fold(true) { acc: Boolean, node: Node ->
                                val teamBar = node as TeamBar
                                acc and teamBar.isPlayerNotNull()
                            }
                        if (isPlayerSelected) {
                            // 锁定主控队员面板
                            informationVBox.children.forEach { (it as TeamBar).lock() }
                            logger.info("锁定主控队员面板")
                            // 锁定分数面板
                            scoresVBox.children.forEach {
                                if (it is ScoreBar) {
                                    it.lock()
                                }
                            }
                            logger.info("锁定分数面板")
                            // 存储比赛数据
                            // TODO: 2022/7/15 拒题部分
                            val repTeamData = data.teamDataList[cache.getRepTeamID()]
                            val repPlayerID =
                                repTeamData.playerDataList.first { it.name == (informationVBox.children[0] as TeamBar).getPlayerValue() }.id
                            val repScores = (scoresVBox.children[0] as ScoreBar).getScores()

                            val oppTeamData = data.teamDataList[cache.getOppTeamID()]
                            val oppPlayerID =
                                oppTeamData.playerDataList.first { it.name == (informationVBox.children[1] as TeamBar).getPlayerValue() }.id
                            val oppScores = (scoresVBox.children[1] as ScoreBar).getScores()

                            val revTeamData = data.teamDataList[cache.getRevTeamID()]
                            val revPlayerID =
                                revTeamData.playerDataList.first { it.name == (informationVBox.children[2] as TeamBar).getPlayerValue() }.id
                            val revScores = (scoresVBox.children[2] as ScoreBar).getScores()
                            // 更新RecordData
                            repTeamData.recordDataList.add(
                                RecordData(
                                    config.round,
                                    cache.phase,
                                    config.roomID,
                                    group.userData as Int,
                                    repPlayerID,
                                    "R",
                                    rule.getScore(repScores),
                                    rule.getRepScoreWeight(refusedQuestionIDList)
                                )
                            )
                            oppTeamData.recordDataList.add(
                                RecordData(
                                    config.round,
                                    cache.phase,
                                    config.roomID,
                                    group.userData as Int,
                                    oppPlayerID,
                                    "O",
                                    rule.getScore(oppScores),
                                    rule.getOppScoreWeight(refusedQuestionIDList)
                                )
                            )
                            revTeamData.recordDataList.add(
                                RecordData(
                                    config.round,
                                    cache.phase,
                                    config.roomID,
                                    group.userData as Int,
                                    revPlayerID,
                                    "O",
                                    rule.getScore(revScores),
                                    rule.getRevScoreWeight(refusedQuestionIDList)
                                )
                            )
                            // 更新缓存并保存
                            logger.info("cache = $cache")
                            logger.info("更新缓存")
                            cache = Cache(cache.phase + 1, cache.endPhase, cache.teamIDMatchList.rotate())
                            logger.info("cache = $cache")
                            logger.info("保存缓存文件")
                            cache.save()
                            // 保存数据文件
                            logger.info("JsonHelper.toJson(data, savePath)")
                            logger.info("data = $data")
                            logger.info("savePath = ${R.DATA_JSON_PATH}")
                            JsonHelper.toJson(data, R.DATA_JSON_PATH)
                            logger.info("保存数据文件")
                        } else {
                            logger.info("存在未选择的主控队员，无法提交评分")
                            PopupView.info("存在未选择的主控队员，无法提交评分")
                            popupStage.show()
                        }
                    }
                    else -> {
                        logger.info("选题信息未锁定，无法提交评分")
                        PopupView.info("选题信息未锁定，无法提交评分")
                        popupStage.show()
                    }
                }
            }
            // 下一阶段
            nextBtn.setOnAction {
                // 转换系统状态
                state = MatchState.QUESTION
                if (cache.phase > cache.endPhase) {
                    primaryStage.apply {
                        scene = Scene(StartView.build()).apply {
                            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
                        }
                        icons.add(Image(R.LOGO_PATH))
                        title = "PTAssist"
                    }
                    PopupView.info("上传数据")
                    // TODO: 2022/7/15 上传数据文件
                    popupStage.show()
                } else {
                    matchUILoad(rule)
                }
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
                logger.info("关闭设置界面")
                settingStage.close()
            }
        }
    }

    private fun matchUILoad(rule: RuleInterface) {
        data = JsonHelper.fromJson(dataFile.absolutePath)
        cache = getCache(data)
        val repTeamRecordDataList = data.teamDataList[cache.getRepTeamID()].recordDataList
        val repPlayerDataList = data.teamDataList[cache.getRepTeamID()].playerDataList
        val oppTeamRecordDataList = data.teamDataList[cache.getOppTeamID()].recordDataList
        val oppPlayerDataList = data.teamDataList[cache.getOppTeamID()].playerDataList
        val revTeamRecordDataList = data.teamDataList[cache.getRevTeamID()].recordDataList
        val revPlayerDataList = data.teamDataList[cache.getRevTeamID()].playerDataList

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

    private fun getConfig(): Config {
        // 获取配置文件
        if (!configFile.exists()) {
            logger.info("未找到配置文件 ${configFile.absolutePath}，创建默认配置文件")
            R.DEFAULT_CONFIG.save()
        }
        var config = R.DEFAULT_CONFIG
        try {
            config = JsonHelper.fromJson(R.SETTING_JSON_PATH)
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.error("配置文件读取失败，可能是文件损坏，已重置为默认配置文件：${e.message}")
            R.DEFAULT_CONFIG.save()
        }
        return config
    }

    private fun getCache(data: Data): Cache {
        val defaultCache = Cache(1, data.teamDataList.size, data.teamDataList.map { it.id })
        // 获取缓存文件
        if (!cacheFile.exists()) {
            logger.info("未找到缓存文件 ${configFile.absolutePath}，创建默认缓存文件")
            defaultCache.save()
        }
        var cache = defaultCache
        try {
            cache = JsonHelper.fromJson(R.CACHE_JSON_PATH)
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.error("缓存文件读取失败，可能是文件损坏，已重置为默认缓存文件：${e.message}")
            defaultCache.save()
        }
        return cache
    }
}

@kotlinx.serialization.Serializable
data class Config(
    val ip: String,
    val port: Int,
    val roomID: Int,
    val round: Int,
    val judgeCount: Int,
    val roundType: RoundType,
    val rule: RuleType
) {
    /**
     * 保存配置文件
     *
     */
    fun save() {
        JsonHelper.toJson(this, R.SETTING_JSON_PATH)
    }
}

@kotlinx.serialization.Serializable
data class Cache(
    val phase: Int,
    val endPhase: Int,
    val teamIDMatchList: List<Int>
) {
    fun getRepTeamID() = teamIDMatchList[0]
    fun getOppTeamID() = teamIDMatchList[1]
    fun getRevTeamID() = teamIDMatchList[2]

    /**
     * 保存缓存文件
     *
     */
    fun save() {
        JsonHelper.toJson(this, R.CACHE_JSON_PATH)
    }
}
package nju.pt.client

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.RadioButton
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.RecordData
import nju.pt.kotlin.ext.mkdirIfEmpty
import nju.pt.kotlin.ext.rotate
import nju.pt.net.FileNetClient
import nju.pt.net.Packet
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    Application.launch(AppUI::class.java)
}

enum class MatchState {
    QUESTION,
    SUBMIT,
    NEXT
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
    private val roundPlayerRecordList = mutableListOf<Int>()
    private var state = MatchState.QUESTION

    private lateinit var startScene: Scene
    private lateinit var matchScene: Scene

    override fun start(primaryStage: Stage) {
        // 获取配置文件
        config = getConfig()
        // 根据配置文件选择规则
        var rule = when (config.rule) {
            RuleType.CUPT -> CUPTRule
            RuleType.JSYPT -> JSYPTRule
        }
        logger.info("RULE: $rule")
        // 构建启动页Stage
        startScene = Scene(StartView.build()).apply {
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
        }
        primaryStage.apply {
            scene = startScene
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

        val aboutStage = Stage().apply {
            initOwner(primaryStage)
            initModality(Modality.WINDOW_MODAL)
            scene = Scene(AboutView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }
            icons.add(Image(R.LOGO_PATH))
            title = "PTAssist-About"
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
                    logger.info("载入数据JSON文件 和Cache")
                    val path = dataFile.absolutePath
                    logger.info("get data Json: $path")
                    data = JsonHelper.fromJson(path)
                    logger.info("data = $data")
                    logger.info("getCache(data)")
                    cache = getCache(data)
                    logger.info("cache = $cache")
                    matchScene = Scene(MatchView.build(config.judgeCount)).apply {
                        stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
                    }
                    // 对于需要拒题功能的比赛，展示出拒题按钮
                    if (config.rule == RuleType.CUPT && config.roundType == RoundType.NORMAL) {
                        logger.info("CUPT规则正常比赛轮次，启用拒题模块")
                        MatchView.confirmHBox.children.add(MatchView.refuseBtn)
                    }
                    primaryStage.apply {
                        scene = matchScene
                        title = "PTAssist-Match"
                    }
                    // 加载数据到UI界面
                    logger.info("加载数据到UI界面 matchUILoad(rule)")
                    matchUILoad(rule)
                } else {
                    logger.warn("未找到比赛数据文件，无法开始比赛，请先尝试下载比赛数据文件")
                    PopupView.info("未找到比赛数据文件，无法开始比赛，请先尝试下载比赛数据文件")
                    popupStage.show()
                }
            }
            downloadBtn.setOnAction {
                when (config.mode) {
                    WorkMode.ONLINE -> {
                        logger.info("开始下载数据文件")
                        PopupView.info("数据下载中，请等待...")
                        popupStage.show()
                        // 下载数据
                        Thread {
                            var packet = Packet(config.roomID, config.round, null)
                            logger.info("构建下载数据的请求数据包 packet = $packet")
                            logger.info("发送下载请求 ip = ${config.ip}, port = ${config.port}")
                            packet = FileNetClient(config.ip, config.port).download(packet)
                            logger.info("接收到响应数据包")
                            if (packet.data == null) {
                                Platform.runLater {
                                    logger.info("服务器数据文件尚未准备完毕，无法获取数据文件")
                                    PopupView.info("服务器数据文件尚未准备完毕，请稍后再试")
                                    popupStage.show()
                                }
                            } else {
                                Platform.runLater {
                                    logger.info("数据下载完毕")
                                    logger.info("保存数据文件${packet.data}")
                                    JsonHelper.toJson(packet.data, R.DATA_JSON_PATH)
                                    logger.info("清空缓存")
                                    File(R.CACHE_JSON_PATH).delete()
                                    PopupView.info("数据下载完毕")
                                    popupStage.show()
                                }
                            }
                        }.start()
                    }

                    WorkMode.OFFLINE -> {
                        logger.info("当前处于离线模式，需要手动放入数据文件，并删除缓存文件")
                        PopupView.info("当前处于离线模式，请手动放入数据文件，并删除缓存文件")
                        popupStage.show()
                    }
                }
            }
            settingBtn.setOnAction {
                logger.info("打开设置界面 $settingStage")
                settingStage.show()
            }
            aboutBtn.setOnAction {
                logger.info("打开关于界面 $aboutStage")
                aboutStage.show()
            }
        }
        MatchView.apply {
            // 拒题
            refuseBtn.setOnAction {
                when (config.roundType) {
                    RoundType.NORMAL -> {
                        when (state) {
                            MatchState.QUESTION -> {
                                val refuseQuestionID = group.userData as Int
                                logger.info("拒题 refuseQuestionID = $refuseQuestionID")
                                refusedQuestionIDList.add(refuseQuestionID)
                                optionalQuestionsVBox.children.removeIf { ((it as RadioButton).userData as Int) == refuseQuestionID }
                                logger.info("拒绝选题 $refuseQuestionID ${questionViewLabel.text}")
                                logger.info("refuseQuestionIDList = $refusedQuestionIDList")
                                PopupView.info("拒绝选题${questionViewLabel.text}")
                                popupStage.show()
                                questionViewLabel.text = ""
                            }

                            else -> {
                                logger.info("选题已锁定，无法拒题")
                                PopupView.info("选题已锁定，无法拒题")
                                popupStage.show()
                            }
                        }
                    }

                    RoundType.SPECIAL -> {
                        logger.info("自选题环节，无法拒题")
                        PopupView.info("自选题环节，无法拒题")
                        popupStage.show()
                    }
                }
            }
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
                            state = MatchState.SUBMIT
                        }

                        else -> {
                            logger.info("选题已锁定，无法重复锁定 state = $state")
                            PopupView.info("选题已锁定，无法重复锁定")
                            popupStage.show()
                        }
                    }
                } else {
                    logger.info("双方未选题，无法锁定 state = $state")
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
                    MatchState.SUBMIT -> {
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
                            val repTeamData = data.teamDataList.first { it.id == cache.getRepTeamID() }
                            val repPlayerID =
                                repTeamData.playerDataList.first { it.name == (informationVBox.children[0] as TeamBar).getPlayerValue() }.id
                            val repScores = (scoresVBox.children[0] as ScoreBar).getScores()

                            val oppTeamData = data.teamDataList.first { it.id == cache.getOppTeamID() }
                            val oppPlayerID =
                                oppTeamData.playerDataList.first { it.name == (informationVBox.children[1] as TeamBar).getPlayerValue() }.id
                            val oppScores = (scoresVBox.children[1] as ScoreBar).getScores()

                            val revTeamData = data.teamDataList.first { it.id == cache.getRevTeamID() }
                            val revPlayerID =
                                revTeamData.playerDataList.first { it.name == (informationVBox.children[2] as TeamBar).getPlayerValue() }.id
                            val revScores = (scoresVBox.children[2] as ScoreBar).getScores()
                            // 增加队员主控记录
                            roundPlayerRecordList.addAll(listOf(repPlayerID, oppPlayerID, revPlayerID))
                            // 更新RecordData
                            refusedQuestionIDList.forEach {
                                val record = RecordData(
                                    config.round,
                                    cache.phase,
                                    config.roomID,
                                    it,
                                    0,
                                    "X",
                                    0.0,
                                    rule.getRepScoreWeight(repTeamData.recordDataList, true)
                                )
                                logger.info("添加拒题记录 record = $record")
                                repTeamData.recordDataList.add(record)
                            }
                            val repRecord = RecordData(
                                config.round,
                                cache.phase,
                                config.roomID,
                                group.userData as Int,
                                repPlayerID,
                                "R",
                                rule.getScore(repScores),
                                rule.getRepScoreWeight(repTeamData.recordDataList, false)
                            )
                            logger.info("添加正方记录 repRecord = $repRecord")
                            repTeamData.recordDataList.add(repRecord)
                            val oppRecord = RecordData(
                                config.round,
                                cache.phase,
                                config.roomID,
                                group.userData as Int,
                                oppPlayerID,
                                "O",
                                rule.getScore(oppScores),
                                rule.getOppScoreWeight()
                            )
                            logger.info("添加反方记录 oppRecord = $oppRecord")
                            oppTeamData.recordDataList.add(oppRecord)
                            val revRecord = RecordData(
                                config.round,
                                cache.phase,
                                config.roomID,
                                group.userData as Int,
                                revPlayerID,
                                "V",
                                rule.getScore(revScores),
                                rule.getRevScoreWeight()
                            )
                            logger.info("添加反方记录 revRecord = $revRecord")
                            revTeamData.recordDataList.add(revRecord)
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
                            // 转换状态至NEXT
                            state = MatchState.NEXT
                            logger.info("state = $state")
                        } else {
                            logger.info("存在未选择的主控队员，无法提交评分 state = $state")
                            PopupView.info("存在未选择的主控队员，无法提交评分")
                            popupStage.show()
                        }
                    }

                    else -> {
                        logger.info("选题信息未锁定，无法提交评分 state = $state")
                        PopupView.info("选题信息未锁定，无法提交评分")
                        popupStage.show()
                    }
                }
            }
            // 下一阶段
            nextBtn.setOnAction {
                when (state) {
                    MatchState.QUESTION -> {
                        logger.info("选题信息未锁定，无法进行下一场 state = $state")
                        PopupView.info("选题信息未锁定，无法进行下一场")
                        popupStage.show()
                    }

                    MatchState.SUBMIT -> {
                        logger.info("比赛数据尚未提交，无法进行下一场 state = $state")
                        PopupView.info("比赛数据尚未提交，无法进行下一场")
                        popupStage.show()
                    }

                    MatchState.NEXT -> {
                        if (cache.phase > cache.endPhase) {
                            // 切换到启动页
                            logger.info("切换到启动界面 scene = $startScene")
                            primaryStage.scene = startScene
                            when (config.mode) {
                                WorkMode.ONLINE -> {
                                    PopupView.info("上传数据")
                                    Thread {
                                        logger.info("上传数据")
                                        logger.info("data = $data")
                                        FileNetClient(config.ip, config.port).upload(
                                            Packet(config.roomID, config.round, data)
                                        )
                                        Platform.runLater {
                                            logger.info("上传数据完毕")
                                            PopupView.info("上传数据完毕")
                                            popupStage.show()
                                        }
                                    }.start()
                                    popupStage.show()
                                    // 回到启动页
                                }

                                WorkMode.OFFLINE -> {
                                    logger.info("离线模式，无法上传数据，需要手动提交数据文件")
                                    PopupView.info("离线模式，无法上传数据，请手动提交数据文件")
                                    popupStage.show()
                                }
                            }
                            logger.info("state = $state")
                        } else {
                            matchUILoad(rule)
                            // 重置主控队员面板
                            informationVBox.children.forEach { (it as TeamBar).reset() }
                            logger.info("重置主控队员面板")
                            // 重置分数面板
                            scoresVBox.children.forEach {
                                if (it is ScoreBar) {
                                    it.reset()
                                }
                            }
                            // 重置拒题列表
                            refusedQuestionIDList.clear()
                            logger.info("重置分数面板")
                            // 重置赛题展示Label
                            questionViewLabel.text = ""
                            logger.info("重置赛题展示Label")
                            // 转换系统状态
                            state = MatchState.QUESTION
                        }
                    }
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
//                更新规则库
                rule = when (config.rule) {
                    RuleType.CUPT -> CUPTRule
                    RuleType.JSYPT -> JSYPTRule
                }
                logger.info("RULE: $rule")
            }
        }
        AboutView.nju.setOnAction {
            hostServices.showDocument(R.NJU_LINK)
            hostServices.showDocument(R.AUTHOR1_LINK)
            hostServices.showDocument(R.AUTHOR2_LINK)
        }
    }

    private fun matchUILoad(rule: RuleInterface) {
        val repTeamRecordDataList = data.teamDataList.first { it.id == cache.getRepTeamID() }.recordDataList
        val repPlayerDataList = data.teamDataList.first { it.id == cache.getRepTeamID() }.playerDataList
        val oppTeamRecordDataList = data.teamDataList.first { it.id == cache.getOppTeamID() }.recordDataList
        val oppPlayerDataList = data.teamDataList.first { it.id == cache.getOppTeamID() }.playerDataList
        val revTeamRecordDataList = data.teamDataList.first { it.id == cache.getRevTeamID() }.recordDataList
        val revPlayerDataList = data.teamDataList.first { it.id == cache.getRevTeamID() }.playerDataList
        logger.info("repTeamRecordDataList = $repTeamRecordDataList")
        logger.info("repPlayerDataList = $repPlayerDataList")
        logger.info("oppTeamRecordDataList = $oppTeamRecordDataList")
        logger.info("oppPlayerDataList = $oppPlayerDataList")
        logger.info("revTeamRecordDataList = $revTeamRecordDataList")
        logger.info("revPlayerDataList = $revPlayerDataList")

        val teamID2NameList = data.teamDataList.map { it.id to it.name }
        logger.info("teamID2NameList = $teamID2NameList")
        val sortedTeamID2NameList = teamID2NameList.sortedBy { cache.teamIDMatchList.indexOf(it.first) }
        logger.info("sortedTeamID2NameList = $sortedTeamID2NameList")
        // 加载比赛队伍
        MatchView.loadTeam(sortedTeamID2NameList.map { it.second })
        // 加载可选题
        MatchView.loadOptionalQuestions(
            repTeamRecordDataList,
            oppTeamRecordDataList,
            usedQuestionIDList,
            data.questionMap,
            rule,
            config.roundType
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
    val rule: RuleType,
    val mode: WorkMode
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
package nju.pt.client

import RuleInterface
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import nju.pt.R
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData
import org.slf4j.LoggerFactory

object StartView {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val rootStackPane = StackPane().apply { id = "StartView_rootStackPane" }
    val imageView = ImageView().apply { id = "StartView_imageView" }
    val menuVBox = VBox().apply { id = "StartView_menuVBox" }

    val startBtn = Button("开始比赛").apply { id = "StartView_startBtn" }
    val downloadBtn = Button("下载数据").apply { id = "StartView_downloadBtn" }
    val settingBtn = Button("设置").apply { id = "StartView_settingBtn" }
    val aboutBtn = Button("关于软件").apply { id = "StartView_aboutBtn" }

    private fun init() = apply {
        logger.info("init()")
        rootStackPane.apply {
            children.addAll(imageView, menuVBox)
            menuVBox.children.addAll(startBtn, downloadBtn, settingBtn, aboutBtn)
        }
        imageView.apply {
            image = Image(R.MAIN_IMAGE_PATH)
        }
        logger.info("init() return => $this")
    }

    private fun layout() = apply {
        logger.info("layout()")
        imageView.apply {
            fitWidth = 0.9 * image.width
            fitHeight = 0.9 * image.height
        }
        rootStackPane.apply {
            prefWidth = imageView.fitWidth
            prefHeight = imageView.fitHeight
        }
        logger.info("layout() return => $this")
    }

    fun build(): StackPane {
        logger.info("build()")
        init()
        layout()
        logger.info("build() return => $rootStackPane")
        return rootStackPane
    }
}

object MatchView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootHBox = HBox().apply { id = "MatchView_rootHBox" }
    private val optionalQuestionsStackPane = StackPane().apply { id = "MatchView_optionalQuestionsStackPane" }
    private val optionalQuestionsVBox = VBox().apply { id = "MatchView_optionalQuestionsVBox" }
    private val njuLogoImageView = ImageView(R.LOGO_PATH).apply { id = "MatchView_njuLogoImageView" }
    private val operationsVBox = VBox().apply { id = "MatchView_operationsVBox" }
    private val confirmHBox = HBox().apply { id = "MatchView_confirmHBox" }
    val questionViewLabel = Label("Here is selected question").apply { id = "MatchView_questionViewLabel" }
    val confirmBtn = Button("确认").apply { id = "MatchView_confirmBtn" }

    //    val refuseBtn = Button("拒绝").apply { id = "MatchView_refuseBtn" }
    val informationVBox = VBox().apply { id = "MatchView_informationVBox" }

    //    val lockBtn = Button("锁定").apply { id = "MatchView_lockBtn" }
    val scoresVBox = VBox().apply { id = "MatchView_scoresVBox" }
    private val submitAndNextHBox = HBox().apply { id = "MatchView_submitAndNextHBox" }
    val submitBtn = Button("确认").apply { id = "MatchView_submitBtn" }
    val nextBtn = Button("下一场").apply { id = "MatchView_nextBtn" }
    private val njuTextLogoImageView = ImageView(R.TEXT_LOGO_PATH).apply { id = "MatchView_njuTextLogoImageView" }
    val group = ToggleGroup()

    private fun init(judgeCount: Int) {
        logger.info("init(judgeCount: Int)")
        // Main Tab
        rootHBox.children.addAll(optionalQuestionsStackPane, operationsVBox)
        optionalQuestionsStackPane.children.addAll(njuLogoImageView, optionalQuestionsVBox)
        operationsVBox.children.addAll(confirmHBox, informationVBox, scoresVBox)
//        confirmHBox.children.addAll(questionViewLabel, confirmBtn, refuseBtn)
        confirmHBox.children.addAll(questionViewLabel, confirmBtn)
        informationVBox.children.addAll(
            TeamBar(TeamType.REPORTER),
            TeamBar(TeamType.OPPONENT),
            TeamBar(TeamType.REVIEWER),
            TeamBar(TeamType.OBSERVER)
        )
        scoresVBox.children.addAll(
            ScoreBar("正：", judgeCount),
            ScoreBar("反：", judgeCount),
            ScoreBar("评：", judgeCount),
            submitAndNextHBox.apply {
                children.addAll(submitBtn, nextBtn)
            },
            njuTextLogoImageView
        )
        // 默认不选中，置为-1，后续运行时置为赛题编号
        group.apply {
            userData = -1
        }
    }

    private fun layout() {
        logger.info("layout()")
        logger.info("setting njuLogoImageView layout")
        njuLogoImageView.apply {
            njuLogoImageView.fitWidth = 0.7 * njuLogoImageView.image.width
            njuLogoImageView.fitHeight = 0.7 * njuLogoImageView.image.height
            logger.info("fitSize = ($fitWidth, $fitHeight)")
        }
        logger.info("setting njuTextLogoImageView layout")
        njuTextLogoImageView.apply {
            fitWidth = 0.45 * njuTextLogoImageView.image.width
            fitHeight = 0.45 * njuTextLogoImageView.image.height
            logger.info("fitSize = ($fitWidth, $fitHeight)")
        }
    }

    /**
     * 构建UI界面
     *
     * @param judgeCount 裁判数
     * @return
     */
    fun build(judgeCount: Int): HBox {
        logger.info("build()")
        logger.info("init <<< judgeCount = $judgeCount")
        init(judgeCount)
        layout()
        logger.info("build() return => $rootHBox")
        return rootHBox
    }

    fun loadTeam(teamNameList: List<String>) {
        teamNameList.forEachIndexed { index, name ->
            val teamBar = informationVBox.children[index] as TeamBar
            teamBar.loadTeam(name)
        }
        if (teamNameList.size == 3) {
            val teamBar = informationVBox.children.last() as TeamBar
            teamBar.loadTeam("无观摩方")
        }
    }

    /**
     * Load optional questions
     *
     * @param questionMap 当前的可选题
     */
    private fun loadOptionalQuestions(questionMap: Map<Int, String>) {
        optionalQuestionsVBox.children.clear()
        optionalQuestionsVBox.children.addAll(questionMap.map {
            RadioButton().apply {
                text = String.format("%02d  ${it.value}", it.key)
                userData = it.key
                toggleGroup = group
            }
        })
    }

    fun loadOptionalQuestions(
        repTeamRecordDataList: List<RecordData>,
        oppTeamRecordDataList: List<RecordData>,
        usedQuestionIDList: List<Int>,
        questionLibMap: Map<Int, String>,
        rule: RuleInterface
    ) {
        logger.info("getOptionalQuestionIDList(repTeamRecordDataList, oppTeamRecordDataList, usedQuestionIDList, questionIDLibList)")
        logger.info("repTeamRecordDataList = $repTeamRecordDataList")
        logger.info("oppTeamRecordDataList = $oppTeamRecordDataList")
        logger.info("usedQuestionIDList = $usedQuestionIDList")
        logger.info("questionIDLibList = ${questionLibMap.keys.toList()}")
        // 当前对局的可选题目编号
        val questionIDList =
            rule.getOptionalQuestionIDList(
                repTeamRecordDataList,
                oppTeamRecordDataList,
                usedQuestionIDList,
                questionLibMap.keys.toList()
            )
        logger.info("questionIDList = $questionIDList")
        // 加载可选题到UI中
        loadOptionalQuestions(questionLibMap.filter { it.key in questionIDList })
    }

    fun loadValidPlayer(
        type: TeamType,
        roundPlayerRecordList: List<PlayerData>,
        teamRecordDataList: List<RecordData>,
        playerDataList: List<PlayerData>,
        rule: RuleInterface
    ) {
        informationVBox.children.forEach {
            val teamBar = it as TeamBar
            if (teamBar.type == type) {
                logger.info("rule.getValidPlayerIDList(roundPlayerRecordList, teamRecordDataList, playerDataList)")
                logger.info("roundPlayerRecordList = $roundPlayerRecordList")
                logger.info("teamRecordDataList = $teamRecordDataList")
                logger.info("playerDataList = $playerDataList")
                val validPlayerIDList =
                    rule.getValidPlayerIDList(roundPlayerRecordList, teamRecordDataList, playerDataList)
                logger.info("validPlayerIDList = $validPlayerIDList")
                teamBar.loadValidPlayer(playerDataList.filter { it.id in validPlayerIDList }.map { it.name })
            }
        }
    }
}

object SettingView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootGridPane = GridPane().apply { id = "SettingView_rootGridPane" }
    private val ipLabel = Label("IP:")
    private val ipTF = TextField()
    private val portLabel = Label("端口:")
    private val portTF = TextField()
    private val roomIDLabel = Label("会场编号:")
    private val roomIDTF = TextField()
    private val roundLabel = Label("轮次:")
    private val roundTF = TextField()
    private val judgeCountLabel = Label("裁判数:")
    private val judgeCountTF = TextField()
    private val roundTypeLabel = Label("本轮比赛类型:")
    private val roundTypeCB = ComboBox<String>().apply { id = "SettingView_roundTypeCB" }
    private val ruleTypeLabel = Label("规则:")
    private val ruleTypeCB = ComboBox<RuleType>().apply { id = "SettingView_ruleTypeCB" }
    private val modeLabel = Label("工作模式:")
    private val modeCB = ComboBox<WorkMode>().apply { id = "SettingView_modeCB" }
    val saveBtn = Button("保存").apply { id = "SettingView_saveBtn" }

    private fun init(config: Config) {
        logger.info("init(config: Config)")
        rootGridPane.add(ipLabel, 0, 0)
        rootGridPane.add(portLabel, 0, 1)
        rootGridPane.add(roomIDLabel, 0, 2)
        rootGridPane.add(roundLabel, 0, 3)
        rootGridPane.add(judgeCountLabel, 0, 4)
        rootGridPane.add(roundTypeLabel, 0, 5)
        rootGridPane.add(ruleTypeLabel, 0, 6)
        rootGridPane.add(modeLabel, 0, 7)

        rootGridPane.add(ipTF, 1, 0)
        rootGridPane.add(portTF, 1, 1)
        rootGridPane.add(roomIDTF, 1, 2)
        rootGridPane.add(roundTF, 1, 3)
        rootGridPane.add(judgeCountTF, 1, 4)
        rootGridPane.add(roundTypeCB, 1, 5)
        rootGridPane.add(ruleTypeCB, 1, 6)
        rootGridPane.add(modeCB, 1, 7)

        rootGridPane.add(saveBtn, 1, 8)

        ipTF.apply {
            tooltip = Tooltip("服务器ip地址")
            text = config.ip
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})?(\\.?((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})?){3}$"))) newValue else oldValue
            }
        }
        portTF.apply {
            tooltip = Tooltip("服务器端口")
            text = config.port.toString()
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^(\\d?|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$"))) newValue else oldValue
            }
        }
        roomIDTF.apply {
            tooltip = Tooltip("会场编号")
            text = config.roomID.toString()
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^\\d*$"))) newValue else oldValue
            }
        }
        roundTF.apply {
            tooltip = Tooltip("比赛轮次")
            text = config.round.toString()
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^\\d*$"))) newValue else oldValue
            }
        }
        judgeCountTF.apply {
            tooltip = Tooltip("裁判人数")
            text = config.judgeCount.toString()
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^\\d*$"))) newValue else oldValue
            }
        }
        ruleTypeCB.apply {
            items.addAll(RuleType.CUPT, RuleType.JSYPT)
            value = config.rule
        }
        roundTypeCB.apply {
            items.addAll(RoundType.NORMAL.toString(), RoundType.SPECIAL.toString())
            value = config.roundType.toString()
        }
        modeCB.apply {
            items.addAll(WorkMode.ONLINE, WorkMode.OFFLINE)
            value = config.mode
        }
    }

    private fun layout() {
        logger.info("layout()")
        ipTF.alignment = Pos.CENTER_RIGHT
        portTF.alignment = Pos.CENTER_RIGHT
        roomIDTF.alignment = Pos.CENTER_RIGHT
        roundTF.alignment = Pos.CENTER_RIGHT
        judgeCountTF.alignment = Pos.CENTER_RIGHT
    }

    fun build(config: Config): GridPane {
        logger.info("build(config:Config)")
        init(config)
        layout()
        logger.info("build() return => $rootGridPane")
        return rootGridPane
    }

    private fun getRoundType(value: String) = when (value) {
        "正常模式" -> RoundType.NORMAL
        "自选题模式" -> RoundType.SPECIAL
        else -> {
            TODO("Not yet implement")
        }
    }

    fun saveConfig() =
        Config(
            ipTF.text,
            portTF.text.toInt(),
            roomIDTF.text.toInt(),
            roundTF.text.toInt(),
            judgeCountTF.text.toInt(),
            getRoundType(roundTypeCB.value),
            ruleTypeCB.value,
            modeCB.value
        )
}

object AboutView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootAnchorPane = AnchorPane()
    private val nameLabel = Label("PTAssist v${R.VERSION}").apply {
        font = Font.font(18.0)
    }
    private val noteLabel = Label("注意事项:").apply {
        font = Font.font(18.0)
    }
    private val notes = listOf(
        "1. 完成一轮比赛后，程序会自动回到首页，此时可以关闭程序",
        "2. 重启程序时，程序会保留上次提交数据时的状态，但不保留下次提交前的全部操作",
        "3. 处于离线模式时，程序不会上传数据也不会下载数据，需手动更换数据库文件"
    )
    private val labelList = notes.map {
        Label(it).apply {
            font = Font.font(18.0)
        }
    }
    val nju = Hyperlink("NJU", Label("By Eur3ka & EnjoyXu @ "))

    fun build(): AnchorPane {
        logger.info("init()")
        rootAnchorPane.apply {
            children.addAll(nameLabel, noteLabel)
            children.addAll(labelList)
            children.add(nju)
        }
        nameLabel.layoutX = 50.0
        nameLabel.layoutY = 50.0
        noteLabel.layoutX = 50.0
        noteLabel.layoutY = 100.0
        labelList.forEachIndexed { index, label ->
            label.layoutX = 100.0
            label.layoutY = 150.0 + index * 50.0
        }
        nju.layoutX = 600.0
        nju.layoutY = 500.0
        rootAnchorPane.style = "-fx-background-color:#FAE3D9"
        return rootAnchorPane
    }
}

object PopupView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootGridPane = GridPane().apply { id = "PopupView_rootGridPane" }
    private val infoLabel = Label().apply { id = "PopupView_infoLabel" }

    private fun init() {
        logger.info("init()")
        rootGridPane.add(infoLabel, 0, 0)
    }

    fun info(msg: String) {
        infoLabel.text = msg
    }

    fun build(): GridPane {
        logger.info("build()")
        init()
        logger.info("build() return => $rootGridPane")
        return rootGridPane
    }
}
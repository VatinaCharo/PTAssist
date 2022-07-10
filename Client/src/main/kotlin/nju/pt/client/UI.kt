package nju.pt.client

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import nju.pt.R
import org.slf4j.LoggerFactory
import kotlin.math.log

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
            fitWidth = 0.15 * image.width
            fitHeight = 0.15 * image.height
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
    val optionalQuestionsVBox = VBox().apply { id = "MatchView_optionalQuestionsVBox" }
    private val njuLogoImageView = ImageView(R.LOGO_PATH).apply { id = "MatchView_njuLogoImageView" }
    private val operationsVBox = VBox().apply { id = "MatchView_operationsVBox" }
    private val confirmHBox = HBox().apply { id = "MatchView_confirmHBox" }
    val questionViewLabel = Label("Here is question view").apply { id = "MatchView_questionViewLabel" }
    val confirmBtn = Button("确认").apply { id = "MatchView_confirmBtn" }
    val refuseBtn = Button("拒绝").apply { id = "MatchView_refuseBtn" }
    val informationVBox = VBox().apply { id = "MatchView_informationVBox" }
    val lockBtn = Button("锁定").apply { id = "MatchView_lockBtn" }
    val scoresVBox = VBox().apply { id = "MatchView_scoresVBox" }
    private val submitAndNextHBox = HBox().apply { id = "MatchView_submitAndNextHBox" }
    val submitBtn = Button("确认").apply { id = "MatchView_submitBtn" }
    val nextBtn = Button("下一场").apply { id = "MatchView_nextBtn" }
    private val njuTextLogoImageView = ImageView(R.TEXT_LOGO_PATH).apply { id = "MatchView_njuTextLogoImageView" }
    val group = ToggleGroup()

    private fun init(judgeCount: Int, questionMap: Map<Int, String>) {
        logger.info("init()")
        // Main Tab
        rootHBox.children.addAll(optionalQuestionsStackPane, operationsVBox)
        optionalQuestionsStackPane.children.addAll(njuLogoImageView, optionalQuestionsVBox)
        optionalQuestionsVBox.children.addAll(questionMap.toList().map {
            RadioButton().apply {
                text = String.format("%02d  ${it.second}", it.first)
                userData = it.first
                toggleGroup = group
            }
        })
        operationsVBox.children.addAll(confirmHBox, informationVBox, scoresVBox)
        confirmHBox.children.addAll(questionViewLabel, confirmBtn, refuseBtn)
        informationVBox.children.addAll(
            TeamBar(TeamType.REPORTER),
            TeamBar(TeamType.OPPONENT),
            TeamBar(TeamType.REVIEWER),
            TeamBar(TeamType.OBSERVER).apply {
                children.add(lockBtn)
            }
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

    fun build(judgeCount: Int, questionMap: Map<Int, String>): HBox {
        logger.info("build()")
        logger.info("init <<< judgeCount = $judgeCount   questionMap = $questionMap")
        init(judgeCount, questionMap)
        layout()
        logger.info("build() return => $rootHBox")
        return rootHBox
    }
}

object DownloadView {
    private val logger = LoggerFactory.getLogger(this::class.java)
}

object SettingView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootGridPane = GridPane().apply { id = "SettingView_rootGridPane" }
    private val ipLabel = Label("IP:")
    private val ipTF = TextField()
    private val portLabel = Label("Port:")
    private val portTF = TextField()
    private val judgeCountLabel = Label("JudgeCount:")
    private val judgeCountTF = TextField()
    val saveBtn = Button("保存").apply { id = "SettingView_saveBtn" }

    private fun init(config: Config) = apply {
        logger.info("init(config: Config)")
        rootGridPane.add(ipLabel, 0, 0)
        rootGridPane.add(portLabel, 0, 1)
        rootGridPane.add(judgeCountLabel, 0, 2)

        rootGridPane.add(ipTF, 1, 0)
        rootGridPane.add(portTF, 1, 1)
        rootGridPane.add(judgeCountTF, 1, 2)

        rootGridPane.add(saveBtn, 1, 3)

        ipTF.apply {
            tooltip = Tooltip("服务器ip地址")
            text = config.ip
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$"))) newValue else oldValue
            }
        }
        portTF.apply {
            tooltip = Tooltip("服务器端口")
            text = config.port.toString()
            textProperty().addListener { _, oldValue, newValue ->
                text =
                    if (newValue.matches(Regex("^(\\d|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$"))) newValue else oldValue
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
    }

    private fun layout() {
        logger.info("layout()")
        ipTF.alignment = Pos.CENTER_RIGHT
        portTF.alignment = Pos.CENTER_RIGHT
        judgeCountTF.alignment = Pos.CENTER_RIGHT
    }

    fun build(config: Config): GridPane {
        logger.info("build(config:Config)")
        init(config)
        logger.info("build() return => $rootGridPane")
        return rootGridPane
    }
    fun saveConfig() = Config(ipTF.text, portTF.text.toInt(), judgeCountTF.text.toInt())
}

object AboutView {
    private val logger = LoggerFactory.getLogger(this::class.java)
}
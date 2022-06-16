package nju.pt.client

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory

object StartView {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val root = StackPane()
    val imageView = ImageView()
    val menuVBox = VBox()

    val startBtn = Button("开始比赛")
    val downloadBtn = Button("下载数据")
    val settingBtn = Button("设置")
    val aboutBtn = Button("关于软件")

    private fun init() = apply {
        logger.info("init()")
        root.apply {
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
        root.apply {
            prefWidth = imageView.fitWidth
            prefHeight = imageView.fitHeight
            menuVBox.apply {
                maxWidth = 120.0
                spacing = 20.0
                padding = Insets(20.0, 10.0, 20.0, 10.0)
                StackPane.setAlignment(this, Pos.BOTTOM_LEFT)
                menuVBox.alignment = Pos.BOTTOM_CENTER
            }
        }
        logger.info("layout() return => $this")
    }

    private fun render() = apply {
        logger.info("render()")
        menuVBox.style = "-fx-background-color:#FAE3D955"
        logger.info("render() return => $this")
    }

    fun build(): StackPane {
        logger.info("build()")
        init()
        layout()
        render()
        logger.info("build() return => $root")
        return root
    }
}

object MatchView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val root = HBox()
    private val optionalQuestionsStackPane = StackPane()
    val optionalQuestionsVBox = VBox()
    private val njuLogoImageView = ImageView(R.LOGO_PATH)
    private val operationsVBox = VBox()
    private val confirmHBox = HBox()
    val questionViewLabel = Label("Here is question view")
    val confirmBtn = Button("确认")
    val refuseBtn = Button("拒绝")
    val informationVBox = VBox()
    val lockBtn = Button("锁定")
    val scoresVBox = VBox()
    private val submitAndNextHBox = HBox()
    val submitBtn = Button("确认")
    val nextBtn = Button("下一场")
    val resetBtn = Button("重置比赛数据")
    private val njuTextLogoImageView = ImageView(R.TEXT_LOGO_PATH)
    val toggleGroup = ToggleGroup()

    private fun init(judgeCount: Int) = apply {
        logger.info("init()")
        // Main Tab
        root.children.addAll(optionalQuestionsStackPane, operationsVBox)
        optionalQuestionsStackPane.children.addAll(njuLogoImageView, optionalQuestionsVBox)
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
                children.addAll(submitBtn, nextBtn, resetBtn)
            },
            njuTextLogoImageView
        )
    }
}

object DownloadView {
    private val logger = LoggerFactory.getLogger(this::class.java)
}

object SettingView {
    private val logger = LoggerFactory.getLogger(this::class.java)
}

object AboutView {
    private val logger = LoggerFactory.getLogger(this::class.java)
}
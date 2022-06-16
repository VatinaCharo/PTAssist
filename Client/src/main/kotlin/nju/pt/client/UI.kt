package nju.pt.client

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory

object StartStage {
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

object MatchStage {
    private val logger = LoggerFactory.getLogger(this::class.java)
}

object DownloadStage {
    private val logger = LoggerFactory.getLogger(this::class.java)
}

object SettingStage {
    private val logger = LoggerFactory.getLogger(this::class.java)
}

object AboutStage {
    private val logger = LoggerFactory.getLogger(this::class.java)
}
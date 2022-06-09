package nju.pt.client

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

object UIContainer {
    val root = StackPane()
    val imageView = ImageView()
    val menuVBox = VBox()

    val startBtn = Button("开始比赛")
    val downloadBtn = Button("下载数据")
    val settingBtn = Button("设置")
    val aboutBtn = Button("关于软件")

    fun init() = apply {
        root.apply {
            children.addAll(imageView, menuVBox)
            menuVBox.children.addAll(startBtn, downloadBtn, settingBtn, aboutBtn)
        }
        imageView.apply {
            image = Image(R.MAIN_IMAGE_PATH)
        }
    }

    fun layout() = apply {
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
    }

    fun render() = apply {
        menuVBox.style = "-fx-background-color:#FAE3D955"
    }

    fun build(): StackPane {
        // TODO: 2022/6/9 @Eur3ka something to build last
        return root
    }
}
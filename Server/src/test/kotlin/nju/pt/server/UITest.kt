package nju.pt.server

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Dialog
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.stage.Stage
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.RecordData
import org.junit.jupiter.api.Test

class UITest {
    @Test
    fun mainViewTest() {
        class TestApp : Application() {
            override fun start(stage: Stage) {
                val tList = JsonHelper.fromJson<Data>(R.DATA_JSON_PATH).apply {
                    teamDataList[0].recordDataList.addAll(
                        mutableListOf(
                            RecordData(2, 1, 1, 9, 1, "nju.pt.net.R", 7.0, 3.0),
                            RecordData(3, 2, 3, 2, 1, "O", 8.0, 3.0)
                        )
                    )
                    teamDataList[1].recordDataList.addAll(
                        mutableListOf(
                            RecordData(2, 1, 1, 1, 3, "nju.pt.net.R", 7.0, 2.8),
                            RecordData(3, 1, 2, 2, 2, "V", 8.0, 2.8),
                        )
                    )

                }

                stage.apply {
                    val root = MainView.build(tList)
                    root.children.add(
                        Button("show").apply {
                            setOnAction {
                                println(tList)
                            }
                        }
                    )
                    scene = Scene(root).apply {
                        stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
                    }
                    icons.add(Image(R.LOGO_PATH))
                    title = "Match"
                    minWidth = 800.0
                    minHeight = 600.0
                }.show()
            }
        }
        Application.launch(TestApp::class.java)
    }

    @Test
    fun DialogTest(){
        class TestApp : Application() {

            val dialog =  ErrorAlert().apply {
                title = "test"
                headerText = "test"
                contentText = "content"
            }
            val Btn = Button("start").apply {
                setOnAction {
                    dialog.show()
                }
            }
            override fun start(primaryStage: Stage) {
                primaryStage.apply {
                    scene = MyScene(
                        VBox().apply {
                            children.add(Btn)
                        }
                    )
                }.show()
            }
        }
        Application.launch(TestApp::class.java)
    }
}
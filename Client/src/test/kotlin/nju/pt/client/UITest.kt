package nju.pt.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import nju.pt.R
import org.junit.jupiter.api.Test


class UITest {
    @Test
    fun matchViewTest() {
        class TestApp : Application() {
            override fun start(stage: Stage) {
                val qMap = mapOf<Int, String>()
                    .plus(1 to "Q1")
                    .plus(2 to "Q2")
                    .plus(3 to "Q3")
                    .plus(4 to "Q4")
                    .plus(5 to "Q5")
                    .plus(6 to "Q6")
                    .plus(7 to "Q7")
                    .plus(8 to "Q8")
                    .plus(9 to "Q9")
                    .plus(10 to "this is a question")
                    .plus(12 to "maybe not a question")
                    .plus(15 to "who knows")
                stage.apply {
                    scene = Scene(MatchView.build(5, qMap)).apply {
                        stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
                    }
                    icons.add(Image(R.LOGO_PATH))
                    title = "Match"
                }.show()
            }
        }
        Application.launch(TestApp::class.java)
    }
}
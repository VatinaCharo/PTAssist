package nju.pt.client

import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import nju.pt.R

class TeamBar(type: TeamType) : HBox() {
    private val tagLabel = Label()
    private val teamTextField = TextField().apply {
        prefWidth = 200.0
        isEditable = false
        tooltip = Tooltip("Team Name")
    }
    private val playerTextField = TextField().apply {
        prefWidth = 100.0
        tooltip = Tooltip("Player Name")
    }

    init {
        spacing = 10.0
        children.addAll(tagLabel, teamTextField)
        when (type) {
            TeamType.REPORTER -> {
                tagLabel.text = "正："
                children.add(playerTextField)
            }
            TeamType.OPPONENT -> {
                tagLabel.text = "反："
                children.add(playerTextField)
            }
            TeamType.REVIEWER -> {
                tagLabel.text = "评："
                children.add(playerTextField)
            }
            TeamType.OBSERVER -> {
                tagLabel.text = "观："
            }
        }
    }
}

class ScoreBar(name: String, judgeCount: Int) : HBox() {
    private val tagLabel = Label(name)

    init {
        spacing = 10.0
        children.add(tagLabel)
        children.addAll(
            (0 until judgeCount).map {
//                TextField("0").apply {
//                    prefWidth = 30.0
//                    alignment = Pos.CENTER
//                    textProperty().addListener { _, oldValue, newValue ->
//                        text = if (!newValue.matches(Regex("^\\d*$"))) oldValue else newValue
//                    }
//                }
                ComboBox<Int>().apply {
                    items.addAll(0..10)
                    prefWidth = 70.0
                }
            })
    }
}

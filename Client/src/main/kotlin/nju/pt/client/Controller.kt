package nju.pt.client

import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox

class TeamBar(val type: TeamType) : HBox() {
    private val tagLabel = Label()
    private val teamTextField = TextField().apply {
        prefWidth = 200.0
        isEditable = false
        tooltip = Tooltip("Team Name")
    }
    private val playerNameCB = ComboBox<String>()

    init {
        spacing = 10.0
        children.addAll(tagLabel, teamTextField)
        when (type) {
            TeamType.REPORTER -> {
                tagLabel.text = "正："
                children.add(playerNameCB)
            }
            TeamType.OPPONENT -> {
                tagLabel.text = "反："
                children.add(playerNameCB)
            }
            TeamType.REVIEWER -> {
                tagLabel.text = "评："
                children.add(playerNameCB)
            }
            TeamType.OBSERVER -> {
                tagLabel.text = "观："
            }
        }
    }

    fun loadTeam(teamName: String) {
        teamTextField.text = teamName
    }

    fun loadValidPlayer(playerNameList: List<String>) {
        playerNameCB.items = FXCollections.observableList(playerNameList)
    }
}

class ScoreBar(name: String, judgeCount: Int) : HBox() {
    private val tagLabel = Label(name)

    init {
        spacing = 10.0
        children.add(tagLabel)
        children.addAll(
            (0 until judgeCount).map {
                ComboBox<Int>().apply {
                    items.addAll(0..10)
                    prefWidth = 70.0
                }
            })
    }
}


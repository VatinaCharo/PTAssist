package nju.pt.server

import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.TeamData
import org.slf4j.LoggerFactory

class MainView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootVBox = VBox().apply { id = "MainView_rootVBox" }
    private val dataTabPane = TabPane().apply { id = "MainView_dataTabPane" }
    private val dataTab = Tab().apply { id = "MainView_dataTab" }
    private val dataHBox = HBox().apply { id = "MainView_dataHBox" }
    private val teamListView = ListView<String>()
    private val playerTableView = TableView<PlayerData>()
    private val playerIdTC = TableColumn<PlayerData, Number>("ID")
    private val playerNameTC = TableColumn<PlayerData, String>("姓名")
    private val playerGenderTC = TableColumn<PlayerData, String>("性别")
    private val recordTableView = TableView<TeamData>()

}
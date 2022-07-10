package nju.pt.server

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData
import nju.pt.databaseassist.Data
import org.slf4j.LoggerFactory

object MainView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootHBox = HBox().apply { id = "MainView_rootHBox" }
    private val informationVBox = VBox().apply { id = "MainView_informationVBox" }
    private val operationHBox = HBox().apply { id = "MainView_operationHBox" }
    private val modifyBtn = Button("修改").apply { id = "MainView_modifyBtn" }
    private val addBtn = Button("保存").apply { id = "MainView_saveBtn" }
    private val deleteBtn = Button("增减").apply { id = "MainView_adBtn" }
    private val exportBtn = Button("导出").apply { id = "MainView_exportBtn" }
    val teamListView = ListView<String>().apply { id = "MainView_teamListView" }
    private val playerTableView = TableView<PlayerData>().apply { id = "MainView_playerTableView" }
    private val playerIDTC = TableColumn<PlayerData, Number>("ID")
    private val playerNameTC = TableColumn<PlayerData, String>("姓名")
    private val playerGenderTC = TableColumn<PlayerData, String>("性别")
    private val recordTableView = TableView<RecordData>().apply { id = "MainView_recordTableView" }
    private val roomIDTC = TableColumn<RecordData, Number>("房间号")
    private val roundTC = TableColumn<RecordData, Number>("轮次")
    private val phaseTC = TableColumn<RecordData, Number>("阶段")
    private val questionIDTC = TableColumn<RecordData, Number>("题号")
    private val masterIDTC = TableColumn<RecordData, Number>("主控队员ID")
    private val roleTC = TableColumn<RecordData, String>("角色")
    private val scoreTC = TableColumn<RecordData, Number>("分数")
    private val weightTC = TableColumn<RecordData, Number>("系数")

    private fun init(data: Data) = apply {
        logger.info("init(data: Data)")
        rootHBox.apply {
            children.addAll(informationVBox, recordTableView)
            informationVBox.children.addAll(operationHBox, teamListView, playerTableView)
            operationHBox.children.addAll(modifyBtn, addBtn, deleteBtn, exportBtn)
            playerTableView.columns.addAll(
                playerIDTC,
                playerNameTC,
                playerGenderTC
            )
            recordTableView.columns.addAll(
                roundTC,
                phaseTC,
                roomIDTC,
                questionIDTC,
                masterIDTC,
                roleTC,
                scoreTC,
                weightTC
            )
        }
        playerIDTC.apply {
            cellValueFactory = PropertyValueFactory("id")
            setCellFactory {
                IntTabCell<PlayerData>()
            }
            setOnEditCommit {
                it.rowValue.id = it.newValue.toInt()
            }
        }
        playerNameTC.apply {
            cellValueFactory = PropertyValueFactory("name")
            cellFactory = TextFieldTableCell.forTableColumn()
            setOnEditCommit {
                it.rowValue.name = it.newValue
            }
        }
        playerGenderTC.apply {
            cellValueFactory = PropertyValueFactory("gender")
            cellFactory = TextFieldTableCell.forTableColumn()
            setOnEditCommit {
                it.rowValue.name = it.newValue
            }
        }
        roundTC.apply {
            cellValueFactory = PropertyValueFactory("round")
            setCellFactory {
                IntTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.round = it.newValue.toInt()
            }
        }
        phaseTC.apply {
            cellValueFactory = PropertyValueFactory("phase")
            setCellFactory {
                IntTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.phase = it.newValue.toInt()
            }
        }
        roomIDTC.apply {
            cellValueFactory = PropertyValueFactory("roomID")
            setCellFactory {
                IntTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.roomID = it.newValue.toInt()
            }
        }
        questionIDTC.apply {
            cellValueFactory = PropertyValueFactory("questionID")
            setCellFactory {
                IntTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.questionID = it.newValue.toInt()
            }
        }
        masterIDTC.apply {
            cellValueFactory = PropertyValueFactory("masterID")
            setCellFactory {
                IntTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.masterID = it.newValue.toInt()
            }
        }
        roleTC.apply {
            cellValueFactory = PropertyValueFactory("role")
            cellFactory = TextFieldTableCell.forTableColumn()
            setOnEditCommit {
                it.rowValue.role = it.newValue
            }
        }
        scoreTC.apply {
            cellValueFactory = PropertyValueFactory("score")
            setCellFactory {
                DoubleTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.score = it.newValue.toDouble()
            }
        }
        weightTC.apply {
            cellValueFactory = PropertyValueFactory("weight")
            setCellFactory {
                DoubleTabCell<RecordData>()
            }
            setOnEditCommit {
                it.rowValue.weight = it.newValue.toDouble()
            }
        }
        val teamNameList = data.teamDataList.map { it.name }
        teamListView.items.addAll(teamNameList)
        logger.info("load teamNameList $teamNameList")
        teamListView.selectionModel.select(0)
        logger.info("select 0 in teamListView")
        val playerDataList = data.teamDataList[0].playerDataList
        playerTableView.items = FXCollections.observableList(playerDataList)
        logger.info("load playerDataList $playerDataList")
        val recordDataList = data.teamDataList[0].recordDataList
        recordTableView.items = FXCollections.observableList(recordDataList)
        logger.info("load recordDataList $recordDataList")
        logger.info("init() return => $this")
    }

    private fun action() = apply {
        modifyBtn.setOnAction {
            playerTableView.isEditable = playerTableView.isEditable.not()
            recordTableView.isEditable = recordTableView.isEditable.not()
            rootHBox.background = Background(
                BackgroundFill(
                    if (playerTableView.isEditable) Paint.valueOf("#FF8953E0") else Paint.valueOf("#C7DAFF"),
                    CornerRadii.EMPTY, Insets.EMPTY
                )
            )
        }
    }

    private fun layout() = apply {
        logger.info("layout()")
        VBox.setVgrow(teamListView, Priority.ALWAYS)
        playerIDTC.prefWidthProperty().bind(playerTableView.widthProperty().multiply(0.2))
        playerNameTC.prefWidthProperty().bind(playerTableView.widthProperty().multiply(0.5))
        // 缩进5px 用于去除滚动条
        playerGenderTC.prefWidthProperty().bind(playerTableView.widthProperty().multiply(0.3).add(-5))
        HBox.setHgrow(recordTableView, Priority.ALWAYS)
        recordTableView.columns.forEach {
            it.prefWidthProperty().bind(recordTableView.widthProperty().multiply(0.1))
        }
        roomIDTC.prefWidthProperty().bind(recordTableView.widthProperty().multiply(0.15))
        masterIDTC.prefWidthProperty().bind(recordTableView.widthProperty().multiply(0.25).add(-10))
        logger.info("layout() return => $this")
    }

    fun loadData(playerDataList: List<PlayerData>, recordDataList: List<RecordData>) = apply {
        playerTableView.items = FXCollections.observableList(playerDataList)
        recordTableView.items = FXCollections.observableList(recordDataList)
    }

    fun build(data: Data): HBox {
        logger.info("build(data: Data)")
        logger.info("data >>> init: $data")
        init(data).action().layout()
        logger.info("build() return => $rootHBox")
        return rootHBox
    }
}
package nju.pt.server

import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.stage.Stage
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import nju.pt.R
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.TeamData
import org.apache.logging.log4j.message.FormattedMessageFactory
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelAttribute
import org.slf4j.LoggerFactory
import java.util.function.UnaryOperator
import kotlin.coroutines.suspendCoroutine

object StartView {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val rootStackPane = StackPane().apply { id = "StartView_rootStackPane" }
    val imageView = ImageView().apply { id = "StartView_imageView" }
    val menuVBox = VBox().apply { id = "StartView_menuVBox" }
    val generateTableBtn = Button("生成对阵表\n  (无裁判)").apply { id = "StartView_generateTableBtn" }
    val generateTableWithJudgeBtn = Button("生成对阵表\n  (有裁判)").apply { id = "StartView_generateTableWithJudgeBtn" }
    val startBtn = Button("进入比赛").apply { id = "StartView_startBtn" }
    val settingBtn = Button("设置").apply { id = "StartView_settingBtn" }
    val aboutBtn = Button("关于软件").apply { id = "StartView_aboutBtn" }

    val generateTableAlert = Alert(Alert.AlertType.ERROR).apply {
        title = "生成对阵表(无裁判)"
        headerText = "生成对阵表错误!"
        dialogPane.apply {
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }

    }
    val generateTableDialog = Dialog<ButtonType>().apply {
        title = "生成对阵表(无裁判)"
        headerText = "对阵表生成完成！"
        dialogPane.apply {
            buttonTypes.add(ButtonType.OK)
            lookupButton(ButtonType.OK)
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }

    }

    val startAlert = Alert(Alert.AlertType.ERROR).apply {
        title = "进入比赛"
        headerText = "Excel未准备完全，无法进入比赛!"
        dialogPane.apply {
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }
    }


    private fun init() = apply {
        logger.info("init()")
        rootStackPane.apply {
            children.addAll(imageView, menuVBox)
            menuVBox.children.addAll(generateTableBtn, generateTableWithJudgeBtn, startBtn, settingBtn, aboutBtn)
        }
        imageView.apply {
            image = Image(R.START_IMAGE_PATH)
        }
        logger.info("init() return => $this")

    }

    private fun layout() = apply {
        logger.info("layout()")
        imageView.apply {
            fitWidth = image.width
            fitHeight = image.height
        }
        rootStackPane.apply {
            alignment = Pos.CENTER
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


object MainView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootHBox = HBox().apply { id = "MainView_rootHBox" }
    private val informationVBox = VBox().apply { id = "MainView_informationVBox" }
    private val operationHBox = HBox().apply { id = "MainView_operationHBox" }
    private val modifyBtn = Button("修改").apply { id = "MainView_modifyBtn" }
    val saveBtn = Button("保存").apply { id = "MainView_saveBtn" }
    val addPlayerMenuItem = MenuItem("增加选手")
    val deletePlayerMenuItem = MenuItem("删除选手")
    val addRecordMenuItem = MenuItem("增加记录")
    val deleteRecordMenuItem = MenuItem("删除记录")

    private val addOrDeleteMenuBtn = MenuButton("增删").apply {
        items.addAll(
            addPlayerMenuItem, deletePlayerMenuItem, addRecordMenuItem, deleteRecordMenuItem
        )
    }
    val exportBtn = Button("导出").apply { id = "MainView_exportBtn" }
    val schoolListView = ListView<String>().apply { id = "MainView_schoolListView" }
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


    var selectedSchoolItems = SimpleListProperty(schoolListView.selectionModel.selectedItems)
    private fun init(data: Data) {
        logger.info("init(data: Data)")
        rootHBox.apply {
            children.addAll(informationVBox, recordTableView)
            informationVBox.children.addAll(operationHBox, schoolListView, teamListView, playerTableView)
            operationHBox.children.addAll(modifyBtn, saveBtn, addOrDeleteMenuBtn, exportBtn)
            playerTableView.columns.addAll(
                playerIDTC, playerNameTC, playerGenderTC
            )
            recordTableView.columns.addAll(
                roundTC, phaseTC, roomIDTC, questionIDTC, masterIDTC, roleTC, scoreTC, weightTC
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
        val schoolNameList = data.schoolMap.values
        schoolListView.apply {
            items.addAll(schoolNameList)
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            logger.info("load schoolNameList $schoolNameList")
        }

        val teamNameList = data.teamDataList.map { it.name }
        teamListView.items.addAll(teamNameList)
        logger.info("load teamNameList $teamNameList")

        val playerDataList = data.teamDataList[0].playerDataList
        playerTableView.items = FXCollections.observableList(playerDataList)
        logger.info("load playerDataList $playerDataList")
        val recordDataList = data.teamDataList[0].recordDataList
        recordTableView.items = FXCollections.observableList(recordDataList)
        logger.info("load recordDataList $recordDataList")
        logger.info("init() return => $this")
    }

    private fun action() {
        logger.info("action()")
        modifyBtn.setOnAction {
            playerTableView.isEditable = playerTableView.isEditable.not()
            recordTableView.isEditable = recordTableView.isEditable.not()
            rootHBox.background = Background(
                BackgroundFill(
                    if (playerTableView.isEditable) Paint.valueOf("#FF8953E0") else Paint.valueOf("#C7DAFF"),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                )
            )
        }

        schoolListView.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, EventHandler {
            if (it.button.name == MouseButton.SECONDARY.name) {
                schoolListView.selectionModel.clearSelection()
            }
        })


    }

    private fun layout() {
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
    }

    fun loadData(playerDataList: List<PlayerData>, recordDataList: List<RecordData>) = apply {
        playerTableView.items = FXCollections.observableList(playerDataList)
        recordTableView.items = FXCollections.observableList(recordDataList)
    }

    fun build(data: Data): HBox {
        logger.info("build(data: Data)")
        logger.info("init <<< data = $data")
        init(data)
        teamListView.selectionModel.select(0)
        logger.info("select 0 in teamListView")
        action()
        layout()
        logger.info("SelectedSchool:${selectedSchoolItems}")

        logger.info("build() return => $rootHBox")
        return rootHBox
    }

    fun refreshData(data: Data) {
        val playerDataList = data.teamDataList[0].playerDataList
        playerTableView.items = FXCollections.observableList(playerDataList)
        logger.info("load playerDataList $playerDataList")
        val recordDataList = data.teamDataList[0].recordDataList
        recordTableView.items = FXCollections.observableList(recordDataList)
    }
}

class ExportView() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    val rootVbox = VBox(15.0).apply { id = "ExportView_rootVbox" }
    val radioBtnHbox = VBox(30.0)
    val reviewTableRadioBtn = RadioButton("各轮回顾表").apply { isSelected = true }
    val teamScoreRadioBtn = RadioButton("各队伍总得分").apply { isSelected = true }
    val playerScoreRadioBtn = RadioButton("个人得分情况").apply { isSelected = true }
    val exportBtn = Button("导出")
    val checkBoxFlowPane = FlowPane().apply { id = "ExportView_checkBoxFlowPane" }

    val reviewTableTurnsCheckBoxList = mutableListOf<CheckBox>()

    private fun layout() {
        checkBoxFlowPane.apply {
            children.addAll(reviewTableTurnsCheckBoxList)
            checkBoxFlowPane.prefWidthProperty().bind(radioBtnHbox.widthProperty())
        }

        radioBtnHbox.apply {
            children.add(checkBoxFlowPane)
            children.add(reviewTableRadioBtn)
            children.add(teamScoreRadioBtn)
            children.add(playerScoreRadioBtn)
        }

        rootVbox.apply {
            children.addAll(radioBtnHbox, exportBtn)
            alignment = Pos.CENTER_RIGHT
        }
    }

    private fun action(data: Data) {
        exportBtn.setOnAction {
            logger.info("Exporting...")
            logger.info("Selected rounds: ${
                reviewTableTurnsCheckBoxList.filter { it.isSelected }.map { it.text.first() }
            }")
            val dataCopy = data.copy()

            dataCopy.teamDataList.forEach { teamData ->
                teamData.recordDataList = teamData.recordDataList.filter { recordData ->
                    reviewTableTurnsCheckBoxList.filter { it.isSelected }.map { "${it.text.first()}" }
                        .contains("${recordData.round}")
                }.toMutableList()
            }

            ExportExcel(dataCopy, R.SERVER_DATA_DIR_PATH).apply {
                if (reviewTableRadioBtn.isSelected) {
                    this.exportReviewTable()
                }
                if (teamScoreRadioBtn.isSelected) {
                    this.exportTeamScore()
                }
                if (playerScoreRadioBtn.isSelected) {
                    this.exportPlayerScore()
                }
            }
        }
    }

    fun build(data: Data): VBox {
        logger.info("build()")
        data.teamDataList.map { it.recordDataList.map { it.round } }.flatten().distinct().sorted().forEach { round ->
            reviewTableTurnsCheckBoxList.add(CheckBox("${round}轮").apply { isSelected = true })
        }
        layout()
        action(data)

        logger.info("build() return => $rootVbox")
        return rootVbox
    }

}


object AddOrDeleteView {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val schoolNameLabel = Label()
    val teamNameLabel = Label()
    val playerNameTextField = TextField()
    val playerGenderChoiceBox = ChoiceBox<String>().apply { items.addAll("女", "男");selectionModel.select(0) }
    val playerDeleteChoiceBox = ChoiceBox<String>()
    val recordRoundTextField = getIntegerTextField()
    val recordPhaseTextField = getIntegerTextField()
    val recordRoomIdTextField = getIntegerTextField()
    val recordQIdTextField = getIntegerTextField()
    val recordMasterIdTextField = getIntegerTextField()
    val recordRoleChoiceBox = ChoiceBox<String>().apply { items.addAll("R", "O", "V");selectionModel.select(0) }
    val recordScoreTextField = getDoubleTextField()
    val recordWeightTextField = getDoubleTextField()


    val addPlayerConfirmBtn = Button("确认添加")
    val deletePlayerConfirmBtn = Button("确认删除")
    val addRecordConfirmBtn = Button("确认添加")


    private val playerNameHbox = HBox(15.0).apply { children.addAll(Label("队员名"), playerNameTextField) }
    private val playerGenderHbox = HBox(15.0).apply { children.addAll(Label("队员性别"), playerGenderChoiceBox) }
    private val schoolInfoHbox = HBox(15.0).apply { children.addAll(Label("学校名"), schoolNameLabel) }
    private val teamInfoHbox = HBox(15.0).apply { children.addAll(Label("队伍名"), teamNameLabel) }
    private val playerChoiceHbox = HBox(15.0).apply { children.addAll(Label("选择删除队员:"), playerDeleteChoiceBox) }
    private val recordInfoFlowPane = FlowPane().apply {
        children.addAll(
            HBox(5.0).apply {
                children.addAll(
                    Label("轮次:"), recordRoundTextField
                )
            }, HBox(5.0).apply { children.addAll(Label("阶段"), recordPhaseTextField) },
            HBox(5.0).apply {
                children.addAll(
                    Label("会场Id"), recordRoomIdTextField
                )
            }, HBox(5.0).apply {
                children.addAll(
                    Label("题目Id"), recordQIdTextField,
                )
            }, HBox(5.0).apply {
                children.addAll(
                    Label("主控队员Id"), recordMasterIdTextField,
                )
            },
            HBox(5.0).apply {
                children.addAll(
                    Label("角色"), recordRoleChoiceBox
                )
            },
            HBox(5.0).apply {
                children.addAll(
                    Label("分数"), recordScoreTextField
                )
            }, HBox(5.0).apply {
                children.addAll(
                    Label("权重"), recordWeightTextField
                )
            })
    }

    val confirmDialog = Dialog<ButtonType>().apply {
        dialogPane.apply {
            buttonTypes.add(ButtonType.OK)
            lookupButton(ButtonType.OK)
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }

    }

    private fun getIntegerTextField() = TextField().apply {

        textProperty().addListener { _, oldValue, newValue ->
            text = if (newValue.matches(Regex("^\\d*\$"))) newValue else oldValue
        }
    }

    private fun getDoubleTextField() = TextField().apply {
        textProperty().addListener { _, oldValue, newValue ->
            text = if (newValue.matches(Regex("\\d*\\.\\d*")) || newValue.matches(Regex("^\\d*")) ) newValue else oldValue
        }
    }


    fun getAddPlayerStage(teamData: TeamData, schoolMap: Map<Int, String>) = Stage().apply {
        logger.info("add player stage")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")


        scene = Scene(VBox().apply {
            children.addAll(schoolInfoHbox, teamInfoHbox, playerNameHbox, playerGenderHbox, addPlayerConfirmBtn)
            alignment = Pos.CENTER_RIGHT
        }

        ).apply {
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
        }
    }

    fun getDeletePlayerStage(teamData: TeamData, schoolMap: Map<Int, String>) = Stage().apply {
        logger.info("delete player stage")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")

        playerDeleteChoiceBox.items.apply {
            clear()
            addAll(teamData.playerDataList.map { "${it.id}-${it.name}" })
        }

        scene = Scene(VBox().apply {
            children.addAll(schoolInfoHbox, teamInfoHbox, playerChoiceHbox, deletePlayerConfirmBtn)
            alignment = Pos.CENTER_RIGHT
        }).apply {
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
        }

    }

    fun getAddRecordStage(teamData: TeamData, schoolMap: Map<Int, String>) = Stage().apply {
        logger.info("add record")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")

        scene = Scene(VBox().apply {
            children.addAll(
                schoolInfoHbox, teamInfoHbox, recordInfoFlowPane, addRecordConfirmBtn
            )
            alignment = Pos.CENTER_RIGHT
        }).apply {
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
        }

    }

}
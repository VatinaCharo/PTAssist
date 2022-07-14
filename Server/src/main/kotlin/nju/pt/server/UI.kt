package nju.pt.server

import javafx.beans.DefaultProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
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

@DefaultProperty("root")
class MyScene(root: Parent) : Scene(root) {
    init {
        stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
    }
}

class MyStage() : Stage() {
    constructor(root: Parent) : this() {
        scene = MyScene(root)
    }

    init {
        icons.add(Image(R.LOGO_PATH))
    }
}

class IntegerTextField() : TextField() {
    constructor(text: String) : this() {
        this.text = text
    }

    init {
        textProperty().addListener { _, oldValue, newValue ->
            text = if (newValue.matches(Regex("^\\d*\$"))) newValue else oldValue
        }
        id = "IntegerTextField"
    }
}

class DoubleTextField() : TextField() {
    constructor(text: String) : this() {
        this.text = text
    }

    init {
        textProperty().addListener { _, oldValue, newValue ->
            text =
                if (newValue.matches(Regex("\\d*\\.\\d*")) || newValue.matches(Regex("^\\d*"))) newValue else oldValue
        }
        id = "DoubleTextField"
    }
}

class ConfirmDialog() : Dialog<ButtonType>() {
    init {
        dialogPane.apply {
            buttonTypes.add(ButtonType.OK)
            lookupButton(ButtonType.OK)
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }
    }
}

class ConfirmAlert() : Alert(AlertType.ERROR) {
    init {
        dialogPane.apply {
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------

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
        items.addAll(addPlayerMenuItem, deletePlayerMenuItem, addRecordMenuItem, deleteRecordMenuItem)
        id = "MainView_addOrDeleteMenuBtn"
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
        playerTableView.refresh()
        recordTableView.items = FXCollections.observableList(recordDataList)
        recordTableView.refresh()
    }

    fun refreshData(teamData: TeamData) {

        logger.info("Refresh data")
        logger.info("teamDataList:${teamData}")

        loadData(teamData.playerDataList, teamData.recordDataList)
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


}

class SettingView {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootGridPane = GridPane().apply { id = "SettingView_rootGridPane" }
    private val portLabel = Label("Port:")
    private val portTF = IntegerTextField().apply { id = "SettingView_portTF" }
    private val judgeCountLabel = Label("JudgeCount:")
    private val judgeCountTF = IntegerTextField()
    private val roomCountLabel = Label("RoomCount:")
    private val roomCountTF = IntegerTextField()
    private val turnLabel = Label("Turns:")
    private val turnCountTF = IntegerTextField()
    private val rWeightLabel = Label("Init R Weight:")
    private val rWeighTF = DoubleTextField()
    private val oWeightLabel = Label("Init R Weight:")
    private val oWeighTF = DoubleTextField()
    private val vWeightLabel = Label("Init R Weight:")
    private val vWeighTF = DoubleTextField()
    val saveBtn = Button("保存").apply { id = "SettingView_saveBtn" }

    private fun init(config: ConfigData) {
        logger.info("init(config: Config)")
        rootGridPane.apply {
            addRow(0, portLabel, portTF, judgeCountLabel, judgeCountTF)
            addRow(1, roomCountLabel, roomCountTF, turnLabel, turnCountTF)
            addRow(2, rWeightLabel, rWeighTF, oWeightLabel, oWeighTF)
            addRow(3, vWeightLabel, vWeighTF)
            add(saveBtn, 3, 4)
        }

        portTF.text = config.port.toString()
        judgeCountTF.text = config.judgeCount.toString()
        roomCountTF.text = config.roomCount.toString()
        turnCountTF.text = config.turns.toString()
        rWeighTF.text = config.rWeight.toString()
        oWeighTF.text = config.oWeight.toString()
        vWeighTF.text = config.vWeight.toString()

    }

    private fun action() {
        saveBtn.setOnAction {
            saveConfig()
        }
    }

    private fun layout() {
        logger.info("layout()")
        portTF.alignment = Pos.CENTER_RIGHT
        judgeCountTF.alignment = Pos.CENTER_RIGHT
    }

    private fun build(config: ConfigData): GridPane {
        logger.info("build(config:Config)")
        init(config)
        layout()
        action()
        logger.info("build() return => $rootGridPane")
        return rootGridPane
    }

    fun getSettingViewStage(config: ConfigData) = MyStage(build(config)).apply {
        title = "服务端设置"
        isResizable = false
    }

    private fun checkModifyConfig(): Boolean {
        logger.info("Check Config")
        if (portTF.text.isNotEmpty() && judgeCountTF.text.isNotEmpty() && roomCountTF.text.isNotEmpty() && turnCountTF.text.isNotEmpty() && rWeighTF.text.isNotEmpty() && oWeighTF.text.isNotEmpty() && vWeighTF.text.isNotEmpty()) {
            logger.info("No Error")
            ConfirmDialog().apply {
                title = "配置服务端"
                contentText = "服务端配置成功!"
            }.show()
            return true
        }
        ConfirmAlert().apply {
            title = "配置服务端"
            headerText = "服务端配置失败!"
            contentText = "Error: 服务端配置信息不得有空"
        }.show()
        logger.info("Error: 服务端配置信息不得有空")
        return false
    }

    private fun saveConfig() {
        if (checkModifyConfig()){
            ConfigData(
                portTF.text.toInt(),
                judgeCountTF.text.toInt(),
                roomCountTF.text.toInt(),
                turnCountTF.text.toInt(),
                rWeighTF.text.toDouble(),
                oWeighTF.text.toDouble(),
                vWeighTF.text.toDouble()
            ).let {
                Config.writeIntoConfig(it)
            }
        }

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
    val playerGenderComboBox = ComboBox<String>().apply {
        items.addAll("女", "男");selectionModel.select(0);id = "AddOrDeleteView_playerGenderChoiceBox"
    }
    val playerDeleteComboBox = ComboBox<String>().apply { id = "AddOrDeleteView_playerDeleteComboBox" }
    val recordRoundTextField = IntegerTextField()
    val recordPhaseTextField = IntegerTextField()
    val recordRoomIdTextField = IntegerTextField()
    val recordQIdTextField = IntegerTextField()
    val recordMasterIdTextField = IntegerTextField()
    val recordRoleComboBox = ComboBox<String>().apply { items.addAll("R", "O", "V");selectionModel.select(0) }
    val recordScoreTextField = DoubleTextField()
    val recordWeightTextField = DoubleTextField()
    val recordComboBox = ComboBox<String>().apply { id = "AddOrDeleteView_recordComboBox" }

    val addPlayerConfirmBtn = Button("确认添加")
    val deletePlayerConfirmBtn = Button("确认删除")
    val addRecordConfirmBtn = Button("确认添加")
    val deleteRecordConfirmBtn = Button("确认删除")

    private val playerNameHbox = HBox(15.0).apply { children.addAll(Label("选手名"), playerNameTextField) }
    private val playerGenderHbox = HBox(15.0).apply { children.addAll(Label("选手性别"), playerGenderComboBox) }
    private val schoolInfoHbox = HBox(15.0).apply { children.addAll(Label("学校名"), schoolNameLabel) }
    private val teamInfoHbox = HBox(15.0).apply { children.addAll(Label("队伍名"), teamNameLabel) }
    private val playerChoiceHbox = HBox(15.0).apply { children.addAll(Label("选择删除选手:"), playerDeleteComboBox) }
    private val recordInfoFlowPane = FlowPane().apply {
        id = "AddOrDeleteView_recordInfoFlowPane"
        children.addAll(HBox(5.0).apply {
            children.addAll(
                Label("轮次:"), recordRoundTextField
            )
        }, HBox(5.0).apply { children.addAll(Label("阶段"), recordPhaseTextField) }, HBox(5.0).apply {
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
        }, HBox(5.0).apply {
            children.addAll(
                Label("角色"), recordRoleComboBox
            )
        }, HBox(5.0).apply {
            children.addAll(
                Label("分数"), recordScoreTextField
            )
        }, HBox(5.0).apply {
            children.addAll(
                Label("权重"), recordWeightTextField
            )
        })
    }
    private val recordChoiceVbox = VBox(5.0).apply {
        children.addAll(Label("选择一条记录:"), recordComboBox)

    }

    val addPlayerStage = MyStage().apply { title = "增加选手" }
    val deletePlayerStage = MyStage().apply { title = "删除选手" }
    val addRecordStage = MyStage().apply { title = "增加记录" }
    val deleteRecordStage = MyStage().apply { title = "删除记录" }


    fun checkAddPlayer(): Boolean {
        logger.info("Checking Add Player")
        if (playerNameTextField.text.isEmpty()) {
            logger.error("Error:选手姓名不得为空")
            ConfirmAlert().apply {
                title = "增加选手"
                headerText = "增加选手失败!"
                contentText = "Error:选手姓名不得为空!"
            }.show()
            return false
        } else {
            logger.info("No Error")
            return true
        }

    }

    fun checkAddRecord(): Boolean {
        logger.info("Checking Add Record")
        if (recordRoundTextField.text.isNotEmpty() && recordPhaseTextField.text.isNotEmpty() && recordRoomIdTextField.text.isNotEmpty() && recordMasterIdTextField.text.isNotEmpty() && recordQIdTextField.text.isNotEmpty() && recordScoreTextField.text.isNotEmpty() && recordWeightTextField.text.isNotEmpty()) {
            return true
        } else {
            ConfirmAlert().apply {
                title = "增加记录"
                headerText = "增加记录失败!"
                contentText = "Error:增加记录框内不得空!"
            }.show()
            logger.error("Error:增加记录框内不得空!")
            return false
        }

    }


    fun getAddPlayerStage(teamData: TeamData, schoolMap: Map<Int, String>) = addPlayerStage.apply {
        logger.info("add player stage")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")

        scene = MyScene(VBox(10.0).apply {
            children.addAll(schoolInfoHbox, teamInfoHbox, playerNameHbox, playerGenderHbox, addPlayerConfirmBtn)
            alignment = Pos.CENTER_RIGHT
            id = "AddOrDeleteView_Layout"
        })
        width = 240.0
        isResizable = false

    }

    fun getDeletePlayerStage(teamData: TeamData, schoolMap: Map<Int, String>) = deletePlayerStage.apply {
        logger.info("delete player stage")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")

        playerDeleteComboBox.items.apply {
            clear()
            addAll(teamData.playerDataList.map { "${it.id}-${it.name}" })
        }
        playerDeleteComboBox.selectionModel.select(0)

        scene = MyScene(VBox(10.0).apply {
            children.addAll(schoolInfoHbox, teamInfoHbox, playerChoiceHbox, deletePlayerConfirmBtn)
            alignment = Pos.CENTER_RIGHT
            id = "AddOrDeleteView_Layout"
        })
        isResizable = false

    }

    fun getAddRecordStage(teamData: TeamData, schoolMap: Map<Int, String>) = addRecordStage.apply {
        logger.info("add record")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")

        scene = MyScene(VBox(10.0).apply {
            children.addAll(
                schoolInfoHbox, teamInfoHbox, recordInfoFlowPane, addRecordConfirmBtn
            )
            alignment = Pos.CENTER_RIGHT
            id = "AddOrDeleteView_Layout"
        })
        width = 509.0
        isResizable = false
    }

    fun getDeleteRecordStage(teamData: TeamData, schoolMap: Map<Int, String>) = deleteRecordStage.apply {
        logger.info("delete record")
        schoolNameLabel.text = "${teamData.schoolID}-${schoolMap[teamData.schoolID]}"
        teamNameLabel.text = teamData.name
        logger.info("schoolName:${schoolNameLabel.text}")
        logger.info("teamName:${teamNameLabel.text}")

        recordComboBox.items.clear()
        teamData.recordDataList.forEachIndexed { index, recordData ->
            recordComboBox.items.add("${index + 1}-${recordData.round}轮,${recordData.phase}阶段,${recordData.roomID}会场->题${recordData.questionID},主控ID ${recordData.masterID},${recordData.role}")
            logger.info("recordData${index + 1}:${recordData}")
        }
        recordComboBox.selectionModel.select(0)

        scene = MyScene(VBox(10.0).apply {
            children.addAll(schoolInfoHbox, teamInfoHbox, recordChoiceVbox, deleteRecordConfirmBtn)
            alignment = Pos.CENTER_RIGHT
            id = "AddOrDeleteView_Layout"
        })
        isResizable = false


    }

}
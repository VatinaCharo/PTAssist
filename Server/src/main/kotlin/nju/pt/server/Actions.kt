package nju.pt.server

import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Paint
import javafx.stage.FileChooser
import nju.pt.R
import nju.pt.databaseassist.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists

object StartViewActions {
    private val logger = LoggerFactory.getLogger("StartViewActions Logger")

    private fun generateTable(totalTeamNumber: Int, judgeMap: Map<String, List<String>>, schoolMap: Map<Int, String>) {
        try {
            CounterPartTable(totalTeamNumber, judgeMap, schoolMap).generateTableWithoutJudge()
        } catch (e: Exception) {
            logger.error(e.message)
            ErrorAlert().apply {
                title = "生成对阵表(无裁判)"
                headerText = "生成对阵表错误!"
                contentText = "Error:${e.message}"
            }.show()
            throw Exception(e.message)
        }
        InfoAlert().apply {
            title = "生成对阵表(无裁判)"
            headerText = "对阵表生成完成！"
            contentText = "对阵表缓存文件保存至${R.COUNTERPART_TABLE_JSON_PATH}\n对阵表生成至${R.COUNTERPART_TABLE_EXCEL_PATH}"

        }.show()
    }

    fun generateTableBtnAction(totalTeamNumber: Int, judgeMap: Map<String, List<String>>, schoolMap: Map<Int, String>) {
        if (Path(R.COUNTERPART_TABLE_JSON_PATH).exists()) {
            ConfirmAlert().apply {
                title = "生成对阵表(无裁判)"
                headerText = "已有对阵表，是否生成并覆盖？"
                yesBtn.setOnAction {
                    generateTable(totalTeamNumber, judgeMap, schoolMap)
                    this.close()
                }
            }.show()

        } else {
            generateTable(totalTeamNumber, judgeMap, schoolMap)
        }
    }

    fun generateTableWithJudgeBtnAction(
        totalTeamNumber: Int, judgeMap: Map<String, List<String>>, schoolMap: Map<Int, String>
    ) {
        try {
            CounterPartTable(totalTeamNumber, judgeMap, schoolMap).generateTableWithJudge()
        } catch (e: FileNotFoundException) {
            logger.error("未找到${R.COUNTERPART_TABLE_JSON_PATH}文件，请先生成无裁判对阵表")
            ErrorAlert().apply {
                title = "生成对阵表(有裁判)"
                headerText = "生成对阵表(有裁判)错误！"
                contentText = "Error:未找到${R.COUNTERPART_TABLE_JSON_PATH}文件，请先生成无裁判对阵表！"
            }.show()

            throw Exception("未找到${R.COUNTERPART_TABLE_JSON_PATH}文件，请先生成无裁判对阵表！")
        } catch (e: Exception) {
            logger.error(e.message)
            ErrorAlert().apply {
                title = "生成对阵表(有裁判)"
                headerText = "生成对阵表(有裁判)错误！"
                contentText = "Error:${e.message}"
            }.show()
            throw Exception(e.message)

        }
        InfoAlert().apply {
            headerText = "对阵表生成完成！"
            title = "生成对阵表(有裁判)"
            contentText = "对阵表生成至${R.COUNTERPART_TABLE_EXCEL_PATH}"
        }.show()
    }

    fun settingBtnAction() {
        SettingView.getSettingViewStage(Config.configData).show()
    }


}

object SettingViewActions {
    private val logger = LoggerFactory.getLogger("SettingViewActions Logger")

    private fun checkModifyConfig(): Boolean {
        logger.info("Check Config")
        if (SettingView.portTF.text.isNotEmpty() && SettingView.judgeCountTF.text.isNotEmpty() && SettingView.roomCountTF.text.isNotEmpty() && SettingView.turnCountTF.text.isNotEmpty() && SettingView.rWeighTF.text.isNotEmpty() && SettingView.oWeighTF.text.isNotEmpty() && SettingView.vWeighTF.text.isNotEmpty()) {
            logger.info("No Error")
            InfoAlert().apply {
                title = "配置服务端"
                headerText = "服务端配置成功!"
                contentText = "服务端配置成功!"
            }.show()
            return true
        }
        ErrorAlert().apply {
            title = "配置服务端"
            headerText = "服务端配置失败!"
            contentText = "Error: 服务端配置信息不得有空"
        }.show()
        logger.info("Error: 服务端配置信息不得有空")
        return false
    }

    fun saveConfigBtnAction() {
        if (checkModifyConfig()) {
            ConfigData(
                SettingView.portTF.text.toInt(),
                SettingView.judgeCountTF.text.toInt(),
                SettingView.roomCountTF.text.toInt(),
                SettingView.turnCountTF.text.toInt(),
                SettingView.rWeighTF.text.toDouble(),
                SettingView.oWeighTF.text.toDouble(),
                SettingView.vWeighTF.text.toDouble()
            ).let {
                Config.writeIntoConfig(it)
            }
        }
    }
}


object MainViewActions {
    private val logger = LoggerFactory.getLogger("MainViewActions Logger")

    fun modifyBtnAction() {
        MainView.playerTableView.isEditable = MainView.playerTableView.isEditable.not()
        MainView.recordTableView.isEditable = MainView.recordTableView.isEditable.not()
        MainView.rootHBox.background = Background(
            BackgroundFill(
                if (MainView.playerTableView.isEditable) Paint.valueOf("#FF8953E0") else Paint.valueOf("#C7DAFF"),
                CornerRadii.EMPTY,
                Insets.EMPTY
            )
        )
    }

    fun saveBtnAction(data: Data) {
        logger.info("Save Data")
        JsonHelper.toJson(data, R.DATA_JSON_PATH)
        InfoAlert().apply {
            title = "保存"
            headerText = "保存成功！"
            contentText = "数据成功保存至${R.DATA_JSON_PATH}"
        }.show()
    }

    fun addPlayerMenuItemAction(selectedTeamData: TeamData, schoolMap: Map<Int, String>) {
        AddOrDeleteView.getAddPlayerStage(selectedTeamData, schoolMap).show()
    }

    fun deletePlayerMenuItemAction(selectedTeamData: TeamData, schoolMap: Map<Int, String>) {
        AddOrDeleteView.getDeletePlayerStage(selectedTeamData, schoolMap).show()
    }

    fun addRecordMenuItemAction(selectedTeamData: TeamData, schoolMap: Map<Int, String>,questionList:List<Int>) {
        AddOrDeleteView.getAddRecordStage(selectedTeamData, schoolMap,questionList).show()
    }

    fun deleteRecordMenuItemAction(selectedTeamData: TeamData, schoolMap: Map<Int, String>) {
        AddOrDeleteView.getDeleteRecordStage(selectedTeamData, schoolMap).show()
    }

    fun generateRoomDataBtnAction(maxTurn: Int) {
        logger.info("generate room data")
        GenerateRoomDataView.getGenerateRoomDataStage(
            if (maxTurn == Config.turns) Config.turns else maxTurn + 1
        ).show()
    }

    fun addDataFromJsonBtnAction(data: Data) {
        try {
            FileChooser().apply {
                initialDirectory = File(".")
                extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Json File", "*.json"),
                    FileChooser.ExtensionFilter("All File", "*.*")
                )
            }.run {
                showOpenMultipleDialog(MyStage())
            }.forEach {

                it.copyTo(File("${R.SERVER_BACKUP_FILE_DIR_PATH}/${it.name}"), true)
                data.mergeData(JsonHelper.fromJson<Data>(it.path), inplace = true)
                MainView.refreshData(data.teamDataList[0])
                it.delete()

            }

            InfoAlert().apply {
                title = "加载数据"
                headerText = "加载数据成功！"
            }.show()

        } catch (e: java.lang.NullPointerException) {
            logger.error("未选择文件！")
            logger.error(e.message)

        }
    }
}


object AddOrDeleteViewActions {
    private val logger = LoggerFactory.getLogger("AddOrDeleteActions Logger")

    private fun checkAddPlayer(): Boolean {
        logger.info("Checking Add Player")
        return if (AddOrDeleteView.playerNameTextField.text.isEmpty()) {
            logger.error("Error:选手姓名不得为空")
            ErrorAlert().apply {
                title = "增加选手"
                headerText = "增加选手失败!"
                contentText = "Error:选手姓名不得为空!"
            }.show()
            false
        } else {
            logger.info("No Error")
            true
        }

    }

    fun addPlayerConfirmBtnAction(data: Data, selectedTeamData: TeamData) {
        if (checkAddPlayer()) {
            data.teamDataList.first {
                it.name == AddOrDeleteView.teamNameLabel.text && "${it.schoolID}" == AddOrDeleteView.schoolNameLabel.text.substringBefore(
                    "-"
                )
            }.playerDataList.add(
                PlayerData(
                    data.getMaxPlayerId() + 1,
                    AddOrDeleteView.playerNameTextField.text,
                    AddOrDeleteView.playerGenderComboBox.selectionModel.selectedItem
                )
            )
            logger.info("Player info:")
            logger.info("schoolName:${AddOrDeleteView.schoolNameLabel.text}")
            logger.info("teamName:${AddOrDeleteView.teamNameLabel.text}")
            logger.info("Id: ${data.getMaxPlayerId()}")
            logger.info(("Name: ${AddOrDeleteView.playerNameTextField.text}"))
            logger.info("Gender: ${AddOrDeleteView.playerGenderComboBox.selectionModel.selectedItem}")
            logger.info("Player added successfully!")

            InfoAlert().apply {
                title = "增加选手"
                headerText = "增加选手成功"
                contentText = "增加选手${AddOrDeleteView.playerNameTextField.text}成功!"
                setOnCloseRequest { AddOrDeleteView.addPlayerStage.close() }
            }.show()
            MainView.refreshData(selectedTeamData)
        }
    }

    fun deletePlayerConfirmBtnAction(data: Data, selectedTeamData: TeamData) {
        data.teamDataList.filter {
            it.name == AddOrDeleteView.teamNameLabel.text && "${it.schoolID}" == AddOrDeleteView.schoolNameLabel.text.substringBefore(
                "-"
            )
        }[0].playerDataList.removeIf {
            "${it.id}" == AddOrDeleteView.playerDeleteComboBox.selectionModel.selectedItem.substringBefore("-")
        }
        InfoAlert().apply {
            title = "删除选手"
            headerText = "删除选手成功"
            contentText = "删除选手${AddOrDeleteView.playerDeleteComboBox.selectionModel.selectedItem}成功!"
            setOnCloseRequest { AddOrDeleteView.deletePlayerStage.close() }
        }.show()
        MainView.refreshData(selectedTeamData)
    }

    private fun checkAddRecord(): Boolean {
        logger.info("Checking Add Record")
        return if (  AddOrDeleteView.recordRoomIdTextField.text.isNotEmpty()   && AddOrDeleteView.recordScoreTextField.text.isNotEmpty() && AddOrDeleteView.recordWeightTextField.text.isNotEmpty()) {
            true
        } else {
            ErrorAlert().apply {
                title = "增加记录"
                headerText = "增加记录失败!"
                contentText = "Error:增加记录框内不得空!"
            }.show()
            logger.error("Error:增加记录框内不得空!")
            false
        }

    }

    fun addRecordConfirmBtnAction(data: Data, selectedTeamData: TeamData) {
        if (checkAddRecord()) {
            data.teamDataList.filter {
                it.name == AddOrDeleteView.teamNameLabel.text && "${it.schoolID}" == AddOrDeleteView.schoolNameLabel.text.substringBefore(
                    "-"
                )
            }[0].recordDataList.add(
                RecordData(
                    AddOrDeleteView.recordRoundComboBox.selectionModel.selectedItem,
                    AddOrDeleteView.recordPhaseComboBox.selectionModel.selectedItem,
                    AddOrDeleteView.recordRoomIdTextField.text.toInt(),
                    AddOrDeleteView.recordQIdComboBox.selectionModel.selectedItem,
                    AddOrDeleteView.recordMasterIdComboBox.selectionModel.selectedItem,
                    AddOrDeleteView.recordRoleComboBox.selectionModel.selectedItem,
                    AddOrDeleteView.recordScoreTextField.text.toDouble(),
                    AddOrDeleteView.recordWeightTextField.text.toDouble()
                )
            )
            InfoAlert().apply {
                title = "增加记录"
                headerText = "增加记录成功!"
                setOnCloseRequest { AddOrDeleteView.addRecordStage.close() }
            }.show()
            MainView.refreshData(selectedTeamData)
        }
    }

    fun deleteRecordConfirmBtnAction(data: Data, selectedTeamData: TeamData) {
        data.teamDataList.filter {
            it.name == AddOrDeleteView.teamNameLabel.text && "${it.schoolID}" == AddOrDeleteView.schoolNameLabel.text.substringBefore(
                "-"
            )
        }[0].recordDataList.removeAt(AddOrDeleteView.recordComboBox.selectionModel.selectedIndex)
        InfoAlert().apply {
            title = "删除记录"
            contentText = "删除记录成功!"
            setOnCloseRequest { AddOrDeleteView.deleteRecordStage.close() }
        }.show()
        MainView.refreshData(selectedTeamData)
    }
}

object ExportViewActions {
    private val logger = LoggerFactory.getLogger("ExportViewActions Logger")

    fun exportBtnAction(data: Data) {
        logger.info("Exporting...")
        logger.info("Selected rounds: ${
            ExportView.reviewTableTurnsCheckBoxList.filter { it.isSelected }.map { it.text.first() }
        }")
        val dataCopy = data.copy()
        logger.info("dataCopy:${dataCopy}")

        dataCopy.teamDataList.forEach { teamData ->
            teamData.recordDataList = teamData.recordDataList.filter { recordData ->
                ExportView.reviewTableTurnsCheckBoxList.filter { it.isSelected }.map { "${it.text.first()}" }
                    .contains("${recordData.round}")
            }.toMutableList().also {
                logger.info("selected teamData:${it}")
            }
        }

        logger.info("datCopy teamList:${dataCopy}")

        ExportExcel(dataCopy, R.SERVER_DATA_DIR_PATH).apply {
            if (ExportView.reviewTableRadioBtn.isSelected) {
                this.exportReviewTable()
            }
            if (ExportView.teamScoreRadioBtn.isSelected) {
                this.exportTeamScore()
            }
            if (ExportView.playerScoreRadioBtn.isSelected) {
                this.exportPlayerScore()
            }

            this.savePath.let {
                InfoAlert().apply {
                    title = "导出数据"
                    headerText = "导出数据成功！"
                    contentText = "数据成功导出至${it}"
                }.show()
            }

        }
    }
}


object GenerateRoomDataViewActions {
    private val logger = LoggerFactory.getLogger("GenerateRoomDataViewActions Logger")


    fun generateRoomDataConfirmBtnAction(data: Data) {
        logger.info("Generate room data")
        val selectedTurn = GenerateRoomDataView.turnSelectComboBox.selectionModel.selectedIndex + 1
        logger.info("selected turn:${selectedTurn}")
        val dataCopy = data.copy()
        dataCopy.teamDataList.forEach { teamData ->
            if (teamData.recordDataList.isNotEmpty()) {
                teamData.recordDataList = teamData.recordDataList.filter {
                    it.round < selectedTurn
                }.toMutableList()
            }

        }
        logger.info("teamDataList:${data.teamDataList}")

        Path(R.SERVER_SEND_FILE_DIR_PATH).apply {
            if (this.notExists()) {
                this.createDirectories()
                logger.info("Directory created")
            }
        }
        val counterPartTable = JsonHelper.fromJson<CounterPartTable>(R.COUNTERPART_TABLE_JSON_PATH)

        for (roomId in 1..Config.roomCount) {
            logger.info("Room $roomId:")

            val thisRoomTeamIdList = mutableListOf<Int>().apply {
                counterPartTable.teamTableList[selectedTurn - 1].let {
                    this.add(it.RList[roomId - 1])
                    this.add(it.OList[roomId - 1])
                    this.add(it.VList[roomId - 1])
                    if (it.OBList[roomId - 1] != -1) {
                        this.add(it.OBList[roomId - 1])
                    }
                }
            }
            logger.info("teamIdList:$thisRoomTeamIdList")
            val dataCopyTemp = dataCopy.copy()
            dataCopyTemp.teamDataList = mutableListOf<TeamData>().apply {
                for (teamId in thisRoomTeamIdList) {
                    this.add(dataCopy.teamDataList.first { it.id == teamId })
                }
            }

            logger.info("teamDataList:${dataCopyTemp.teamDataList}")
            Path("${R.SERVER_SEND_FILE_DIR_PATH}/Round${selectedTurn}").apply {
                if (this.notExists()) {
                    this.createDirectories()
                }
            }
            JsonHelper.toJson(dataCopyTemp, "${R.SERVER_SEND_FILE_DIR_PATH}/Round${selectedTurn}/Room${roomId}.json")

        }
        InfoAlert().apply {
            title = "生成分会场数据"
            headerText = "生成分会场数据成功！"
            contentText = "第${selectedTurn}轮数据成功保存至${R.SERVER_SEND_FILE_DIR_PATH}/Round${selectedTurn}"
        }.show()
    }
}
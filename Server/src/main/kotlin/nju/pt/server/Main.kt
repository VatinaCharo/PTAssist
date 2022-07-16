package nju.pt.server

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData
import nju.pt.kotlin.ext.*
import nju.pt.net.FileNetServer
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

fun main() {
    Application.launch(AppUI::class.java)
}


class AppUI : Application() {
    private val logger = LoggerFactory.getLogger(AppUI::class.java)

    private var data = Json.decodeFromString<Data>(R.DATA_JSON_EXAMPLE)
    private val totalTeamNumber: Int by lazy { WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getTotalTeamNumber() }
    private val judgeMap: Map<String, List<String>> by lazy {
        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadJudgeFromExcel()
    }
    private val schoolMap: Map<Int, String> by lazy {
        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadSchoolFromExcel()
    }
    private lateinit var fileNetServer: FileNetServer

    private fun getExportSettingStage(data: Data) = MyStage(ExportView().build(data.copy())).apply {
        minWidth = 150.0
        minHeight = 300.0
        isResizable = false
        title = "导出内容设置"
    }

    private fun getSelectedTeamData(selectedTeamName: String? = MainView.teamListView.selectionModel.selectedItem) =
        data.teamDataList.first {
            //当学校选择为空，则默认显示全部的学校，此时不需要学校的判定
            when (MainView.selectedSchoolItems.size == 0) {
                true -> it.name == selectedTeamName
                false -> it.name == selectedTeamName && data.schoolMap[it.schoolID] in MainView.selectedSchoolItems
            }
        }


    override fun init() {
        logger.info("init()")
        for (path in listOf(
            R.SERVER_CACHE_DIR_PATH,
            R.SERVER_SEND_FILE_DIR_PATH,
            R.SERVER_BACKUP_FILE_DIR_PATH,
            R.SERVER_ACCEPT_FILE_TEMP_DIR_PATH
        )) {
            Path(path).apply {
                if (this.notExists()) {
                    this.createDirectories()
                }
            }
        }

        logger.info("checking configuration file...")
        // 配置文件json找不到
        if (Path(R.CONFIG_JSON_PATH).notExists()) {
            Path(R.CONFIG_EXCEL_PATH).apply {
                //配置文件excel找不到
                if (this.notExists()) {
                    if (this.parent.notExists()) {
                        //Data文件夹都没有
                        this.parent.createDirectory()
                    }
                    XSSFWorkbook().initializeExcel()
                    logger.error("已新建配置文件，请先在${R.SERVER_DATA_DIR_PATH}中配置服务端配置文件再进入程序！")
                    throw Exception("已新建配置文件，请在${R.SERVER_DATA_DIR_PATH}中配置服务端配置文件再进入程序！")
                } else {
                    WorkbookFactory.create(R.CONFIG_EXCEL_FILE).apply {
                        checkConfigExcel()
                    }
                    //根据excel生成config_data.json
                    Config.configData
                }
            }
        }

        fileNetServer = FileNetServer(Config.port, FileRouter())
        // 启动文件接收线程
        fileNetServer.service().start()
    }

    override fun start(primaryStage: Stage) {
        primaryStage.apply {

            scene = MyScene(StartView.build())

            icons.add(Image(R.LOGO_PATH))
            title = "Match-Server ${R.VERSION}"
        }.show()

        // TODO: 2022/7/11 新建了Alert/Dialog提示框，但是还没有做UI
        StartView.apply {
            //生成对阵表按钮
            generateTableBtn.setOnAction {
                try {
                    CounterPartTable(totalTeamNumber, judgeMap, schoolMap).generateTableWithoutJudge()
                } catch (e: Exception) {
                    logger.error(e.message)
                    ConfirmAlert().apply {
                        title = "生成对阵表(无裁判)"
                        headerText = "生成对阵表错误!"
                        contentText = "Error:${e.message}"
                    }.show()
                    throw Exception(e.message)
                }
                generateTableDialog.show()

            }
            //生成有裁判的对阵表按钮
            generateTableWithJudgeBtn.setOnAction {
                try {
                    CounterPartTable(totalTeamNumber, judgeMap, schoolMap).generateTableWithJudge()
                } catch (e: FileNotFoundException) {
                    logger.error("未找到${R.COUNTERPART_TABLE_JSON_PATH}文件，请先生成无裁判对阵表")
                    ConfirmAlert().apply {
                        title = "生成对阵表(有裁判)"
                        headerText = "生成对阵表(有裁判)错误！"
                        contentText = "Error:未找到${R.COUNTERPART_TABLE_JSON_PATH}文件，请先生成无裁判对阵表！"
                    }.show()

                    throw Exception("未找到${R.COUNTERPART_TABLE_JSON_PATH}文件，请先生成无裁判对阵表！")
                } catch (e: Exception) {
                    logger.error(e.message)
                    ConfirmAlert().apply {
                        title = "生成对阵表(有裁判)"
                        headerText = "生成对阵表(有裁判)错误！"
                        contentText = "Error:${e.message}"
                    }.show()
                    throw Exception(e.message)

                }
                ConfirmDialog().apply {
                    headerText = "对阵表生成完成！"
                    title = "生成对阵表(有裁判)"
                }.show()
            }


            //设置界面
            settingBtn.setOnAction {
                SettingView().getSettingViewStage(Config.configData).show()
            }

            //进入比赛按钮
            startBtn.setOnAction {
                try {
                    //加载数据
                    logger.info("Judging whether the json file exists:")
                    if (Path(R.DATA_JSON_PATH).notExists()) {
                        logger.info("Not exists, loading data from excel...")
                        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).initializeJson()
                    } else {
                        logger.info("Exists, loading json file...")
                    }
                    data = JsonHelper.fromJson<Data>(R.DATA_JSON_PATH)

                    //设置stage
                    primaryStage.apply {

                        scene = MyScene(MainView.build(data))
                        minWidth = 800.0
                        minHeight = 800.0

                        setOnCloseRequest { _ ->
                            Alert(Alert.AlertType.CONFIRMATION).apply {
                                dialogPane.apply {
                                    (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
                                }
                                title = "退出"
                                headerText = "是否保存?"
                                if (this.showAndWait().get() == ButtonType.OK) {
                                    MainView.saveBtn.fire()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e.message)
                    startAlert.apply {
                        contentText = "Error:${e.message}"
                    }.show()
                    throw Exception(e.message)
                }


                MainView.apply {
                    teamListView.selectionModel.selectedItemProperty().addListener { _, _, newSelectedTeamName ->
                        val selectedTeamData = getSelectedTeamData(newSelectedTeamName)
                        loadData(selectedTeamData.playerDataList, selectedTeamData.recordDataList)
                    }

                    selectedSchoolItems.addListener { _, _, newValue ->
                        val teamListOriginalSize = teamListView.items.size
                        if (newValue.size == 0) {
                            logger.info("selected:All")
                            teamListView.items.addAll(data.teamDataList.map { it.name })
                        } else {
                            logger.info("selected:${selectedSchoolItems}")
                            teamListView.items.addAll(data.teamDataList.filter {
                                data.schoolMap[it.schoolID] in newValue
                            }.map { it.name })
                        }

                        teamListView.apply {
                            selectionModel.select(teamListOriginalSize)
                            logger.info("select the first in teamListView")

                            for (i in 0 until teamListOriginalSize) {
                                items.removeAt(0)
                            }

                        }
                    }

                    exportBtn.setOnAction {
                        getExportSettingStage(data).show()
                    }

                    saveBtn.setOnAction {
                        logger.info("Save Data")
                        JsonHelper.toJson(data, R.DATA_JSON_PATH)
                        ConfirmDialog().apply {
                            title = "保存"
                            headerText = "保存成功！"
                            contentText = "数据成功保存至${R.DATA_JSON_PATH}"
                        }.show()
                    }

                    //增删Menu
                    addPlayerMenuItem.setOnAction {
                        AddOrDeleteView.getAddPlayerStage(getSelectedTeamData(), data.schoolMap).show()
                    }
                    deletePlayerMenuItem.setOnAction {
                        AddOrDeleteView.getDeletePlayerStage(getSelectedTeamData(), data.schoolMap).show()
                    }
                    addRecordMenuItem.setOnAction {
                        AddOrDeleteView.getAddRecordStage(getSelectedTeamData(), data.schoolMap).show()
                    }
                    deleteRecordMenuItem.setOnAction {
                        AddOrDeleteView.getDeleteRecordStage(getSelectedTeamData(), data.schoolMap).show()
                    }

                    //分会场数据交互
                    generateRoomDataBtn.setOnAction {
                        logger.info("generate room data")
                        //目前轮数中最大
                        val maxTurn =
                            data.teamDataList.asSequence().map { it.recordDataList }.filter { it.isNotEmpty() }
                                .flatten().toList().let {
                                    if (it.isEmpty()) {
                                        0
                                    } else {
                                        it.maxOf { it.round }
                                    }
                                }

                        GenerateRoomDataView.getGenerateRoomDataStage(
                            if (maxTurn == Config.turns) Config.turns else maxTurn + 1
                        ).show()
                    }

                    addDataFromJsonBtn.setOnAction {
                        try {
                            FileChooser()
                                .apply {
                                    initialDirectory = File(".")
                                    extensionFilters.addAll(
                                        FileChooser.ExtensionFilter("Json File", "*.json"),
                                        FileChooser.ExtensionFilter("All File", "*.*")
                                    )
                                }
                                .run {
                                    showOpenMultipleDialog(MyStage())
                                }.forEach {

                                    it.copyTo(File("${R.SERVER_BACKUP_FILE_DIR_PATH}/${it.name}"), true)
                                    data = data.mergeData(JsonHelper.fromJson<Data>(it.path))
                                    refreshData(data.teamDataList[0])
                                    it.delete()

                                }
                        } catch (e: java.lang.NullPointerException) {
                            logger.error("未选择文件！")
                            logger.error(e.message)
                            ConfirmAlert().apply {
                                title = "加载数据"
                                headerText = "加载数据失败！"
                                contentText = "Error:未找到文件，${e.message}"
                            }.show()
                        }

                        ConfirmDialog().apply {
                            title = "加载数据"
                            headerText = "加载数据成功！"
                        }.show()
                    }

                }


                AddOrDeleteView.apply {
                    addPlayerConfirmBtn.setOnAction {
                        if (checkAddPlayer()) {
                            data.teamDataList.filter {
                                it.name == teamNameLabel.text && "${it.schoolID}" == schoolNameLabel.text.substringBefore(
                                    "-"
                                )
                            }[0].playerDataList.add(
                                PlayerData(
                                    data.getMaxPlayerId() + 1,
                                    playerNameTextField.text,
                                    playerGenderComboBox.selectionModel.selectedItem
                                )
                            )
                            logger.info("Player info:")
                            logger.info("schoolName:${schoolNameLabel.text}")
                            logger.info("teamName:${teamNameLabel.text}")
                            logger.info("Id: ${data.getMaxPlayerId()}")
                            logger.info(("Name: ${playerNameTextField.text}"))
                            logger.info("Gender: ${playerGenderComboBox.selectionModel.selectedItem}")
                            logger.info("Player added successfully!")

                            ConfirmDialog().apply {
                                title = "增加选手"
                                contentText = "增加选手${playerNameTextField.text}成功!"
                                setOnCloseRequest { addPlayerStage.close() }
                            }.show()
                            MainView.refreshData(getSelectedTeamData())
                        }
                    }

                    deletePlayerConfirmBtn.setOnAction {

                        data.teamDataList.filter {
                            it.name == teamNameLabel.text && "${it.schoolID}" == schoolNameLabel.text.substringBefore("-")
                        }[0].playerDataList.removeIf {
                            "${it.id}" == playerDeleteComboBox.selectionModel.selectedItem.substringBefore("-")
                        }
                        ConfirmDialog().apply {
                            title = "删除选手"
                            contentText = "删除选手${playerDeleteComboBox.selectionModel.selectedItem}成功!"
                            setOnCloseRequest { deletePlayerStage.close() }
                        }.show()
                        MainView.refreshData(getSelectedTeamData())

                    }

                    addRecordConfirmBtn.setOnAction {
                        if (checkAddRecord()) {
                            data.teamDataList.filter {
                                it.name == teamNameLabel.text && "${it.schoolID}" == schoolNameLabel.text.substringBefore(
                                    "-"
                                )
                            }[0].recordDataList.add(
                                RecordData(
                                    recordRoundTextField.text.toInt(),
                                    recordPhaseTextField.text.toInt(),
                                    recordRoomIdTextField.text.toInt(),
                                    recordQIdTextField.text.toInt(),
                                    recordMasterIdTextField.text.toInt(),
                                    recordRoleComboBox.selectionModel.selectedItem,
                                    recordScoreTextField.text.toDouble(),
                                    recordWeightTextField.text.toDouble()
                                )
                            )
                            ConfirmDialog().apply {
                                title = "增加记录"
                                contentText = "增加记录成功!"
                                setOnCloseRequest { addRecordStage.close() }
                            }.show()
                            MainView.refreshData(getSelectedTeamData())
                        }

                    }

                    deleteRecordConfirmBtn.setOnAction {
                        data.teamDataList.filter {
                            it.name == teamNameLabel.text && "${it.schoolID}" == schoolNameLabel.text.substringBefore("-")
                        }[0].recordDataList.removeAt(recordComboBox.selectionModel.selectedIndex)
                        ConfirmDialog().apply {
                            title = "删除记录"
                            contentText = "删除记录成功!"
                            setOnCloseRequest { deleteRecordStage.close() }
                        }.show()
                        MainView.refreshData(getSelectedTeamData())
                    }

                }

                GenerateRoomDataView.apply {
                    generateRoomDataConfirmBtn.setOnAction {
                        this.generateRoomData(data)
                    }
                }

            }
        }

        logger.info("完成UI构建，展示Start界面")
    }

    override fun stop() {
        fileNetServer.shutdown()
        super.stop()
    }
}


package nju.pt.server

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.kotlin.ext.*
import nju.pt.net.FileNetServer
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import kotlin.io.path.*

fun main() {
    Application.launch(AppUI::class.java)
}


class AppUI : Application() {
    private val logger = LoggerFactory.getLogger(AppUI::class.java)

    private var data:Data = if (Path(R.DATA_JSON_PATH).exists()) JsonHelper.fromJson<Data>(R.DATA_JSON_PATH) else Json.decodeFromString(R.DATA_JSON_EXAMPLE)
    private val totalTeamNumber: Int by lazy { WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getTotalTeamNumber() }
    private val judgeMap: Map<String, List<String>> by lazy {
        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadJudgeFromExcel()
    }
    private val schoolMap: Map<Int, String> by lazy {
        if (Path(R.DATA_JSON_PATH).exists()) {
            data.schoolMap
        } else {
            WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadSchoolFromExcel()
        }
    }
    private lateinit var fileNetServer: FileNetServer


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

        logger.info("checking data file...")
        if (Path(R.DATA_JSON_PATH).notExists()){
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
                StartViewActions.generateTableBtnAction(totalTeamNumber, judgeMap, schoolMap)
            }
            //生成有裁判的对阵表按钮
            generateTableWithJudgeBtn.setOnAction {
                StartViewActions.generateTableWithJudgeBtnAction(totalTeamNumber, judgeMap, schoolMap)
            }

            //设置界面
            settingBtn.setOnAction {
                StartViewActions.settingBtnAction()
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
                        height = 900.0
                        minWidth = 800.0
                        minHeight = 800.0

                        setOnCloseRequest { _ ->
                            ConfirmAlert().apply {
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
            }
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
                ExportView.getExportSettingStage(data.copy()).show()
            }

            saveBtn.setOnAction {
                MainViewActions.saveBtnAction(data)
            }

            //增删Menu
            addPlayerMenuItem.setOnAction {
                MainViewActions.addPlayerMenuItemAction(getSelectedTeamData(), schoolMap)
            }
            deletePlayerMenuItem.setOnAction {
                MainViewActions.deletePlayerMenuItemAction(getSelectedTeamData(), schoolMap)
            }
            addRecordMenuItem.setOnAction {
                MainViewActions.addRecordMenuItemAction(getSelectedTeamData(), schoolMap,data.questionMap.keys.toList())
            }
            deleteRecordMenuItem.setOnAction {
                MainViewActions.deleteRecordMenuItemAction(getSelectedTeamData(), schoolMap)
            }

            //分会场数据交互
            generateRoomDataBtn.setOnAction {
                MainViewActions.generateRoomDataBtnAction(
                    data.teamDataList.asSequence().map { it.recordDataList }.filter { it.isNotEmpty() }
                        .flatten().toList().let {
                            if (it.isEmpty()) {
                                0
                            } else {
                                it.maxOf { it.round }
                            }
                        }
                )
            }

            addDataFromJsonBtn.setOnAction {
                MainViewActions.addDataFromJsonBtnAction(data)
            }
        }


        AddOrDeleteView.apply {
            addPlayerConfirmBtn.setOnAction {
                AddOrDeleteViewActions.addPlayerConfirmBtnAction(data, getSelectedTeamData())
            }

            deletePlayerConfirmBtn.setOnAction {
                AddOrDeleteViewActions.deletePlayerConfirmBtnAction(data, getSelectedTeamData())
            }

            addRecordConfirmBtn.setOnAction {
                AddOrDeleteViewActions.addRecordConfirmBtnAction(data, getSelectedTeamData())

            }

            deleteRecordConfirmBtn.setOnAction {
                AddOrDeleteViewActions.deleteRecordConfirmBtnAction(data, getSelectedTeamData())
            }

        }

        GenerateRoomDataView.apply {
            generateRoomDataConfirmBtn.setOnAction {
                GenerateRoomDataViewActions.generateRoomDataConfirmBtnAction(data)
            }
        }


        logger.info("完成UI构建，展示Start界面")


    }

    override fun stop() {
        fileNetServer.shutdown()
        super.stop()
    }
}


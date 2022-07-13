package nju.pt.server

import com.sun.tools.javac.Main
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.SelectionMode
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.serialization.json.Json
import nju.pt.R
import nju.pt.databaseassist.*
import kotlin.io.path.Path
import nju.pt.kotlin.ext.*
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.io.path.notExists

fun main() {
    Application.launch(AppUI::class.java)
}


class AppUI : Application() {
    private val logger = LoggerFactory.getLogger(AppUI::class.java)

    private var data = JsonHelper.fromJson<Data>(R.DATA_JSON_EXAMPLE_PATH)
    private val totalTeamNumber: Int by lazy { WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getTotalTeamNumber() }
    private val judgeMap: Map<String, List<String>> by lazy {
        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadJudgeFromExcel()
    }
    private val schoolMap: Map<Int, String> by lazy {
        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadSchoolFromExcel()
    }

    private fun getExportSettingStage(data: Data) = Stage().apply {
        scene = Scene(ExportView().build(data)).apply {
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            icons.add(Image(R.LOGO_PATH))

        }
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
        if (Path(R.SERVER_CACHE_DIR_PATH).notExists()) {
            File(R.SERVER_CACHE_DIR_PATH).mkdir()
        }
        if (Path(R.SERVER_DATA_DIR_PATH).notExists()) {
            File(R.SERVER_DATA_DIR_PATH).mkdir()
            XSSFWorkbook().initializeExcel()
            logger.error("已新建配置文件，请在${R.SERVER_DATA_DIR_PATH}中配置服务端配置文件！")
            throw Exception("已新建配置文件，请在${R.SERVER_DATA_DIR_PATH}中配置服务端配置文件！")
        }

        if (Path(R.CONFIG_EXCEL_PATH).notExists()) {
            XSSFWorkbook().initializeExcel()
            logger.error("已新建配置文件，请在${R.SERVER_DATA_DIR_PATH}中配置服务端配置文件！")
            throw Exception("已新建配置文件，请在${R.SERVER_DATA_DIR_PATH}中配置服务端配置文件！")
        }
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

                    generateTableAlert.apply {
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
                } catch (e: Exception) {
                    logger.error(e.message)
                    generateTableAlert.apply {
                        title = "生成对阵表(有裁判)"
                        contentText = "Error:${e.message}"
                    }.show()
                    throw Exception(e.message)

                }
                generateTableDialog.apply {
                    title = "生成对阵表(有裁判)"
                }.show()
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
                    }.show()
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

                            confirmDialog.apply {
                                title = "增加选手"
                                contentText = "增加选手${playerNameTextField.text}成功!"
                                setOnCloseRequest { addPlayerStage.close() }
                            }.show()
                            MainView.refreshData(data)
                        }
                    }

                    deletePlayerConfirmBtn.setOnAction {

                        data.teamDataList.filter {
                            it.name == teamNameLabel.text && "${it.schoolID}" == schoolNameLabel.text.substringBefore("-")
                        }[0].playerDataList.removeIf {
                            "${it.id}" == playerDeleteComboBox.selectionModel.selectedItem.substringBefore("-")
                        }
                        confirmDialog.apply {
                            title = "删除选手"
                            contentText = "删除选手${playerDeleteComboBox.selectionModel.selectedItem}成功!"
                            setOnCloseRequest { deletePlayerStage.close() }
                        }.show()
                        MainView.refreshData(data)

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
                            confirmDialog.apply {
                                title = "增加记录"
                                contentText = "增加记录成功!"
                                setOnCloseRequest { addRecordStage.close() }
                            }.show()
                            MainView.refreshData(data)
                        }

                    }

                    deleteRecordConfirmBtn.setOnAction {
                        data.teamDataList.filter {
                            it.name == teamNameLabel.text && "${it.schoolID}" == schoolNameLabel.text.substringBefore("-")
                        }[0].recordDataList.removeAt(recordComboBox.selectionModel.selectedIndex)
                        confirmDialog.apply {
                            title = "删除记录"
                            contentText = "删除记录成功!"
                            setOnCloseRequest { deleteRecordStage.close() }
                        }.show()
                        MainView.refreshData(data)
                    }

                }

            }
        }

        logger.info("完成UI构建，展示Start界面")

    }
}
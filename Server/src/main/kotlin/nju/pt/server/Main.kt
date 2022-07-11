package nju.pt.server

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import nju.pt.R
import kotlin.io.path.Path
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
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

            scene = Scene(StartView.build()).apply {
                stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
            }

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
                        scene = Scene(MainView.build(data)).apply {
                            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
                        }
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

                //增加对ListView的监听
                MainView.apply {
                    teamListView.selectionModel.selectedItemProperty().addListener { _, _, newSelectedTeamName ->
                        val selectedTeamData = data.teamDataList.first { it.name == newSelectedTeamName }
                        loadData(selectedTeamData.playerDataList, selectedTeamData.recordDataList)
                    }
                }
            }
        }


        logger.info("完成UI构建，展示Start界面")

    }
}
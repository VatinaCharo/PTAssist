package nju.pt.server

import nju.pt.R
import nju.pt.databaseassist.JsonInterface
import nju.pt.databaseassist.RecordData
import nju.pt.kotlin.ext.initializeJson
import nju.pt.kotlin.ext.loadJudgeFromExcel
import nju.pt.kotlin.ext.loadQuestionFromExcel
import nju.pt.kotlin.ext.loadSchoolFromExcel
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class ExcelTest {
    private val logger = LoggerFactory.getLogger(ExcelTest::class.java)

    @Test
    fun serverConfigTest() {
        logger.info("port = ${Config.port}, judgeCount = ${Config.judgeCount}, roomCount = ${Config.roomCount}")
    }

    @Test
    fun workbookExtTest() {
        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).apply {
            logger.info("questionMap = ${loadQuestionFromExcel()}")
            logger.info("schoolMap = ${loadSchoolFromExcel()}")
            logger.info("judgeMap = ${loadJudgeFromExcel()}")
            logger.info("teamData = ${initializeJson()}")
        }

        println(JsonInterface.fromJson(R.DATA_JSON_PATH))
    }

    @Test
    fun exportExcelTest() {
        val teamDataList = JsonInterface.fromJson(R.DATA_JSON_PATH).apply {
            this.teamDataList[0].recordDataList = mutableListOf<RecordData>(
                RecordData(
                    1, 1, 1, 9, 1, "R", 30.0, doubleArrayOf(3.0, 2.0, 1.0)
                ),
            )

            this.teamDataList[1].recordDataList = mutableListOf<RecordData>(
                RecordData(
                    1, 1, 1, 1, 3, "R", 30.0, doubleArrayOf(3.0, 2.0, 1.0)
                ),
            )

        }
        println(teamDataList)
        ExportExcel(teamDataList, ".").exportTeamScore()
        ExportExcel(teamDataList, ".").exportReviewTable()
        ExportExcel(teamDataList, ".").exportPlayerScore()

    }


}



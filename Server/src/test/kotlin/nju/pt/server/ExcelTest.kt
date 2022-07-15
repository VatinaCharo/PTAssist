package nju.pt.server

import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.databaseassist.RecordData
import nju.pt.kotlin.ext.getTotalTeamNumber
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
//            logger.info("teamData = ${initializeJson()}")
            logger.info("totTeamNumber = ${getTotalTeamNumber()}")
        }

//        println(JsonHelper.fromJson(nju.pt.net.R.DATA_JSON_PATH))
    }

    @Test
    fun exportExcelTest() {
        val data = JsonHelper.fromJson<Data>("../${R.DATA_JSON_PATH}").apply {
            this.teamDataList[0].recordDataList = mutableListOf<RecordData>(
                RecordData(
                    1, 1, 1, 9, 1, "nju.pt.net.R", 10.0, 3.0
                ),
                RecordData(
                    2, 1, 1, 1, 1, "nju.pt.net.R", 8.0, 3.0
                ),
            )

//            this.teamDataList[0].recordDataList = mutableListOf<RecordData>(
//                RecordData(
//                    2, 1, 1, 1, 1, "nju.pt.net.R", 8.0, 3.0
//                ),
//            )

        }.apply {
            JsonHelper.toJson<Data>(this, "data_test.json")
        }
        println(data)
        ExportExcel(data, ".").exportTeamScore()
        ExportExcel(data, ".").exportReviewTable()
        ExportExcel(data, ".").exportPlayerScore()

    }


}



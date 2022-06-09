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

//    @Test
//    fun serverConfigTest() {
//        logger.info("port = ${Config.port}, judgeCount = ${Config.judgeCount}, roomCount = ${Config.roomCount}")
//    }

//    @Test
//    fun workbookExtTest() {
//        WorkbookFactory.create(R.CONFIG_EXCEL_FILE).apply {
////            logger.info("questionMap = ${loadQuestionFromExcel()}")
////            logger.info("schoolMap = ${loadSchoolFromExcel()}")
////            logger.info("judgerMap = ${loadJudgeFromExcel()}")
//            logger.info("teamData = ${initializeJson()}")
//        }
//
//        println(JsonInterface.fromJson(R.TO_JSON_PATH))
//    }

    @Test
    fun exportExcelTest(){
        val teamDataList = JsonInterface.fromJson(R.TO_JSON_PATH).apply {
            this.teamDataList.forEach {
                it.recordDataList = mutableListOf<RecordData>(
                    RecordData(
                        1,1,1,2,1,"R",30.0, doubleArrayOf(3.0,2.0,1.0)
                    ),
                    RecordData(
                        2,2,2,1,1,"O",30.0, doubleArrayOf(3.0,2.0,1.0)
                    )
                )
            }
        }
//        ExportExcel(teamDataList,".").exportTeamScore()
        ExportExcel(teamDataList,".").exportReveiwTable()

    }





}



package nju.pt.server

import nju.pt.R
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
        }
    }
}
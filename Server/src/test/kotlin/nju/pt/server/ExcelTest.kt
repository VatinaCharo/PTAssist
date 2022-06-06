package nju.pt.server

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class ExcelTest {
    private val logger = LoggerFactory.getLogger(ExcelTest::class.java)

    @Test
    fun serverConfigTest() {
        logger.info("port = ${ConfigData.port}, judgeCount = ${ConfigData.judgeCount}, roomCount = ${ConfigData.roomCount}")
    }

    @Test
    fun serverQuestionDataTest() {
        logger.info("questionMap = ${QuestionData.questionMap}")
    }
}
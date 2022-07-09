package nju.pt.server

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.math.log

class CounterPartTableTest {
    private val logger = LoggerFactory.getLogger(ExcelTest::class.java)

    @Test
    fun generateTableWithoutJudgeTest(){
        val counterPartTable = CounterPartTable()
        val tableList = counterPartTable.apply {
            generateTableWithoutJudge(3)
            generateTableWithJudge()
        }
    }
}
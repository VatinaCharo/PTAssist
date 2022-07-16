package nju.pt.client

import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import org.junit.jupiter.api.Test

class RuleTest {
    @Test
    fun getOptionalQuestionIDListTest() {
        val data = JsonHelper.fromJson<Data>("src/test/data.json")
        val repRecordList = data.teamDataList[0].recordDataList
        println("repRecordList = ${repRecordList.map { it.role to it.questionID }}")
        val oppRecordList = data.teamDataList[1].recordDataList
        println("oppRecordList = ${oppRecordList.map { it.role to it.questionID }}")
        val qIDList =
            JSYPTRule.getOptionalQuestionIDList(
                repRecordList,
                oppRecordList,
                listOf(),
                data.questionMap.keys.toList()
            )
        println(qIDList)
    }
}
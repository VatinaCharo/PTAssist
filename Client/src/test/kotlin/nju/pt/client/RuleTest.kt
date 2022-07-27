package nju.pt.client

import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import org.junit.jupiter.api.Test

class RuleTest {
    val data = JsonHelper.fromJson<Data>("src/test/data.json")
    val repPlayerDataList = data.teamDataList[0].playerDataList
    val repRecordList = data.teamDataList[0].recordDataList
    val oppRecordList = data.teamDataList[1].recordDataList

    init {
        println("repRecordList = ${repRecordList.map { Triple(it.role, it.masterID, it.questionID) }}")
        println("oppRecordList = ${oppRecordList.map { Triple(it.role, it.masterID, it.questionID) }}")
    }

    @Test
    fun getOptionalQuestionIDListTest() {
        var usedQuestionIDList = listOf<Int>()
        var qIDList =
            CUPTRule.getOptionalQuestionIDList(
                repRecordList,
                oppRecordList,
                usedQuestionIDList,
                data.questionMap.keys.toList(),
                RoundType.NORMAL
            )
        println(qIDList)
        assert(qIDList.equals(listOf(1, 3, 9, 11, 16)))
        // 反-正
        usedQuestionIDList = listOf(1)
        qIDList =
            CUPTRule.getOptionalQuestionIDList(
                repRecordList,
                oppRecordList,
                usedQuestionIDList,
                data.questionMap.keys.toList(),
                RoundType.NORMAL
            )
        println(qIDList)
        assert(qIDList.equals(listOf(3, 7, 9, 11, 16)))
        // 反-反
        usedQuestionIDList = listOf(1, 3)
        qIDList =
            CUPTRule.getOptionalQuestionIDList(
                repRecordList,
                oppRecordList,
                usedQuestionIDList,
                data.questionMap.keys.toList(),
                RoundType.NORMAL
            )
        println(qIDList)
        assert(qIDList.equals(listOf(7, 9, 11, 14, 16)))
        // 正-正
        usedQuestionIDList = listOf(1, 3, 16)
        qIDList =
            CUPTRule.getOptionalQuestionIDList(
                repRecordList,
                oppRecordList,
                usedQuestionIDList,
                data.questionMap.keys.toList(),
                RoundType.NORMAL
            )
        println(qIDList)
        assert(qIDList.equals(listOf(6, 7, 9, 11, 14)))
        // 正-正
        usedQuestionIDList = listOf(1, 3, 11, 16)
        qIDList =
            CUPTRule.getOptionalQuestionIDList(
                repRecordList,
                oppRecordList,
                usedQuestionIDList,
                data.questionMap.keys.toList(),
                RoundType.NORMAL
            )
        println(qIDList)
        assert(qIDList.equals(listOf(2, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 17)))
    }

    @Test
    fun getValidPlayerIDListTest() {
        var roundPlayerRecordList = listOf<Int>()
        var pIDList = CUPTRule.getValidPlayerIDList(listOf(), repRecordList, repPlayerDataList)
        println(pIDList)
        assert(pIDList.equals(listOf(34, 35, 36, 37, 38)))
        roundPlayerRecordList = listOf(34, 34, 38, 38)
        pIDList = CUPTRule.getValidPlayerIDList(roundPlayerRecordList, repRecordList, repPlayerDataList)
        println(pIDList)
        assert(pIDList.equals(listOf(35, 36, 37)))
    }
}
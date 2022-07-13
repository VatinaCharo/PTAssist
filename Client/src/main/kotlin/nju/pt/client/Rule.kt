package nju.pt.client

import RuleInterface
import nju.pt.databaseassist.RecordData

class Rule : RuleInterface {
    override fun getOptionalQuestionIDList(
        repTeamRecordDataList: List<RecordData>,
        oppTeamRecordDataList: List<RecordData>,
        usedQuestionIDList: List<Int>,
        questionLibList: List<Int>
    ): List<Int> {
        TODO("Not yet implemented")
    }

    override fun getValidPlayerIDList(): List<Int> {
        TODO("Not yet implemented")
    }

    override fun getScore(scoreList: List<Double>): Double {
        TODO("Not yet implemented")
    }
}
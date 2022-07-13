package nju.pt.client

import RuleInterface
import nju.pt.databaseassist.PlayerData
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

    override fun getValidPlayerIDList(
        tempPlayerRecordList: List<PlayerData>,
        teamRecordDataList: List<RecordData>,
        playerDataList: List<PlayerData>
    ): List<Int> {
        TODO("Not yet implemented")
    }

    /**
     * 获取本阶段的得分
     *
     * 5裁判 -> ((最高分 + 最低分) / 2 + 其他分数求和) / (裁判数 -1 )
     * 7裁判 -> 去掉一个最高分，去掉一个最低分，再取平均分
     *
     * @param scoreList 分数列表 传入各裁判打分
     * @return 分数 获取最后的统计分数
     */
    override fun getScore(scoreList: List<Double>): Double {
        when (scoreList.size) {
            5 -> {
                val sortedScoreList = scoreList.sorted()
                val minScore = sortedScoreList.first()
                val maxScore = sortedScoreList.last()
                return (sortedScoreList.sum() - (minScore + maxScore) / 2.0) / (sortedScoreList.size - 1)
            }
            7 -> {
                val sortedScoreList = scoreList.sorted()
                val minScore = sortedScoreList.first()
                val maxScore = sortedScoreList.last()
                return (sortedScoreList.sum() - minScore - maxScore) / (sortedScoreList.size - 2)
            }
            else -> {
                TODO("Not yet implemented")
            }
        }
    }
}
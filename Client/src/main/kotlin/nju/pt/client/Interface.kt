import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData

interface RuleInterface {
    /**
     * 获取当前对局的可选赛题
     *
     * @param repTeamRecordDataList 正方队伍比赛记录
     * @param oppTeamRecordDataList 反方队伍比赛记录
     * @param usedQuestionIDList 当前比赛轮次中已用的赛题
     * @param questionLibList 赛题库
     * @return 赛题编号列表 包含了当前对局中的全部可选题
     */
    fun getOptionalQuestionIDList(
        repTeamRecordDataList: List<RecordData>,
        oppTeamRecordDataList: List<RecordData>,
        usedQuestionIDList: List<Int>,
        questionLibList: List<Int>
    ): List<Int>

    /**
     * 获取当前对局的可上场队员
     *
     * @param tempPlayerRecordList 当前比赛轮次中上场主控的队员
     * @param teamRecordDataList 队伍比赛记录
     * @param playerDataList 队伍的队员列表
     * @return 当前可主控队员 包含了此队伍的当前可上场主控的全部队员
     */
    fun getValidPlayerIDList(
        tempPlayerRecordList: List<PlayerData>,
        teamRecordDataList: List<RecordData>,
        playerDataList: List<PlayerData>
    ): List<Int>

    /**
     * 获取本阶段的得分
     *
     * @param scoreList 分数列表 传入各裁判打分
     * @return 分数 获取最后的统计分数
     */
    fun getScore(scoreList: List<Double>): Double
}
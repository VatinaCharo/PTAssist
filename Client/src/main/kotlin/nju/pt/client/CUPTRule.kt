package nju.pt.client

import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.RecordData
import org.slf4j.LoggerFactory

object CUPTRule : RuleInterface {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private const val questionCount = 5
    private val banRuleListConfig = listOf(
        TeamType.REPORTER to QuestionType.REFUSED,
        TeamType.REPORTER to QuestionType.REPORTED,
        TeamType.OPPONENT to QuestionType.OPPOSED,
        TeamType.OPPONENT to QuestionType.REPORTED
    )
    private val specialBanRuleListConfig = listOf(TeamType.REPORTER to QuestionType.REPORTED)
    private const val playerMasterTimesIn1RoundConfig = 2
    private const val playerMasterTimesIn1MatchConfig = 5
    private const val playerRepTimesIn1MatchConfig = 3
    private const val maxRefuseQuestionCount = 5

    private fun getQuestionType(value: String) = when (value) {
        "R" -> QuestionType.REPORTED
        "O" -> QuestionType.OPPOSED
        "X" -> QuestionType.REFUSED
        else -> QuestionType.OPTIONAL
    }

    /**
     * 获取当前对局的可选赛题
     *
     * 当前的比赛赛题禁选规则为：
     *
     * 【不可解锁规则】：
     *
     * 在同一轮对抗赛中，题目只能被陈述一次。
     *
     * 【可解锁规则】：
     *
     * 1. 正方作为正方拒绝过的题目
     * 2. 正方作为正方拒绝过的题目
     * 3. 反方作为反方挑战过的题目
     * 4. 反方作为正方陈述过的题目
     *
     *
     * @param repTeamRecordDataList 正方队伍比赛记录
     * @param oppTeamRecordDataList 反方队伍比赛记录
     * @param usedQuestionIDList 当前比赛轮次中已用的赛题
     * @param questionIDLibList 赛题库
     * @param roundType 本轮比赛类型 普通轮次或自选题轮次
     * @return 赛题编号列表 包含了当前对局中的全部可选题
     */
    override fun getOptionalQuestionIDList(
        repTeamRecordDataList: List<RecordData>,
        oppTeamRecordDataList: List<RecordData>,
        usedQuestionIDList: List<Int>,
        questionIDLibList: List<Int>,
        roundType: RoundType
    ): List<Int> {
        if (questionIDLibList.size > questionCount) {
            val tempQuestionIDLibList = questionIDLibList.minus(usedQuestionIDList.toSet())
            logger.info("当前比赛可用题库为$tempQuestionIDLibList")
            val repQRecordSet =
                repTeamRecordDataList.map {
                    it.questionID to (TeamType.REPORTER to getQuestionType(it.role))
                }.toSet()
            logger.info("repQRecordSet = $repQRecordSet")
            val oppQRecordSet =
                oppTeamRecordDataList
                    .map {
                        it.questionID to (TeamType.OPPONENT to getQuestionType(it.role))
                    }.toSet()
            logger.info("oppQRecordSet = $oppQRecordSet")
            var banRuleList = banRuleListConfig
            when (roundType) {
                RoundType.SPECIAL -> {
                    // 自选题轮次，除了正过的题，都可以上
                    logger.info("自选题轮次")
                    logger.info("getOptionalQuestionIDList(tempQuestionIDLibList, repQRecordSet, oppQRecordSet, banRuleList)")
                    logger.info("tempQuestionIDLibList = $tempQuestionIDLibList")
                    logger.info("repQRecordSet = $repQRecordSet")
                    logger.info("oppQRecordSet = $oppQRecordSet")
                    logger.info("specialBanRuleListConfig = $specialBanRuleListConfig")
                    return getOptionalQuestionIDList(
                        tempQuestionIDLibList,
                        repQRecordSet,
                        oppQRecordSet,
                        specialBanRuleListConfig
                    )
                }

                RoundType.NORMAL -> {
                    var optionalQuestionIDList: List<Int>
                    // 如果获取到的可选题目数量小于指定的题目数量限制（这里是5）,则从后往前依次解锁题目限制规则，直到最终题目数量大于5
                    do {
                        logger.info("正常题轮次")
                        logger.info("getOptionalQuestionIDList(tempQuestionIDLibList, repQRecordSet, oppQRecordSet, banRuleList)")
                        logger.info("tempQuestionIDLibList = $tempQuestionIDLibList")
                        logger.info("repQRecordSet = $repQRecordSet")
                        logger.info("oppQRecordSet = $oppQRecordSet")
                        logger.info("banRuleList = $banRuleList")
                        optionalQuestionIDList =
                            getOptionalQuestionIDList(tempQuestionIDLibList, repQRecordSet, oppQRecordSet, banRuleList)
                        banRuleList = banRuleList.dropLast(1)
                    } while (optionalQuestionIDList.size < questionCount)
                    return optionalQuestionIDList
                }
            }
        } else {
            logger.warn("赛题小于${questionCount}道，无法进行赛题的禁用与解放")
            return questionIDLibList
        }
    }

    /**
     * 获取当前可选题的题号列表
     *
     * @param questionIDList 题号列表
     * @param repQRecordList 正方已比赛的题目记录
     * @param oppQRecordList 反方已比赛的题目记录
     * @param banRuleList 题目禁选规则
     * @return 当前禁选规则下的可选题号列表
     */
    private fun getOptionalQuestionIDList(
        questionIDList: List<Int>,
        repQRecordList: Set<Pair<Int, Pair<TeamType, QuestionType>>>,
        oppQRecordList: Set<Pair<Int, Pair<TeamType, QuestionType>>>,
        banRuleList: List<Pair<TeamType, QuestionType>>
    ): List<Int> {
        val repBanQuestionIDList = repQRecordList.filter { it.second in banRuleList }.map { it.first }
        logger.info("repBanQuestionIDList = $repBanQuestionIDList")
        val oppBanQuestionIDList = oppQRecordList.filter { it.second in banRuleList }.map { it.first }
        logger.info("oppBanQuestionIDList = $oppBanQuestionIDList")
        return questionIDList.minus(repBanQuestionIDList.toSet()).minus(oppBanQuestionIDList.toSet())
    }

    /**
     * 获取当前对局的可上场队员
     *
     * 当前队员禁上场规则为：
     *
     * 1. 在每轮比赛中，每个队员最多只能主控2次
     * 2. 在整个比赛中，每个队员最多只能主控5次
     * 3. 在整个比赛中，每个队员最多只能正方陈述3次
     *
     * @param roundPlayerRecordList 当前比赛轮次中上场主控的队员记录
     * @param teamRecordDataList 队伍比赛记录
     * @param playerDataList 队伍的队员列表
     * @return 当前可主控队员 包含了此队伍的当前可上场主控的全部队员
     */
    override fun getValidPlayerIDList(
        roundPlayerRecordList: List<Int>,
        teamRecordDataList: List<RecordData>,
        playerDataList: List<PlayerData>
    ): List<Int> = playerDataList
        .filter { playerData ->
            // 筛选未超过本轮主控次数限制的队员
            val playerMasterTimesIn1Round = roundPlayerRecordList.filter { it == playerData.id }.size
            logger.info("playerData = $playerData, playerMasterTimesIn1Round = $playerMasterTimesIn1Round")
            playerMasterTimesIn1Round < playerMasterTimesIn1RoundConfig
        }
        .filter { playerData ->
            // 筛选未超过比赛总主控次数限制的队员
            val playerMasterTimesIn1Match =
                teamRecordDataList
                    .filter { it.role in listOf("R", "O", "V") }
                    .filter { it.masterID == playerData.id }
                    .size
            logger.info("playerData = $playerData, playerMasterTimesIn1Match = $playerMasterTimesIn1Match")
            playerMasterTimesIn1Match < playerMasterTimesIn1MatchConfig
        }
        .filter { playerData ->
            // 筛选未超过比赛总的正方主控次数限制的队员
            val playerRepTimesIn1Match =
                teamRecordDataList.filter { it.masterID == playerData.id && it.role == "R" }.size
            logger.info("playerData = $playerData, playerRepTimesIn1Match = $playerRepTimesIn1Match")
            playerRepTimesIn1Match < playerRepTimesIn1MatchConfig
        }
        .map { it.id }

    /**
     * 获取本阶段的得分
     *
     * 5裁判 -> ((最高分 + 最低分) / 2 + 其他分数求和) / (裁判数 -1 )
     * 7裁判 -> 去掉一个最高分，去掉一个最低分，再取平均分
     *
     * @param scoreList 分数列表 传入各裁判打分
     * @return 分数 获取最后的统计分数
     */
    override fun getScore(scoreList: List<Int>): Double {
        when (scoreList.size) {
            5 -> {
                val sortedScoreList = scoreList.sorted()
                val minScore = sortedScoreList.first()
                val maxScore = sortedScoreList.last()
                return (sortedScoreList.sum() - (minScore + maxScore) / 2.0) / (sortedScoreList.size - 1.0)
            }

            7 -> {
                val sortedScoreList = scoreList.sorted()
                val minScore = sortedScoreList.first()
                val maxScore = sortedScoreList.last()
                return (sortedScoreList.sum() - minScore - maxScore) / (sortedScoreList.size - 2.0)
            }

            else -> {
                logger.warn("暂未提供其他裁判数下的统分规则，默认采用平均分机制")
                return scoreList.average()
            }
        }
    }

    /**
     * Get rep score weight
     *
     * CUPT规则 最多拒绝5题，每多拒一题，扣0.2正方系数
     *
     * @param teamRecordDataList 队伍比赛记录
     * @param isRefuse 是否拒题
     * @return 正方计分权重
     */
    override fun getRepScoreWeight(teamRecordDataList: List<RecordData>, isRefuse: Boolean): Double {
        logger.info("getRepScoreWeight(teamRecordDataList: List<RecordData>, isRefuse: Boolean)")
        logger.info("teamRecordDataList = $teamRecordDataList")
        logger.info("isRefuse = $isRefuse")
        val oldRepScoreWeightList = teamRecordDataList.filter { it.role in listOf("R", "X") }.map { it.weight }
//        空列表调用 minof() 函数会抛出 NoSuchElementException
        return if (oldRepScoreWeightList.isEmpty()) {
            logger.info("return 3.0")
            3.0
        } else {
            val oldRepScoreWeight = oldRepScoreWeightList.minOf { it }
            val refusedQuestionCount = teamRecordDataList.filter { it.role == "X" }.size
            val weight =
                if (isRefuse && refusedQuestionCount >= maxRefuseQuestionCount) oldRepScoreWeight - 0.2 else oldRepScoreWeight
            logger.info("return $weight")
            weight
        }
    }

    override fun getOppScoreWeight(): Double = 2.0

    override fun getRevScoreWeight(): Double = 1.0
}
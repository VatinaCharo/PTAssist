package nju.pt.databaseassist


@kotlinx.serialization.Serializable
data class PlayerData(
    var id: Int,
    var name: String,
    var gender: String
)

/**
 * 一条比赛记录
 *  */
@kotlinx.serialization.Serializable
data class RecordData(
    var roomId: Int,
    var round: Int,
    var phase: Int,
    var questionId: Int,
    var playerId: Int,
    var role: String,
    var score: Double,
    var weight: Double
)

@kotlinx.serialization.Serializable
data class TeamData(
    var id: Int,
    var name: String,
    var schoolId: Int,
    val playerDataList: MutableList<PlayerData>,
    var recordDataList: MutableList<RecordData> = mutableListOf<RecordData>()
)


@kotlinx.serialization.Serializable
data class TeamDataList(
    var teamDataList: List<TeamData>,
    val questionMap: Map<Int, String>,
    val schoolMap: Map<Int, String>
) {
    fun getTeamScore() = teamDataList.map {
        // 学校名，队伍名，总成绩
        Triple(
            schoolMap[it.schoolId],
            it.name,
            // 可以使用fold函数简化代码
//            it.recordDataList.let { recordDataList ->
//                var totalScore: Double = 0.0
//                recordDataList?.forEach { recordData ->
//                    totalScore += recordData.score
//                }
//                totalScore
//            }
            it.recordDataList.fold(0.0) { total, recordData ->
                total + recordData.score * recordData.weight
            }
        )
    }.sortedByDescending { it.third }


    fun getReviewTable() = teamDataList.map {
        // 学校名，队伍名，[(赛题id to 队员们),()]
        Triple(
            schoolMap[it.schoolId],
            it.name,
            mutableMapOf<Int, String>().apply {
                it.recordDataList.forEach { recordData ->
                    this[recordData.questionId] = this.getOrDefault(recordData.questionId, "") + recordData.role
                }
            }
        )
    }

    fun getPlayerScore() = teamDataList.map { it ->
        // 学校名，队伍名，[(姓名 to [性别，正方得分总览，反方得分总览，评方得分总览,正方平均分,反方平均分,评方平均分])]
        Triple(
            schoolMap[it.schoolId],
            it.name,
            mutableMapOf<String, MutableList<Any>>().apply {
                it.recordDataList.forEach { recordData ->
                    //若不是拒题
                    if (recordData.role != "X") {
                        //获取这条记录中的队员名称和性别
                        try {
                            val (playerName, playerGender) = it.playerDataList.filter { playerData ->
                                playerData.id == recordData.playerId
                            }[0].let { playerData ->
                                listOf(playerData.name, playerData.gender)
                            }

                            //增加这条记录
                            this[playerName] = this.getOrDefault(
                                playerName, mutableListOf<Any>(
                                    playerGender,
                                    "", "", "",
                                    0.0, 0.0, 0.0
                                )
                            ).apply {
                                when (recordData.role) {
                                    "R" -> {
                                        this[1] = this[1] as String + "," + recordData.score.toString()
                                        this[1 + 3] = this[1 + 3] as Double + recordData.score
                                    }
                                    "O" -> {
                                        this[2] = this[2] as String + "," + recordData.score.toString()
                                        this[2 + 3] = this[2 + 3] as Double + recordData.score
                                    }
                                    "V" -> {
                                        this[3] = this[3] as String + "," + recordData.score.toString()
                                        this[3 + 3] = this[3 + 3] as Double + recordData.score
                                    }
                                }
                            }

                        } catch (e: IndexOutOfBoundsException) {
                            throw Exception("未在队员名单中找到比赛记录中id为${recordData.playerId}的队员，请检查！")
                        }


                    }

                }

                //再对得到的列表进行一定处理
                this.forEach { (_, dataList) ->
                    for (i in 1..2) {
                        if (dataList[i] != "") {
                            //求平均值
                            dataList[i + 3] = (dataList[i + 3] as Double) / (dataList[i] as String).count { it == ',' }
                            //去掉第一个逗号
                            dataList[i] = (dataList[i] as String).substringAfter(",")
                        }
                    }
                }

            }.toMap()
        )
    }

}


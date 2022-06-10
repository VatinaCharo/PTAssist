package nju.pt.databaseassist


@kotlinx.serialization.Serializable
data class PlayerData(
    var id: Int,
    var name: String,
    var gender: String
)

@kotlinx.serialization.Serializable
data class TeamData(
    var id: Int,
    var name: String,
    var schoolId: Int,
    val playerDataList: MutableList<PlayerData>,
    var recordDataList: MutableList<RecordData>?
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
    var weightArray: DoubleArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordData

        if (roomId != other.roomId) return false
        if (round != other.round) return false
        if (phase != other.phase) return false
        if (questionId != other.questionId) return false
        if (playerId != other.playerId) return false
        if (role != other.role) return false
        if (score != other.score) return false
        if (!weightArray.contentEquals(other.weightArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId
        result = 31 * result + round
        result = 31 * result + phase
        result = 31 * result + questionId
        result = 31 * result + playerId
        result = 31 * result + role.hashCode()
        result = 31 * result + score.hashCode()
        result = 31 * result + weightArray.contentHashCode()
        return result
    }
}

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
            it.recordDataList.let { listData ->
                var totalScore: Double = 0.0
                listData?.forEach { recordData ->
                    totalScore += recordData.score
                }
                totalScore
            })
    }.sortedByDescending { it.third }


    fun getReviewTable() = teamDataList.map {
        // 学校名，队伍名，[(赛题id to 角色们),()]
        Triple(
            schoolMap[it.schoolId],
            it.name,
            mutableMapOf<Int, String>().apply {
                it.recordDataList?.forEach { recordData ->
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
                it.recordDataList?.forEach { recordData ->
                    //若不是拒题
                    if (recordData.role != "X") {
                        //获取这条记录中的队员名称和性别
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
                                0.0,0.0,0.0
                            )
                        ).apply {
                            when (recordData.role) {
                                "R" -> {this[1] = this[1] as String + "," + recordData.score.toString()
                                    this[1+3] = this[1+3] as Double + recordData.score}
                                "O" -> {this[2] = this[2] as String + "," + recordData.score.toString()
                                    this[2+3] = this[2+3] as Double + recordData.score}
                                "V" -> {this[3] = this[3] as String + "," + recordData.score.toString()
                                    this[3+3] = this[3+3] as Double + recordData.score}
                            }
                        }
                    }

                }

                this.forEach { (name, dataList) ->
                    for (i in 1..2){
                        if (dataList[i] != ""){
                            //求平均值
                            dataList[i+3] = (dataList[i+3]as Double)/( dataList[i] as String).count {it ==',' }
                            //去掉第一个,
                            dataList[i] = (dataList[i] as String).substringAfter(",")
                        }
                    }
                }

            }.toMap()
        )
    }

}


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
data class TeamDataList(var teamDataList:List<TeamData>, val questionMap:Map<Int,String>,val schoolMap:Map<Int,String>){
    fun getTeamScore() = teamDataList.map {
        // 学校名，队伍名，总成绩
        Triple(
            schoolMap[it.schoolId],
            it.name,
            it.recordDataList.let { listData ->
            var totalScore : Double = 0.0
            listData?.forEach {recordData->
                totalScore += recordData.score
            }
            totalScore
        })
    }.sortedByDescending { it.third }


    fun getReviewTable() = teamDataList.map{
        // 学校名，队伍名，[(赛题id，角色们),()]
        Triple(
            schoolMap[it.schoolId],
            it.name,
//            mutableListOf<Pair<Int,String>>().apply {
//                    it.recordDataList?.forEach {recordData ->
//                       this.add( recordData.questionId  to recordData.role)
//                    }
//            }.toList()
            mutableMapOf<Int,String>().apply {
                it.recordDataList?.forEach {recordData ->
                    this[recordData.questionId] = this.getOrDefault(recordData.questionId,"")+ recordData.role
                }
            }
        )
    }
}


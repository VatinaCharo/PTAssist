package nju.pt.databaseassist

import java.io.File


@kotlinx.serialization.Serializable
data class PlayerData(
    var id: Int, var name: String, var gender: String
)

/**
 * 一条比赛记录
 *  */
@kotlinx.serialization.Serializable
data class RecordData(
    var round: Int,
    var phase: Int,
    var roomID: Int,
    var questionID: Int,
    var masterID: Int,
    var role: String,
    var score: Double,
    var weight: Double
)

@kotlinx.serialization.Serializable
data class TeamData(
    var id: Int,
    var name: String,
    var schoolID: Int,
    val playerDataList: MutableList<PlayerData>,
    var recordDataList: MutableList<RecordData> = mutableListOf()
)


@kotlinx.serialization.Serializable
data class Data(
    var teamDataList: List<TeamData>, val questionMap: Map<Int, String>, val schoolMap: Map<Int, String>
) {
    fun copy(): Data {
        JsonHelper.toJson(this, "./tmp.json")
        JsonHelper.fromJson<Data>("./tmp.json").let {
            File("./tmp.json").delete()
            return it
        }
    }

    fun getTeamScore() = teamDataList.map { it ->
        // 学校名，队伍名，总成绩
        Triple(schoolMap[it.schoolID],
            it.name,
            it.recordDataList.filter { it.role == "R" || it.role == "O" || it.role == "V" }
                .fold(0.0) { total, recordData ->
                    total + recordData.score * recordData.weight
                })
    }.sortedByDescending { it.third }


    fun getReviewTable() = teamDataList.map {
        // 学校名，队伍名，[(赛题id to 队员们),()]
        Triple(schoolMap[it.schoolID], it.name, mutableMapOf<Int, String>().apply {
            it.recordDataList.forEach { recordData ->
                this[recordData.questionID] = this.getOrDefault(recordData.questionID, "") + recordData.role
            }
        })
    }

    fun getPlayerScore() = teamDataList.map { it ->
        // 学校名，队伍名，[(姓名 to [性别，正方得分总览，反方得分总览，评方得分总览,正方平均分,反方平均分,评方平均分])]
        Triple(
            schoolMap[it.schoolID], it.name, mutableMapOf<String, MutableList<Any>>().apply {
                it.recordDataList.forEach { recordData ->

                    if (recordData.role == "R" || recordData.role == "O" || recordData.role == "V") {

                        //获取这条记录中的队员名称和性别
                        try {
                            val (playerName, playerGender) = it.playerDataList.first { playerData ->
                                playerData.id == recordData.masterID
                            }.let { playerData ->
                                listOf(playerData.name, playerData.gender)
                            }

                            //增加这条记录
                            this[playerName] = this.getOrDefault(
                                playerName, mutableListOf<Any>(
                                    playerGender, "", "", "", 0.0, 0.0, 0.0
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
                            throw Exception("未在队员名单中找到比赛记录中id为${recordData.masterID}的队员，请检查！")
                        }


                    }

                }

                //再对得到的列表进行一定处理
                this.forEach { (_, dataList) ->
                    for (i in 1..3) {
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

    fun getMaxPlayerId() = teamDataList.map { it.playerDataList.map { it.id } }.flatten().maxOf { it }

    fun mergeData(newData: Data, inplace: Boolean = false): Data {
        //队伍record的合并
        val oldTeamIdList = this.teamDataList.map { it.id }.distinct()
        val newTeamIdList = newData.teamDataList.map { it.id }.distinct()
        val totalTeamIdList = (oldTeamIdList + newTeamIdList).distinct()

        val totalTeamDataList = mutableListOf<TeamData>()
        totalTeamIdList.forEach { teamId ->
            if (teamId in oldTeamIdList) {
                val teamData = this.teamDataList.first { it.id == teamId }
                if (teamId in newTeamIdList) {
                    teamData.recordDataList += newData.teamDataList.first { it.id == teamId }.recordDataList
                    teamData.recordDataList = teamData.recordDataList.distinct().toMutableList()
                }
                totalTeamDataList.add(teamData)
            } else {
                totalTeamDataList.add(newData.teamDataList.first { it.id == teamId })
            }
        }
        return if (inplace) {
            this.apply {
                teamDataList = totalTeamDataList
            }
        } else {
            Data(totalTeamDataList, this.questionMap, this.schoolMap)
        }

    }
}


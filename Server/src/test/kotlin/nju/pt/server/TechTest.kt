package nju.pt.server

import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import org.junit.jupiter.api.Test

class TechTest {
    @Test
    fun getRoundXTeamReviewTest() {
        val roundX = 1
        val data = JsonHelper.fromJson<Data>("data.json")
        val round1TeamReviewList =
            data.teamDataList.map {
                // teamName to List<questionName to Role>()
                it.name to
                        it.recordDataList
                            .filter { it.round == roundX }
                            .map { data.questionMap[it.questionID]!! to it.role }
            }
        println(data)
    }
}
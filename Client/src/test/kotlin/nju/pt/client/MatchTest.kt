package nju.pt.client

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

private val json = Json { prettyPrint = true }

class MatchTest {
    @Test
    fun pairSetTechTest() {
        val banRuleList = listOf(
            TeamType.REPORTER to QuestionType.REFUSED,
            TeamType.REPORTER to QuestionType.REPORTED,
            TeamType.OPPONENT to QuestionType.OPPOSED,
            TeamType.OPPONENT to QuestionType.REPORTED
        )
        val testPair = TeamType.REPORTER to QuestionType.REFUSED

        assert(testPair in banRuleList)
    }

    @Test
    fun enumParseInJsonTest() {
        @kotlinx.serialization.Serializable
        data class EnumTestObj(
            val type: TeamType,
            val pair: Pair<Int, QuestionType>,
            val xxx: Int
        )
        println(json.encodeToString(EnumTestObj(TeamType.REPORTER, 5 to QuestionType.REPORTED, 5)))
    }
}
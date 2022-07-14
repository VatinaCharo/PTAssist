package nju.pt.client

enum class TeamType {
    REPORTER,
    OPPONENT,
    REVIEWER,
    OBSERVER
}

enum class QuestionType {
    OPTIONAL,
    REPORTED,
    OPPOSED,
    REFUSED;

    override fun toString(): String {
        return when (this) {
            OPTIONAL -> "P"
            REPORTED -> "nju.pt.net.R"
            OPPOSED -> "O"
            REFUSED -> "X"
        }
    }
}

enum class RuleType {
    CUPT,
    JSYPT
}

enum class RoundType(s: String) {
    NORMAL("正常模式"),
    SPECIAL("自选题模式")
}

enum class WorkMode {
    ONLINE,
    OFFLINE
}
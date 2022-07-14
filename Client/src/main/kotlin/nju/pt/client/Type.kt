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
            REPORTED -> "R"
            OPPOSED -> "O"
            REFUSED -> "X"
        }
    }
}
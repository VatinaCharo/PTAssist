interface Rule{
    /**
     * @return 赛题编号列表 包含了当前对局中的全部可选题
     */
    fun getOptionalQuestionIdList():List<Int>

    /**
     * @return 是否合法 检查队员是否能上场作为主控队员，包括了上场次数检查和当前对局的主控次数检查
     */
    fun isPlayerValid():Boolean

    /**
     * @return 分数 获取最后的统计分数
     */
    fun getScore():Double
}
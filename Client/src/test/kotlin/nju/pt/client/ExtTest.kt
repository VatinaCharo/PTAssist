package nju.pt.client

import nju.pt.kotlin.ext.rotate
import org.junit.jupiter.api.Test

class ExtTest {
    @Test
    fun listRotateTest() {
        println(listOf(1, 2, 3, 4).rotate())
        println(listOf(1, 2, 3, 4).rotate().rotate())
        println(listOf(1).rotate())
        println(listOf<Int>().rotate())
    }
}
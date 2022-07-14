package nju.pt.kotlin.ext

fun <T> List<T>.rotate() = mutableListOf(this.last()).apply { addAll(this@rotate.dropLast(1)) }
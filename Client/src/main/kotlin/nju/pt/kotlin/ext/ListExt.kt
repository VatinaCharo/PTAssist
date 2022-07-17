package nju.pt.kotlin.ext

fun <T> List<T>.rotate() = this.drop(1).toMutableList().apply { add(this@rotate.first()) }.toList()
package nju.pt.kotlin.ext


fun <T> MutableList<T>.offset(offset: Int, inplace: Boolean = false) =
    MutableList(this.size) { index ->
        this[(index - offset).mod(this.size)]
    }.let {
        if (inplace) {
            this.clear()
            this.addAll(it)
            this
        } else {
            it
        }

    }

fun <T> MutableList<T>.sortByIndexList(indexList: List<Int>, inplace: Boolean = false) =
    MutableList(this.size) { index ->
        this[indexList[index]]
    }.let {
        if (inplace) {
            this.clear()
            this.addAll(it)
            this
        } else {
            it
        }

    }
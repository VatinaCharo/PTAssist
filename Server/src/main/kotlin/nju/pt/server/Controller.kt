package nju.pt.server

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox

class IntTabCell<T> : TableCell<T, Number>() {

    override fun startEdit() {
        super.startEdit()
        graphic = HBox().apply {
            alignment = Pos.CENTER
            children.add(TextField("$item").apply {
                textProperty().addListener { _, oldValue, newValue ->
                    text = if (newValue.matches(Regex("^\\d*\$"))) newValue else oldValue
                }
                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER && text.isNotEmpty()) {
                        commitEdit(text.toInt())
                    }
                }
            })
        }
    }

    override fun cancelEdit() {
        graphic = HBox().apply {
            alignment = Pos.CENTER
            children.add(Label("$item"))
        }
        super.cancelEdit()
    }

    override fun updateItem(item: Number?, empty: Boolean) {
        if (!empty && item != null) {
            graphic = HBox().apply {
                alignment = Pos.CENTER
                children.add(Label("$item"))
            }
        }
        super.updateItem(item, empty)
    }
}

class DoubleTabCell<T> : TableCell<T, Number>() {

    override fun startEdit() {
        super.startEdit()
        graphic = HBox().apply {
            alignment = Pos.CENTER
            children.add(TextField("$item").apply {
                textProperty().addListener { _, oldValue, newValue ->
                    text = if (newValue.matches(Regex("^\\d*\\.?\\d+\$"))) newValue else oldValue
                }
                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER && text.isNotEmpty()) {
                        commitEdit(text.toDouble())
                    }
                }
            })
        }
    }

    override fun cancelEdit() {
        graphic = HBox().apply {
            alignment = Pos.CENTER
            children.add(Label("$item"))
        }
        super.cancelEdit()
    }

    override fun updateItem(item: Number?, empty: Boolean) {
        if (!empty && item != null) {
            graphic = HBox().apply {
                alignment = Pos.CENTER
                children.add(Label("$item"))
            }
        }
        super.updateItem(item, empty)
    }
}
package nju.pt.server

import javafx.beans.DefaultProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.stage.Stage
import nju.pt.R

class IntTabCell<T> : TableCell<T, Number>() {

    override fun startEdit() {
        super.startEdit()
        graphic = HBox().apply {
            alignment = Pos.CENTER
            children.add(TextField("$item").apply {
                textProperty().addListener { _, oldValue, newValue ->
                    text = if (newValue.matches(Regex("^\\d*\$"))) newValue else oldValue
                }
                focusedProperty().addListener { _, _, newValue ->
                    if (!newValue && text.isNotEmpty()){
                        commitEdit(text.toInt())
                    }
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
                    text = if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) newValue else oldValue
                }
                focusedProperty().addListener { _, _, newValue ->
                    if (!newValue && text.isNotEmpty()){
                        commitEdit(text.toInt())
                    }
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

@DefaultProperty("root")
class MyScene(root: Parent) : Scene(root) {
    init {
        stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
    }
}

class MyStage() : Stage() {
    constructor(root: Parent) : this() {
        scene = MyScene(root)
    }

    init {
        icons.add(Image(R.LOGO_PATH))

    }

    fun centerAndFocus() = this.apply{
        this.centerOnScreen()
        this.requestFocus()
    }

}

class IntegerTextField() : TextField() {
    constructor(text: String) : this() {
        this.text = text
    }

    init {
        textProperty().addListener { _, oldValue, newValue ->
            text = if (newValue.matches(Regex("^\\d*\$"))) newValue else oldValue
        }
        id = "IntegerTextField"
    }
}

class DoubleTextField() : TextField() {
    constructor(text: String) : this() {
        this.text = text
    }

    init {
        textProperty().addListener { _, oldValue, newValue ->
            text =
                if (newValue.matches(Regex("\\d*\\.\\d*")) || newValue.matches(Regex("^\\d*"))) newValue else oldValue
        }
        id = "DoubleTextField"
    }
}

class InfoAlert() : Alert(AlertType.INFORMATION) {
    init {
        dialogPane.apply {
            buttonTypes.clear()
            buttonTypes.add(ButtonType.OK)
            lookupButton(ButtonType.OK)
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
        }
    }
}

class ErrorAlert() : Alert(AlertType.ERROR) {
    init {
        dialogPane.apply {
            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)
        }
    }
}

class ConfirmAlert():Alert(AlertType.CONFIRMATION){
    var yesBtn:Button
    var noBtn:Button
    init {
        dialogPane.apply {
            buttonTypes.clear()
            buttonTypes.addAll(ButtonType.YES,ButtonType.NO)
            yesBtn = lookupButton(ButtonType.YES) as Button
            noBtn =  lookupButton(ButtonType.NO) as Button

            stylesheets.addAll(R.DEFAULT_CSS_PATH, R.SPECIAL_CSS_PATH)

            (scene.window as Stage).icons.add(Image(R.LOGO_PATH))
        }

    }
}

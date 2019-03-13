package ru.svk.windowManger

import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Stage
import java.text.SimpleDateFormat
import java.util.*

class MainWindow
{
    constructor(stage: Stage)
    {
        val table = Main.instance.programs

        table.columns.addAll(
                TableColumn<RowProgram, String>("ProgramName").also {
                    it.setCellValueFactory(PropertyValueFactory<RowProgram, String>("programName"))
                },
                TableColumn<RowProgram, String>("Time").also {
                    it.setCellValueFactory(PropertyValueFactory<RowProgram, String>("strTime"))
                    it.minWidth = 100.0
                    it.maxWidth = 100.0
                    it.resizableProperty().set(false)
                })

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        val scene = Scene(table, 400.0, 600.0)
        stage.scene = scene
        stage.show()
        stage.title = "WindowMonitor"
    }

    data class RowProgram(val programName: String, val processName: String, var time: Long, val titles: TableView<RowTitle> = TableView())
    {
        companion object
        {
            val format = SimpleDateFormat("HH:mm:ss").also { it.timeZone = TimeZone.getTimeZone("UTC") }
        }

        fun getStrTime() = format.format(time)

        data class RowTitle(val title: String, var time: Long)
        {
            fun getStrTime() = format.format(time)
        }
    }
}
package ru.svk.windowManger

import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Stage
import java.text.DecimalFormat
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
                TableColumn<RowProgram, String>("procent").also {
                    it.setCellValueFactory(PropertyValueFactory<RowProgram, String>("procent"))
                    it.minWidth = 50.0
                    it.maxWidth = 50.0
                    it.resizableProperty().set(false)
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
            val dicFormat = DecimalFormat("00.00")
        }

        fun getStrTime() = format.format(time)
        fun getProcent() = "${ dicFormat.format(Math.round((time.toFloat()/Main.instance.totalTime)*10000)/100.0) }%"

        data class RowTitle(val title: String, var time: Long)
        {
            fun getStrTime() = format.format(time)
        }
    }
}
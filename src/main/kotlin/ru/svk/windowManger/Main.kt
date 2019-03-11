package ru.svk.windowManger

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import java.awt.*
import java.net.URL
import javax.imageio.ImageIO

fun main(args: Array<String>)
{
    Application.launch(Main::class.java, *args)
}

class Main : Application()
{
    companion object {lateinit var instance: Main }
    lateinit var th: Thread

    val programs = Data.load()
    var tempTime: Long = System.currentTimeMillis()

    override fun start(stage: Stage)
    {
        Data.load()

        instance = this
        Tray.initTray(stage)
        MainWindow(stage)

        th = object : Thread()
        {
            var prewWindowText = ""

            override fun run()
            {
                while (!isInterrupted)
                {
                    if(WinUser.WINDOWINFO().also { User32.INSTANCE.GetWindowInfo(User32.INSTANCE.GetForegroundWindow(), it) }.dwStyle.let { it > 0 || it == -1778384896 })
                    {
                        val textLenght = User32.INSTANCE.GetWindowTextLength(User32.INSTANCE.GetForegroundWindow())
                        String(CharArray(textLenght + 1).also { User32.INSTANCE.GetWindowText(User32.INSTANCE.GetForegroundWindow(), it, textLenght + 1) }).let {
                            if (it != prewWindowText)
                            {
                                changeWindow(it, prewWindowText)
                                prewWindowText = it
                            }
                        }
                    }

                    sleep(1000)
                }
            }
        }
        th.start()
    }

    fun changeWindow(newWindowText: String, prewWindowText: String)
    {
        val newWindowText = newWindowText.split("-")
        val newProgramName = newWindowText.last().dropLast(1).trim()
        val newWindowTitle = newWindowText.dropLast(1).joinToString()

        val prewWindowText = prewWindowText.split("-")
        val prewProgramName = prewWindowText.last().dropLast(1).trim()
        val prewWindowTitle = prewWindowText.dropLast(1).joinToString()

        programs.items.find { it.programName == newProgramName }.also {
            if(it == null)
            {
                programs.items.add(MainWindow.RowProgram(newProgramName, 0).also {
                    it.titles.items.add(MainWindow.RowProgram.RowTitle(newWindowTitle, 0))
                })
            }
        }

        programs.items.find { it.programName == prewProgramName }?.also { program ->
            val delta = System.currentTimeMillis() - tempTime
            program.time += delta

            program.titles.items.find { it.title == prewWindowTitle }.also {
                if (it != null)
                    it.time += delta
                else
                    program.titles.items.add(MainWindow.RowProgram.RowTitle(newWindowTitle, 0))
            }
        }

        programs.columns[1].tableView.refresh()

        tempTime = System.currentTimeMillis()
    }

    override fun stop()
    {
        Data.save()
        th.interrupt()
        super.stop()
    }
}

class Tray
{
    companion object
    {
        lateinit var trayIcon: TrayIcon

        fun initTray(stage: Stage)
        {
            trayIcon = createIconTray(stage)

            Platform.setImplicitExit(false)

            stage.onCloseRequest = EventHandler {
                it.consume()
                SystemTray.getSystemTray().add(trayIcon)
                stage.hide()
            }
        }

        fun createIconTray(stage: Stage): TrayIcon
        {
            val image = ImageIO.read(URL("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTt_kwPym8HUMNboa9DiruzpBvCFG98tnUUefu1M186jcVw9SnEKQ"))
            val trayIcon = TrayIcon(image.getScaledInstance(16, -1, Image.SCALE_SMOOTH))
            trayIcon.addActionListener {
                Platform.runLater {
                    stage.show()
                    SystemTray.getSystemTray().remove(trayIcon)
                }
            }
            trayIcon.popupMenu = createTrayMenu()
            return trayIcon
        }

        fun createTrayMenu(): PopupMenu
        {
            val menu = PopupMenu()

            val exitItem = MenuItem("Exit").also {
                it.addActionListener {
                    SystemTray.getSystemTray().remove(trayIcon)
                    Platform.exit()
                }
            }

            return menu.also {
                it.add(exitItem)
            }
        }
    }
}
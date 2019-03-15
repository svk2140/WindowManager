package ru.svk.windowManger

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import java.awt.*
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
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
    var totalTime = 0L

    override fun start(stage: Stage)
    {
        Data.load()

        instance = this
        Tray.initTray(stage)
        MainWindow(stage)

        th = object : Thread()
        {
            var prewWindowText = ""
            var tempTime: Long = System.currentTimeMillis()

            override fun run()
            {
                while (!isInterrupted)
                {
                    val window = WindowInfo.getForegroundWindow()
                    if(window != null)
                        if (window.dwStyle > 0 || window.dwStyle == -1778384896)// || Desktop
                        {
                            if ((if (window.processName == "explorer.exe") "Explorer " else window.title) != prewWindowText)
                            {
                                val newWindowText = window.title.split("-")
                                var newProgramName = newWindowText.last().dropLast(1).trim()
                                val newWindowTitle = newWindowText.dropLast(1).joinToString()

                                val prewWindowText = prewWindowText.split("-")
                                var prewProgramName = prewWindowText.last().dropLast(1).trim()
                                val prewWindowTitle = prewWindowText.dropLast(1).joinToString()

                                if (window.processName == "explorer.exe") newProgramName = "Explorer"

                                programs.items.find { it.programName == newProgramName }.also {
                                    if (it == null)
                                    {
                                        programs.items.add(MainWindow.RowProgram(newProgramName, window.processName, 0).also {
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

                                Main.instance.updateData()

                                tempTime = System.currentTimeMillis()
                                this.prewWindowText = if (window.processName == "explorer.exe") "Explorer " else window.title

                                Data.save()
                            }
                        }

                    sleep(1000)
                }
            }
        }
        th.start()
    }

    fun updateData()
    {
        totalTime = 0
        programs.items.forEach { totalTime += it.time }
        programs.columns[1].tableView.refresh()
        programs.sort()
    }

    override fun stop()
    {
        Data.save()
        th.interrupt()
        super.stop()
    }

    data class WindowInfo(val title: String, val processName: String, val dwStyle: Int)
    {
        companion object
        {
            val user32 = User32.INSTANCE

            fun getForegroundWindow(): WindowInfo?
            {
                val foregroundWindow = user32.GetForegroundWindow()

                if(foregroundWindow != null)
                {
                    val dwStyle = WinUser.WINDOWINFO().also { user32.GetWindowInfo(foregroundWindow, it) }.dwStyle
                    val titleTextLenght = user32.GetWindowTextLength(foregroundWindow) + 1
                    val title = String(CharArray(titleTextLenght).also { user32.GetWindowText(foregroundWindow, it, titleTextLenght + 1) })
                    return WindowInfo(title, getProcessName(foregroundWindow)!!, dwStyle)
                }
                else
                {
                    return null
                }
            }

            fun getProcessName(foregroundWindow: WinDef.HWND): String?
            {
                var ret: String? = null
                val pid = IntByReference().also { user32.GetWindowThreadProcessId(foregroundWindow, it) }.value

                InputStreamReader(Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe /fo csv /nh").getInputStream(), Charset.forName("Cp866")).forEachLine {
                        val arr = it.split(",").map { it.replace("\"", "") }

                        if(arr[1].toInt() == pid)
                        {
                            ret = arr[0]
                        }
                }

                return ret
            }
        }
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
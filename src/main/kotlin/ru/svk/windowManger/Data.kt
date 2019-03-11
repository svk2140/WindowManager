package ru.svk.windowManger

import javafx.scene.control.TableView
import net.querz.nbt.CompoundTag
import net.querz.nbt.ListTag
import net.querz.nbt.NBTUtil
import java.nio.file.Paths

object Data
{
    val file = Paths.get("data.nbt").toFile()

    fun save()
    {
        val nbt = ListTag(CompoundTag::class.java)

        for (program in Main.instance.programs.items)
        {
            nbt.add(CompoundTag().also {
                it.putString("programName", program.programName)
                it.putLong("time", program.time)
                it.put("titles", ListTag(CompoundTag::class.java).also {
                    for (title in program.titles.items)
                    {
                        it.add(CompoundTag().also {
                            it.putString("title", title.title)
                            it.putLong("time", title.time)
                        })
                    }
                })
            })
        }

        NBTUtil.writeTag(nbt, file)
    }

    fun load(): TableView<MainWindow.RowProgram>
    {
        val programs = TableView<MainWindow.RowProgram>()

        if(file.exists())
        {
            val nbt = NBTUtil.readTag(file) as ListTag<CompoundTag>

            for (program in nbt)
            {
                programs.items.add(MainWindow.RowProgram(
                        program.getString("programName"),
                        program.getLong("time"),
                        TableView<MainWindow.RowProgram.RowTitle>().also { table ->
                            program.getListTag("titles").forEach {
                                val nbt = it as CompoundTag
                                table.items.add(MainWindow.RowProgram.RowTitle(nbt.getString("title"), nbt.getLong("time")))
                            }
                        }))
            }

            return programs
        }

        return programs
    }
}
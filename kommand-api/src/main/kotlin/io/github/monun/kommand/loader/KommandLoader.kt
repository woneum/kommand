/*
 * Kommand
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.kommand.loader

import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException

internal object KommandLoader {
    private val compatVersion by lazy {
        "v" + Bukkit.getServer().minecraftVersion.replace('.', '_')
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> loadCompat(type: Class<T>, vararg initArgs: Any? = emptyArray()): T {
        val packageName = type.`package`.name
        val className = "NMS${type.simpleName}"
        val parameterTypes = initArgs.map {
            it?.javaClass
        }.toTypedArray()

        val candidates = ArrayList<String>(2)
        candidates.add("$packageName.internal.compat.$compatVersion.$className")

        val lastDot = packageName.lastIndexOf('.')
        if (lastDot > 0) {
            val superPackageName = packageName.substring(0, lastDot)
            val subPackageName = packageName.substring(lastDot + 1)
            candidates.add("$superPackageName.internal.compat.$compatVersion.$subPackageName.$className")
        }

        return try {
            val nmsClass = candidates.firstNotNullOfOrNull { candidate ->
                try {
                    Class.forName(candidate, true, type.classLoader).asSubclass(type)
                } catch (exception: ClassNotFoundException) {
                    null
                }
            } ?: throw ClassNotFoundException("Not found nms library class: $candidates")
            val constructor = kotlin.runCatching {
                nmsClass.getConstructor(*parameterTypes)
            }.getOrNull()
                ?: throw UnsupportedOperationException("${type.name} does not have Constructor for [${parameterTypes.joinToString()}]")
            constructor.newInstance(*initArgs) as T
        } catch (exception: ClassNotFoundException) {
            throw UnsupportedOperationException(
                "${type.name} does not support this version: $compatVersion",
                exception
            )
        } catch (exception: IllegalAccessException) {
            throw UnsupportedOperationException("${type.name} constructor is not visible")
        } catch (exception: InstantiationException) {
            throw UnsupportedOperationException("${type.name} is abstract class")
        } catch (exception: InvocationTargetException) {
            throw UnsupportedOperationException(
                "${type.name} has an error occurred while creating the instance",
                exception
            )
        }
    }
}
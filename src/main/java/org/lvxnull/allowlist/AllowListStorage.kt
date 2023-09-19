/*This file is part of allowlist.

  Allowlist is free software: you can redistribute it and/or modify it under the
  terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  Allowlist is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  more details.

  You should have received a copy of the GNU General Public License along with
  allowlist. If not, see <https://www.gnu.org/licenses/>. */

package org.lvxnull.allowlist

import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.nio.file.Path

class AllowListStorage(configRoot: Path): AutoCloseable, Iterable<String> {
    private companion object {
        private val nameRegex = Regex("^\\w{3,16}$")
        private val logger = LogManager.getLogger("AllowList")
    }

    private val allowed: MutableSet<String> = mutableSetOf()
    private val listFile = configRoot.resolve("allowlist.txt").toFile().apply {
        createNewFile()
    }
    private var loaded = false
    val size: Int
        get() = allowed.size

    /**
     * Loads the allowlist from disk once. Subsequent calls will have no effect.
     */
    @Throws(IOException::class)
    fun load() {
        if(loaded) return
        listFile.bufferedReader().use {
            var nth = 1
            var line = it.readLine()?.trim()
            while(line != null) {
                try {
                    add(line)
                } catch(e: IllegalArgumentException) {
                    logger.warn("Line {}: Ignoring invalid name '{}'", nth, line)
                }
                line = it.readLine()?.trim()
                ++nth
            }
        }
        loaded = true
    }

    fun save() {
        listFile.bufferedWriter().use {
            for(n in allowed) {
                it.write(n)
                it.newLine()
            }
        }
    }

    override fun close() = save()

    override fun iterator() = allowed.asIterable().iterator()

    fun isAllowed(name: String?) = allowed.contains(name)

    fun add(name: String): Boolean {
        if(nameRegex.matchEntire(name) == null) {
            throw IllegalArgumentException("Invalid player name.")
        }
        return allowed.add(name)
    }

    fun clear() = allowed.clear()

    fun remove(name: String) = allowed.remove(name)
}

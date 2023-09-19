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

import java.nio.file.Path
import java.sql.DriverManager

class AllowListStorage(configRoot: Path): AutoCloseable {
    private val dbPath = configRoot.resolve("allowlist.db")
    private val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath").apply {
        createStatement().use {
            it.execute("""CREATE TABLE IF NOT EXISTS allowlist(
            |  name TEXT PRIMARY KEY,
            |  timestamp INTEGER
            |)""".trimMargin())
        }
    }

    fun isAllowed(name: String): Boolean {
        connection.prepareStatement("SELECT 1 FROM allowlist WHERE name = ? LIMIT 1").use {
            it.setString(1, name)
            return it.executeQuery().next()
        }
    }

    override fun close() {
        connection.close()
    }
}

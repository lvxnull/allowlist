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

import com.mojang.authlib.GameProfile
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.sql.Connection
import java.sql.DriverManager
import kotlin.io.path.absolute
import kotlin.reflect.jvm.isAccessible

@Suppress("UNUSED")
object AllowList: ModInitializer {
    val meta: ModMetadata = FabricLoader.getInstance().getModContainer("allowlist").get().metadata
    val logger: Logger = LogManager.getLogger("AllowList")

    private val connection: Connection by lazy {
        val dbPath = FabricLoader.getInstance().configDir.resolve("allowlist.db").absolute()
        DriverManager.getConnection("jdbc:sqlite:$dbPath").apply {
            createStatement().use {
                it.execute("""CREATE TABLE IF NOT EXISTS allowlist(
                |  name TEXT PRIMARY KEY,
                |  timestamp INTEGER
                |)""".trimMargin())
            }
        }
    }
    override fun onInitialize() {
        logger.info("Started allowlist ${meta.version}")

        ServerLifecycleEvents.SERVER_STOPPING.register {
            onClose()
        }
    }

    fun processPlayer(profile: GameProfile): Boolean {
        connection.prepareStatement("SELECT 1 FROM allowlist WHERE name = ? LIMIT 1").use {
            it.setString(1, profile.name)
            return it.executeQuery().next()
        }
    }

    private fun onClose() {
        logger.info("Stopping allowlist ${meta.version}")
        if((::connection.apply { isAccessible = true }.getDelegate() as Lazy<*>).isInitialized()) {
            connection.close()
        }
    }
}

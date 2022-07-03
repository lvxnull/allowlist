<h1 align="center">AllowList</h1>
<p align="center">
  <a title="Fabric API" href="https://github.com/FabricMC/fabric">
    <img src="https://i.imgur.com/Ol1Tcf8.png" width="151" height="50" />
  </a>
  <a title="Fabric Language Kotlin" href="https://github.com/FabricMC/fabric-language-kotlin" target="_blank" rel="noopener noreferrer">
    <img src="https://i.imgur.com/c1DH9VL.png" width="171" height="50" />
  </a>
</p>

Allowlist is a simple server-side minecraft fabric mod which prevents unauthorized users
from entering the game. Unlike minecraft's built in whitelist commands, this mod
uses user names instead of instead of UUIDs.

## Configuration

Allowed players are stored in `/path/to/server/config/allowlist.txt`, one
user name per line. Players can be added to this file directly when the server
is stopped or through `/al add <player>` command while it's running.

**NOTE: Don't modify the file while the server is running. Your edits won't be saved.**

## License
This software is distributed under the GNU General Public License, Version 3.0
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

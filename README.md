<h1 align="center">AllowList</h1>

Allowlist is a simple server-side minecraft quilt mod which prevents
unauthorized users from entering the game. Unlike minecraft's built in whitelist
commands, this mod uses user names instead of instead of UUIDs.

![server](https://img.shields.io/badge/environment-server-orangered?style=flat-square)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
## Configuration

Allowed players are stored in `/path/to/server/config/allowlist/allowlist.txt`, one
user name per line. Players can be added to this file directly when the server
is stopped or through `/al add <player>` command while it's running.

## License
This software is distributed under the MIT license

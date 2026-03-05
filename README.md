# PluginSpoofer
A Velocity &amp; Bukkit Plugin to Spoof Client the Server Plugin List  
Velocity support is still planned

## Features
依赖于`packetevents`，可以防范某些外挂发包获取插件列表，同时可自定义伪造的插件列表，支持 Paper 格式

## Config
```yml
debug: false
modern-server: true  # 伪造高版本服务器特性（1.13+）
block-slash-completion: true
block-non-minecraft-namespaces: true
blocked-commands:
  - "plugins"
  - "pl"
  - "version"
  - "ver"
  - "about"
  - "icanhasbukkit"
  - "bukkit:plugins"
  - "bukkit:pl"
  - "bukkit:version"
  - "bukkit:ver"
  - "bukkit:about"

plugins:
  enabled: true
  paper:
    enabled:
      - "ViaVersion"
      - "ProtocolLib"
    legacy:
      - "ViaBackwards"
    disabled:
      - "SkinsRestorer"
  bukkit:
    enabled:
      - "EssentialsX"
      - "LuckPerms"
      - "PlaceholderAPI"
    legacy:
      - "GroupManager"
    disabled:
      - "WorldGuard"
```

## Commands
- /ps
- /ps help
- /ps reload
- /ps version

## License
Under AGPL-3.0

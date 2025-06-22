# PluginSpoofer
A Velocity &amp; Bukkit Plugin to Spoof Client the Server Plugin List

## Features
依赖于`packetevents`，可以防范某些外挂发包获取插件列表，同时可自定义伪造的插件列表，支持 Paper 格式

## Config
```yml
debug: false
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
  hover-tooltips: true  # 新点的 Paper 会有一个悬停文字，用于解释某些标记，仅限 Paper 且带有 Adventure API 的版本
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

unknown-msg: "Unknown command. Type \"/help\" for help."
```

## Commands
- /ps
- /ps help
- /ps reload

## License
Under AGPL-3.0
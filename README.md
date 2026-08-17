# GrimConfig

Toggle individual [GrimAC](https://modrinth.com/plugin/grimanticheat) checks on or off — like
[GrimCheckup](https://modrinth.com/plugin/grimcheckup), but built for **Minecraft 1.21.11**
and editable **in-game** through a clickable GUI and commands, not just the config file.

When a check is disabled, GrimConfig cancels its flags, its prediction offsets, and any
punishment commands Grim would run for it — the check effectively stops existing without
touching Grim's own config.

## Requirements

- Paper/Purpur/Spigot **1.21.11** (Java 21)
- **GrimAC** installed (hard dependency)

## Install

Drop `target/GrimConfig-1.0.0.jar` into your server's `plugins/` folder alongside GrimAC and restart.

## Usage

| Command | What it does |
|---|---|
| `/grimconfig` | Opens the clickable GUI (lime = enabled, gray = disabled) |
| `/grimconfig list` | Lists every check, color-coded |
| `/grimconfig enable <check\|all>` | Enables a check (or everything) |
| `/grimconfig disable <check\|all>` | Disables a check (or everything) |
| `/grimconfig toggle <check>` | Flips a check |
| `/grimconfig status <check>` | Shows one check's state |
| `/grimconfig reload` | Reloads `config.yml` after hand-editing |

Aliases: `/gcfg`, `/grimchecks`. Permission: `grimconfig.use` (default: op).

All changes apply instantly (no restart) and are saved to `plugins/GrimConfig/config.yml`,
which is a simple `CheckName: true/false` list — the same shape as GrimCheckup's config.
The check list auto-populates from whatever checks your GrimAC version actually loads,
discovered as players join, so it stays correct across Grim updates.

## Building

```
mvn package
```

Produces `target/GrimConfig-1.0.0.jar`. Compiles against `paper-api 1.21.11-R0.1-SNAPSHOT`
and `GrimAPI 1.6.0.9` (from `repo.grim.ac`), nothing is shaded.

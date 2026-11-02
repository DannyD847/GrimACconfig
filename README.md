# GrimConfig

Toggle individual [GrimAC](https://modrinth.com/plugin/grimanticheat) checks on or off — like
[GrimCheckup](https://modrinth.com/plugin/grimcheckup), but for **every Minecraft version from
1.21 through the newest release (26.x)** in a single jar, and editable **in-game** through a
clickable GUI and commands, not just the config file.

When a check is disabled, GrimConfig cancels its flags, its prediction offsets, and any
punishment commands Grim would run for it — the check effectively stops existing without
touching Grim's own config.

## Requirements

- A **Paper-based** server (Paper, Purpur, Pufferfish, …) on any version from **1.21** to the
  newest (1.21.x, 26.1, 26.2, 26.3 — one jar covers them all). Plain Spigot is not supported: the GUI
  uses Paper's Adventure text API.
- Java 21+ (whatever your server version already needs)
- A current **GrimAC** build (hard dependency). GrimConfig talks to Grim through its modern
  event-bus API, so it needs a Grim release from 2025 or later — Grim itself runs on every
  Minecraft version, so just keep it up to date.

## Install

Drop `target/GrimConfig-1.1.1.jar` into your server's `plugins/` folder alongside GrimAC and restart.

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

Produces `target/GrimConfig-1.1.1.jar`. Needs JDK 21. Compiles against the **oldest** supported
API (`paper-api 1.21-R0.1-SNAPSHOT`) on purpose, so the jar can't accidentally depend on anything
newer, plus `GrimAPI 1.6.0.13` (from `repo.grim.ac`). Nothing is shaded.

To double-check the source against a newer server API (needs a JDK matching that API, e.g. JDK 25
for 26.x):

```
mvn compile -Dpaper.version=26.2.build.135-stable -Dmaven.compiler.release=25
```

Verified compiling against 1.21, 1.21.11, 26.1.2, 26.2 and 26.3.

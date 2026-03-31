# Color Variants 🎨

[![CI Status](https://img.shields.io/github/actions/workflow/status/deftorch/colorvariants/ci.yml?branch=develop&label=build&logo=github)](https://github.com/deftorch/colorvariants/actions)
[![Code Coverage](https://img.shields.io/codecov/c/github/deftorch/colorvariants?logo=codecov)](https://codecov.io/gh/deftorch/colorvariants)
[![License](https://img.shields.io/github/license/deftorchcolorvariants)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?logo=minecraft)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.14+-blue)](https://fabricmc.net/)
[![Forge](https://img.shields.io/badge/Forge-Compatible-orange)](https://files.minecraftforge.net/)

> **⚠️ DEVELOPMENT STATUS:** This mod is currently in active development (v0.x) and **not production-ready**. Core rendering features are being implemented. See [Roadmap](#-roadmap) for current progress.

A powerful Minecraft mod that allows you to **recolor any block** in the game using an intuitive color picker and professional tools. Built with a modern multiloader architecture supporting both Fabric and Forge.

---

## ✨ Features

### 🎨 Core Tools

| Tool | Description | Status |
|------|-------------|--------|
| **Color Wand** | Paint individual blocks with custom colors | 🔄 In Progress |
| **Area Selector** | Select and recolor entire regions | 🔄 In Progress |
| **Color Palette** | Save and reuse your favorite colors | ✅ Working |
| **Eyedropper** | Sample colors from existing blocks | ✅ Working |

### 🖌️ Advanced Features

- **HSV Color Picker** — Intuitive color selection with hue, saturation, and value sliders
- **40+ Color Presets** — Quick access to common color schemes (Pastel, Neon, Earth Tones, etc.)
- **Undo/Redo System** — Revert mistakes easily (up to 50 actions)
- **Multiplayer Support** — Color changes sync across server and clients
- **Per-Block Storage** — Colors persist through world saves/reloads
- **Multiloader Architecture** — Single codebase for both Fabric and Forge

### 🔮 Planned Features

- ✅ Native Sodium/Embeddium compatibility via FRAPI
- ✅ Permission system integration (FTB Chunks, GriefPrevention, etc.)
- ✅ WorldEdit/Litematica integration
- ✅ Shader compatibility (Iris, Optifine)
- ⏳ Custom gradients and patterns
- ⏳ Color animations
- ⏳ Texture preservation mode

---

## 📸 Screenshots

> **Note:** Visual examples will be added as rendering implementation is completed.

```
[Color Picker Interface]  [Area Selection]  [Colored Builds]
     Coming Soon             Coming Soon        Coming Soon
```

---

## 📥 Installation

### Requirements

- **Minecraft:** 1.20.1 (Java Edition)
- **Java:** 17 or higher
- **Loader:** Fabric 0.14+ *or* Forge/NeoForge (latest for 1.20.1)

### For Players

#### Fabric Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. **(Recommended)** Install [Indium](https://modrinth.com/mod/indium) if using Sodium
4. Download Color Variants from [Releases](https://github.com/deftorch/colorvariants/releases)
5. Place the `.jar` file in your `.minecraft/mods` folder

#### Forge Installation

1. Install [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
2. Download Color Variants from [Releases](https://github.com/deftorch/colorvariants/releases)
3. Place the `.jar` file in your `.minecraft/mods` folder

### Compatibility

| Mod | Status | Notes |
|-----|--------|-------|
| Sodium/Embeddium | 🔄 In Progress | Requires Indium (temporary) |
| Iris Shaders | ⚠️ Partial | Some shaders may not display colors correctly |
| Optifine | ⚠️ Limited | Use Sodium + Iris instead (recommended) |
| WorldEdit | ✅ Planned | Integration coming in v0.5 |
| Litematica | ✅ Planned | Integration coming in v0.6 |

---

## 🎮 Usage

### Basic Usage

1. **Craft the Color Wand** (recipe: TBD)
2. **Right-click** any block while holding the wand
3. **Select a color** in the picker that appears
4. Click **Apply** to recolor the block

### Area Selection

1. **Craft the Area Selector** (recipe: TBD)
2. **Right-click** the first corner of your region
3. **Right-click** the second corner
4. **Open the color picker** and choose your color
5. Click **Apply Area** to recolor all blocks in the selection

### Color Palette

1. **Save colors** by clicking the save icon in the color picker
2. **Access saved colors** from the palette menu
3. **Share palettes** by copying the palette code

### Commands

```
/color help                    — Show command help
/color preset <name>           — Apply a preset color scheme
/color undo                    — Undo last color change
/color redo                    — Redo undone change
/color clear <radius>          — Remove colors in radius
/color info                    — Show color at targeted block
```

---

## 👩‍💻 For Developers

### Building from Source

```bash
git clone https://github.com/deftorch/colorvariants.git
cd colorvariants
./gradlew build
```

Output JARs: `fabric/build/libs/` and `forge/build/libs/`

### Development Setup

```bash
# Run Fabric development client
./gradlew :fabric:runClient

# Run Fabric development server
./gradlew :fabric:runServer

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport
```

### Project Structure

```
colorvariants/
├── common/           ← Shared logic (platform-agnostic)
│   ├── src/main/
│   │   ├── java/     ← Core mod code
│   │   └── resources/
│   └── src/test/     ← Unit tests
├── fabric/           ← Fabric-specific implementation
├── forge/            ← Forge-specific implementation
├── buildSrc/         ← Gradle build scripts
└── .github/
    ├── workflows/    ← CI/CD automation
    └── JULES_TASKS/  ← AI agent tasks
```

### Architecture

This mod uses the **Service Locator pattern** for platform abstraction:

```java
// ✅ Correct: Platform-agnostic in common/
Services.PLATFORM.getPlatformName();
Services.REGISTRY.registerItem("color_wand", ...);

// ❌ Wrong: Direct platform API in common/
FabricLoader.getInstance().getEnvironmentType();
```

**Key Design Patterns:**
- Service Locator (platform abstraction)
- Strategy (color transforms)
- Observer (network synchronization)
- Memento (undo/redo system)

---

## 🗺️ Roadmap

### Phase 1: Core Functionality (Current — v0.1-0.3)

- [x] Project structure and multiloader setup
- [x] Basic items (wand, palette, eyedropper, area selector)
- [x] Color picker GUI
- [x] Data persistence layer
- [ ] **Rendering system** (in progress) — Priority #1
- [ ] Server-side validation and security
- [ ] Multiplayer bug fixes

**ETA:** 2-3 months

### Phase 2: Compatibility & Polish (v0.4-0.7)

- [ ] Sodium/Embeddium native support (FRAPI)
- [ ] Permission system integration
- [ ] Comprehensive test coverage (80%+)
- [ ] Performance optimization
- [ ] Documentation completion

**ETA:** 3-4 months

### Phase 3: Advanced Features (v0.8-1.0)

- [ ] WorldEdit integration
- [ ] Litematica integration
- [ ] Gradients and patterns
- [ ] Color animations
- [ ] Resource pack compatibility

**ETA:** 4-6 months

**Total Development Timeline:** 6-12 months to v1.0

For detailed technical roadmap, see [Implementation Guide](docs/guide/04_Implementation_Guide.md).

---

## 🤝 Contributing

We welcome contributions! This project uses **Google Jules** (AI agent) for automated code analysis and fixes.

### For Human Contributors

1. Read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines
2. Fork the repository
3. Create a feature branch: `git checkout -b feat/your-feature`
4. Follow our [coding standards](CONTRIBUTING.md#architecture-rules)
5. Ensure tests pass: `./gradlew test`
6. Submit a Pull Request to `develop` branch

### For AI Agents

1. Read [AGENTS.md](AGENTS.md) for complete context
2. Use prompts from [INSTRUKSI_COPAS.md](docs/INSTRUKSI_COPAS.md)
3. Follow [daily instructions](.github/JULES_TASKS/DAILY_INSTRUCTIONS.md)
4. Always create separate PRs for each fix
5. Never merge directly to `main` or `develop`

### Code Quality Standards

- **Test Coverage:** Minimum 70% line coverage
- **Build:** Must pass `./gradlew build` with zero errors
- **Code Style:** Follow Checkstyle rules (see `config/checkstyle/`)
- **Documentation:** Public methods must have Javadoc

---

## 📊 Project Status

| Metric | Status |
|--------|--------|
| **Overall Completion** | 40% |
| **Test Coverage** | Target: 70%+ |
| **Known Issues** | [86 documented](docs/guide/03_Bug_Catalog.md) |
| **P0 Critical Issues** | 17 (actively being fixed) |
| **Build Status** | ![Build](https://img.shields.io/badge/build-passing-success) |

### Current Focus

1. 🔴 **Rendering System** — Implementing vertex coloring for visible block colors
2. 🔴 **Multiplayer Fixes** — Fixing area selector shared state bug
3. 🔴 **Security** — Adding server-side validation for all packets

See [Daily Analysis Issues](https://github.com/deftorch/colorvariants/issues?q=label%3Adaily-analysis) for live progress.

---

## 📜 License

This project is licensed under the **MIT License** — see [LICENSE](LICENSE) for details.

```
Copyright (c) 2026 deftorch

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🙏 Acknowledgments

### Technologies

- [Minecraft](https://www.minecraft.net/) by Mojang Studios
- [Fabric Loader](https://fabricmc.net/) — Modern, lightweight mod loader
- [Forge/NeoForge](https://neoforged.net/) — Classic mod loader
- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) — Runtime code modification

### Inspiration

- [ColorMatic](https://modrinth.com/mod/colormatic) — Color customization for Fabric
- [Chroma](https://www.curseforge.com/minecraft/mc-mods/chroma-forge) — RGB effects for blocks
- [Tweakeroo](https://www.curseforge.com/minecraft/mc-mods/tweakeroo) — Inspiration for tool design

### Special Thanks

- **Google Jules** — AI agent assisting with code quality and bug fixes
- **Anthropic Claude** — Documentation and architecture analysis
- **Open Source Community** — Testing and feedback

---

## 💬 Community & Support

- **Discord:** [Join our server](https://discord.gg/deftorch) (coming soon)
- **Issues:** [GitHub Issues](https://github.com/deftorch/colorvariants/issues)
- **Discussions:** [GitHub Discussions](https://github.com/deftorch/colorvariants/discussions)
- **Wiki:** [Documentation](https://github.com/deftorch/colorvariants/wiki) (coming soon)

### Reporting Bugs

Please use our [bug report template](.github/ISSUE_TEMPLATE/bug_report.md) and include:
- Minecraft version, loader (Fabric/Forge), and mod version
- Steps to reproduce
- Full error logs (if applicable)
- Whether Sodium/Optifine is installed

### Feature Requests

Open a [feature request issue](https://github.com/deftorchcolorvariants/issues/new?labels=enhancement) with:
- Clear description of the feature
- Use case / rationale
- Any relevant mockups or examples

---

## 📈 Development Stats

```
Total Files:        48 Java classes + resources
Lines of Code:      ~7,200
Test Coverage:      Target 80%
Issues Tracked:     86 (17 critical, 35 major)
Development Time:   Est. 6-12 months to v1.0
```

---

## 🔗 Links

- **CurseForge:** Coming after v1.0 release
- **Modrinth:** Coming after v1.0 release
- **Source Code:** [GitHub](https://github.com/deftorch/colorvariants)
- **Documentation:** [Technical Docs](docs/guide/)
- **CI/CD Status:** [GitHub Actions](https://github.com/deftorch/colorvariants/actions)

---

<div align="center">

**Made with ❤️ by deftorch**

If you find this mod useful, please consider ⭐ starring the repository!

[![Star History](https://img.shields.io/github/stars/deftorch/colorvariants?style=social)](https://github.com/deftorch/colorvariants/stargazers)

</div>

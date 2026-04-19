---
title: "TODO: Implement multi-loader config system"
labels: ["jules-task"]
---

### Description
There is a TODO found in `common/src/main/java/com/colorvariants/config/ModConfig.java` at line 23:
```java
// TODO: Implement multi-loader config system
```

The current stub configuration needs to be replaced with a robust multi-loader compatible config system (e.g., using Cloth Config, ForgeConfigSpec, or an independent JSON-based system that works seamlessly on both Fabric and Forge).

### Tasks
- [ ] Evaluate config libraries for multi-loader compatibility.
- [ ] Implement a unified config reading/writing mechanism in the `common` module.
- [ ] Ensure config values reflect in real-time or upon game start.
- [ ] Remove the TODO comment in `ModConfig.java`.

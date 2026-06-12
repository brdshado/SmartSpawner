---
title: GUI Layout API
description: API for registering and overriding GUI layouts programmatically or via YAML.
---
## GUI Layout API

SmartSpawner provides a Developer API that allows external plugins to override or replace the built-in GUI layouts (`main_gui.yml`, `storage_gui.yml`, `sell_confirm_gui.yml`) at runtime.

## Overview

There are two ways to interact with GUI layouts through the API:

1. **Global Layout Registration** — Register a complete layout (by code or YAML) that server admins can select via `gui_layout` in `config.yml`.
2. **Per-Spawner Layout Provider** — Dynamically choose a layout for each spawner/player combination (e.g., based on island, rank, etc.).

---

## Global Layout Registration

Registering a layout globally makes it available as a named layout that can be used across the server.

### Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getLayoutRegistry()` | Gets the layout registry | `GuiLayoutRegistry` |
| `registerLayout(String, GuiLayoutData, GuiLayoutData, GuiLayoutData)` | Registers a programmatic layout | `boolean` |
| `registerLayoutFromYaml(String, File, File, File)` | Registers a layout from YAML files | `boolean` |
| `unregisterLayout(String)` | Unregisters a layout | `boolean` |
| `isRegistered(String)` | Checks if a layout is registered | `boolean` |
| `getRegisteredLayouts()` | Gets all registered layout names | `Set<String>` |

### Registering a Programmatic Layout

You can build layouts using the builder API:

```java
import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.SmartSpawnerProvider;
import github.nighter.smartspawner.api.gui.*;
import org.bukkit.Material;

SmartSpawnerAPI api = SmartSpawnerProvider.getAPI();

// Build the main GUI layout
GuiLayoutData mainGui = new GuiLayoutBuilder()
    .type(GuiLayoutType.MAIN_GUI)
    .addButton("slot_11", new GuiButtonBuilder()
        .slot(11)
        .material(Material.CHEST)
        .action("click", "open_storage")
        .build())
    .addButton("slot_14", new GuiButtonBuilder()
        .slot(14)
        .material(Material.PLAYER_HEAD)
        .infoButton(true)
        .customTexture("df5de940bfe499c59ee8dac9f9c3919e7535eff3a9acb16f4842bf290f4c679f")
        .action("left_click", "sell_and_exp")
        .action("right_click", "open_stacker")
        .build())
    .addButton("slot_15", new GuiButtonBuilder()
        .slot(15)
        .material(Material.EXPERIENCE_BOTTLE)
        .action("click", "collect_exp")
        .build())
    .build();

// Register the layout (storage and sell confirm can be null)
GuiLayoutRegistry registry = api.getLayoutRegistry();
registry.registerLayout("myplugin_custom", mainGui, null, null);
```

After registration, server admins can select it in `config.yml`:

```yaml
gui_layout: "myplugin_custom"
```

### Registering a Layout from YAML

Your plugin can ship layout YAML files in its resources and register them:

```java
SmartSpawnerAPI api = SmartSpawnerProvider.getAPI();

File mainGui = new File(getDataFolder(), "layouts/main_gui.yml");
File storageGui = new File(getDataFolder(), "layouts/storage_gui.yml");
File sellConfirmGui = new File(getDataFolder(), "layouts/sell_confirm_gui.yml");

api.getLayoutRegistry().registerLayoutFromYaml("myplugin_custom", mainGui, storageGui, sellConfirmGui);
```

The YAML format must match the built-in SmartSpawner layout files.

---

## Per-Spawner Layout Provider

A per-spawner layout provider lets you dynamically choose a layout based on the spawner, player, or any external condition.

### Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `setSpawnerLayoutProvider(SpawnerGuiLayoutProvider)` | Sets the active provider | `void` |
| `clearSpawnerLayoutProvider()` | Clears the provider | `void` |

### Setting a Provider

```java
import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.SmartSpawnerProvider;
import github.nighter.smartspawner.api.gui.*;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import org.bukkit.entity.Player;

SmartSpawnerAPI api = SmartSpawnerProvider.getAPI();

api.setSpawnerLayoutProvider(new SpawnerGuiLayoutProvider() {
    private final GuiLayoutData premiumMainGui = buildPremiumMainGui(); // build once

    @Override
    public GuiLayoutData getLayout(SpawnerDataDTO spawner, Player player, GuiLayoutType type) {
        // Example: premium island members get a custom layout
        if (type == GuiLayoutType.MAIN_GUI && isPremiumIsland(player)) {
            return premiumMainGui;
        }
        return null; // Use default layout resolution
    }

    @Override
    public String getProviderName() {
        return "MyIslandPlugin";
    }
});
```

**Return `null`** from `getLayout()` to fall back to the default resolution:
1. Global registered layout (if `gui_layout` matches a registered name)
2. File-based layout from `gui_layouts/` directory

---

## GuiLayoutType

```java
public enum GuiLayoutType {
    MAIN_GUI,
    STORAGE_GUI,
    SELL_CONFIRM_GUI
}
```

Use this enum to specify which GUI type your provider or registered layout targets.

---

## GuiButtonBuilder Actions

Common action strings used in `GuiButtonBuilder.action(String clickType, String action)`:

| Action | Description |
|--------|-------------|
| `open_storage` | Opens the spawner storage GUI |
| `open_stacker` | Opens the spawner stacker GUI |
| `collect_exp` | Collects stored experience |
| `sell_and_exp` | Sells items and collects exp (requires sell integration) |
| `sell_all` | Sells all items without exp collection |
| `cancel` | Cancels sell confirmation |
| `confirm` | Confirms sell |
| `previous_page` | Goes to previous storage page |
| `next_page` | Goes to next storage page |
| `take_all` | Takes all items from storage |
| `sort_items` | Opens item sort/filter GUI |
| `drop_page` | Drops all items on current page |
| `open_filter` | Opens item filter GUI |
| `return_main` | Returns to main spawner GUI |
| `none` | No action (display-only button) |

Click types:
- `click` — any click
- `left_click`
- `right_click`
- `shift_left_click`
- `shift_right_click`

Button slots use the same **1-based numbering** as the YAML layout files:
- Main and sell-confirm GUI: `1` through `27`
- Storage control row: `1` through `9` (mapped to inventory slots `45` through `53`)

---

## Important Notes

- **Thread safety**: `GuiLayoutRegistryImpl` uses `ConcurrentHashMap`. `GuiLayoutData` and `GuiButtonData` are immutable.
- **Backward compatibility**: Existing `gui_layouts/` files and the `gui_layout` config setting continue to work unchanged.
- **Layout priority**: When a GUI is opened, the resolution order is:
  1. Per-spawner provider (if set and returns non-null)
  2. Global registered layout (if `gui_layout` matches a registered name)
  3. File-based layout (fallback)
- **Storage GUI limitation**: Because the storage inventory is shared among all viewers, per-player layout providers should not change slot positions between players for the same spawner. Item materials and actions can vary, but slot positions must remain consistent.

<br>
<br>

---

*Last update: June 12, 2026*

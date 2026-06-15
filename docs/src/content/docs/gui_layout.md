---
title: GUI Layout Configuration
description: How to customize spawner GUIs, including custom player head textures
---

SmartSpawner allows you to fully customize the appearance and behavior of spawner GUIs using layout files. These files are located in the `plugins/SmartSpawner/gui_layouts/` directory.

## Directory Structure

```text
plugins/SmartSpawner/gui_layouts/
├── default/
│   ├── main_gui.yml
│   ├── storage_gui.yml
│   └── sell_confirm_gui.yml
├── DonutSMP/
│   └── ...
└── DonutSMP_v2/
    └── ...
```

You can select which layout to use by changing the `gui_layout` value in your main `config.yml` file.

## Button Cooldowns

Every layout button supports an optional per-player cooldown:

```yaml
slot_12:
  material: CHEST
  enabled: true
  cooldown: "2s"
  click:
    action: "open_storage"
```

The value uses the same tick-based duration format as `config.yml`:

- `20` means 20 server ticks.
- `5s`, `10m`, `1h`, `2d`, `1w`, `1mo`, and `1y` are supported.
- Compound values can use underscores, for example `1m_30s`.
- Missing or `0` means no cooldown.

Cooldowns are not added to bundled buttons by default. Add `cooldown` only to the buttons that need it. The cooldown is tracked separately for each player, GUI type, and button key. A click rejected by cooldown does not run the action or play its configured sound, and sends the general `action_not_ready` message with the `{time}` placeholder.

All actionable GUI buttons also have a separate built-in 300 ms anti-spam debounce, even when no `cooldown` is configured. Anti-spam is checked before the button cooldown and does not replace or disable it.

Cooldown can also be overridden inside a conditional branch:

```yaml
slot_14:
  material: PLAYER_HEAD
  enabled: true
  if:
    sell_integration:
      cooldown: "3s"
      click:
        action: "sell_and_exp"
    no_sell_integration:
      click:
        action: "open_stacker"
```

## Button Click Sounds

Navigate buttons use one sound when navigation is accepted:

```yaml
slot_1:
  material: ARROW
  enabled: true
  click:
    action: "previous_page"
    sound: ui.button.click
```

Action buttons use separate sounds for their final result:

```yaml
slot_6:
  material: BUNDLE
  enabled: true
  click:
    action: "take_all"
    sound_success:
      name: entity.item.pickup
      volume: 1.0
      pitch: 1.0
    sound_fail: block.note_block.pling
```

`volume` and `pitch` default to `1.0`. The success or fail sound is played only after the handler knows the actual result, including asynchronous sell actions.

Result messages such as `no_exp`, `exp_collected`, `inventory_full`, `action_failed`, and sell result messages do not define sounds. Their sounds are owned by the action button so layouts can customize them independently.

Each click type owns its action and sounds, so left and right clicks can be configured independently:

```yaml
left_click:
  action: "sell_and_exp"
  sound: ui.button.click
  sound_success: block.note_block.bell
  sound_fail: block.note_block.pling
right_click:
  action: "open_stacker"
  sound: ui.button.click
```

Each sound key accepts one sound or a list. List entries may be plain Bukkit keys or objects with `name`, `volume`, and `pitch`:

```yaml
sound_success:
  - block.note_block.bell
  - name: entity.experience_orb.pickup
    volume: 0.8
    pitch: 1.2
```

The same click blocks work inside an `if:` branch. Use Bukkit sound keys such as `ui.button.click`, `block.note_block.bell`, or `entity.item.pickup`. Omit a sound key, or set it to `none`, to disable that sound.

The older flat action format (`click: "action"`) and button-level nested `sound` format remain readable for backward compatibility. New layouts should use click blocks.

## Custom Player Head Textures

If a button uses `PLAYER_HEAD` as its material, you can provide a `custom_texture` to display a specific skin. This is especially useful for the spawner info button or any custom decorative buttons, working exactly like the `head_texture` configuration in `spawners_settings.yml`.

### Example Configuration

```yaml
slot_14:
  material: PLAYER_HEAD
  custom_texture: "df5de940bfe499c59ee8dac9f9c3919e7535eff3a9acb16f4842bf290f4c679f"
  enabled: true
  info_button: true
  if:
    sell_integration:
      left_click:
        action: "sell_and_exp"
      right_click:
        action: "open_stacker"
    no_sell_integration:
      click:
        action: "open_stacker"
```

### Key Details

- **`custom_texture`**: The texture hash string (without the `http://textures.minecraft.net/texture/` prefix). You can find these hashes on sites like [Minecraft Heads](https://minecraft-heads.com/).
- **Scope**: This setting works for **any** button in `main_gui.yml` or `storage_gui.yml` that uses `PLAYER_HEAD`.
- **Fallback Behavior**: If `custom_texture` is omitted for the spawner info button, the plugin will automatically fall back to the default mob head texture defined for that entity in `spawners_settings.yml`.
- **Performance**: Custom texture heads are heavily cached by the plugin after the first load, ensuring zero performance impact when players repeatedly open the GUI.

## Conditional Overrides

You can also dynamically change the button's `material` and `actions` based on server conditions using the `if:` block. 

:::note[Custom Texture Limitation]
The `custom_texture` property **only works when `material` is set to `PLAYER_HEAD`**. If you override the material to something else (e.g., `EMERALD`) inside an `if:` block, the `custom_texture` will be ignored.
:::

```yaml
slot_14:
  material: PLAYER_HEAD
  custom_texture: "default_texture_hash_here"
  enabled: true
  info_button: true
  if:
    sell_integration:
      material: EMERALD # custom_texture is ignored here because material is no longer PLAYER_HEAD
      left_click:
        action: "sell_and_exp"
    no_sell_integration:
      click:
        action: "open_stacker"
```

<br>
<br>

---

*Last update: June 14, 2026*

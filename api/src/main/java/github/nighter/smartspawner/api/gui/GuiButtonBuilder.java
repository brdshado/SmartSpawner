package github.nighter.smartspawner.api.gui;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for creating {@link GuiButtonData} instances programmatically.
 * Used in the SmartSpawner Developer API to define custom GUI buttons.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * GuiButtonData button = new GuiButtonBuilder()
 *     .slot(11)
 *     .material(Material.CHEST)
 *     .action("click", "open_storage")
 *     .build();
 * }</pre>
 */
public class GuiButtonBuilder {

    private String buttonType;
    private int slot = 0;
    private Material material = Material.STONE;
    private boolean enabled = true;
    private String condition = null;
    private final Map<String, String> actions = new HashMap<>();
    private boolean infoButton = false;
    private String customTexture = null;

    /**
     * Sets the button type identifier.
     *
     * @param buttonType the button type (e.g., "slot_11")
     * @return this builder
     */
    public GuiButtonBuilder buttonType(String buttonType) {
        this.buttonType = buttonType;
        return this;
    }

    /**
     * Sets the slot position for this button.
     *
     * @param slot the 1-based slot number used by SmartSpawner YAML layouts
     * @return this builder
     */
    public GuiButtonBuilder slot(int slot) {
        this.slot = slot;
        return this;
    }

    /**
     * Sets the material for this button.
     *
     * @param material the Bukkit material
     * @return this builder
     */
    public GuiButtonBuilder material(Material material) {
        this.material = material;
        return this;
    }

    /**
     * Sets whether this button is enabled.
     *
     * @param enabled true if enabled
     * @return this builder
     */
    public GuiButtonBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets a condition for this button to be shown.
     *
     * @param condition the condition string (e.g., "sell_integration")
     * @return this builder
     */
    public GuiButtonBuilder condition(String condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Adds an action for a specific click type.
     *
     * @param clickType the click type (e.g., "click", "left_click", "right_click")
     * @param action the action to perform
     * @return this builder
     */
    public GuiButtonBuilder action(String clickType, String action) {
        this.actions.put(clickType, action);
        return this;
    }

    /**
     * Marks this button as the spawner info button.
     *
     * @return this builder
     */
    public GuiButtonBuilder infoButton() {
        this.infoButton = true;
        return this;
    }

    /**
     * Sets whether this button is the spawner info button.
     *
     * @param infoButton true if info button
     * @return this builder
     */
    public GuiButtonBuilder infoButton(boolean infoButton) {
        this.infoButton = infoButton;
        return this;
    }

    /**
     * Sets a custom texture hash for PLAYER_HEAD material.
     *
     * @param customTexture the texture hash (without URL prefix)
     * @return this builder
     */
    public GuiButtonBuilder customTexture(String customTexture) {
        this.customTexture = customTexture;
        return this;
    }

    /**
     * Builds the {@link GuiButtonData} instance.
     *
     * @return the immutable button data
     */
    public GuiButtonData build() {
        return new GuiButtonData(buttonType, slot, material, enabled, condition,
                new HashMap<>(actions), infoButton, customTexture);
    }
}

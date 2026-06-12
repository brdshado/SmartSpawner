package github.nighter.smartspawner.api.gui;

import lombok.Getter;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable data transfer object representing a GUI button configuration.
 * This class is part of the SmartSpawner Developer API and can be used
 * to programmatically define GUI layouts via {@link GuiButtonBuilder}.
 */
@Getter
public class GuiButtonData {

    private final String buttonType;
    private final int slot;
    private final Material material;
    private final boolean enabled;
    private final String condition;
    private final Map<String, String> actions;
    private final boolean infoButton;
    private final String customTexture;

    /**
     * Creates button data using the same 1-based slot numbering as GUI layout YAML files.
     */
    public GuiButtonData(String buttonType, int slot, Material material, boolean enabled,
                         String condition, Map<String, String> actions, boolean infoButton,
                         String customTexture) {
        this.buttonType = buttonType;
        this.slot = slot;
        this.material = material;
        this.enabled = enabled;
        this.condition = condition;
        this.actions = actions != null ? Collections.unmodifiableMap(new HashMap<>(actions)) : Collections.emptyMap();
        this.infoButton = infoButton;
        this.customTexture = customTexture;
    }

    /**
     * Gets the action for a specific click type.
     *
     * @param clickType the click type (e.g., "click", "left_click", "right_click")
     * @return the action string, or null if not found
     */
    public String getAction(String clickType) {
        return actions.get(clickType);
    }

    /**
     * Gets the default action (checks "click" first, then "default").
     *
     * @return the default action string, or null if not found
     */
    public String getDefaultAction() {
        String clickAction = actions.get("click");
        if (clickAction != null && !clickAction.isEmpty()) {
            return clickAction;
        }
        return actions.get("default");
    }

    public boolean hasCondition() {
        return condition != null && !condition.isEmpty();
    }
}

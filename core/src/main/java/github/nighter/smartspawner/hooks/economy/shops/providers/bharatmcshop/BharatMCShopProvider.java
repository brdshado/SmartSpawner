package github.nighter.smartspawner.hooks.economy.shops.providers.bharatmcshop;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import net.bharatmc.sell.SellPlugin;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public class BharatMCShopProvider implements ShopProvider {

    private final SmartSpawner plugin;
    private SellPlugin bharatShop;

    public BharatMCShopProvider(SmartSpawner plugin) {
        this.plugin = plugin;
        Plugin target = plugin.getServer().getPluginManager().getPlugin("BharatMC-Shop");
        if (target instanceof SellPlugin) {
            this.bharatShop = (SellPlugin) target;
        }
    }

    @Override
    public String getPluginName() {
        return "BharatMC-Shop";
    }

    @Override
    public boolean isAvailable() {
        return bharatShop != null && bharatShop.isEnabled();
    }

    @Override
    public double getSellPrice(Material material) {
        if (!isAvailable()) return 0.0;
        Double price = bharatShop.getPrice(material);
        return price != null ? price : 0.0;
    }
}

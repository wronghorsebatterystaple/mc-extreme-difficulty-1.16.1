package AnonymousRand.ExtremeDifficultyPlugin;

import AnonymousRand.ExtremeDifficultyPlugin.listeners.*;
import AnonymousRand.ExtremeDifficultyPlugin.util.BlockOverride;
import net.minecraft.server.v1_16_R1.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtremeDifficultyPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        BlockOverride endStone = new BlockOverride(Blocks.END_STONE); /**end stone now has a blast resistance of 16*/
        endStone.set("durability", 16.0f);

        BlockOverride spawner = new BlockOverride(Blocks.SPAWNER); /**spawners are now indestructible by explosions and twice as hard to break as obsidian*/
        spawner.set("durability", 3600000.0f);
    }

    @Override
    public void onEnable() { //this runs when the plugin is first enabled (when the server starts up)
        getServer().getPluginManager().registerEvents(new BlockPlaceAndBreakListeners(this), this);  //registers the listeners
        getServer().getPluginManager().registerEvents(new DropItemListeners(),this);
        getServer().getPluginManager().registerEvents(new MobDamageListeners(), this);
        getServer().getPluginManager().registerEvents(new MobSpawnAndReplaceWithCustomListeners(this), this);
        getServer().getPluginManager().registerEvents(new MobDeathListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerDeathAndRespawnListeners(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerEatListeners(this),this);
        getServer().getPluginManager().registerEvents(new PlayerMovementAndFallDamageListeners(), this);
        getServer().getPluginManager().registerEvents(new ProjectileListeners(), this);
        getServer().getPluginManager().registerEvents(new RaidAndVillageListeners(),this);
        getServer().getPluginManager().registerEvents(new SheepDyeListeners(), this);
        getServer().getPluginManager().registerEvents(new SleepListeners(), this);
        getServer().getPluginManager().registerEvents(new VehicleCreateListeners(), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListeners(),this);

        addEyeOfEnderRecipe(); /**changes eye of ender recipe*/
    }

    @Override
    public void onDisable() {

    }

    public void addEyeOfEnderRecipe() {
        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft("ender_eye"));
        NamespacedKey key = new NamespacedKey(this, "eye_of_ender");
        ShapelessRecipe newRecipe = new ShapelessRecipe(key, new ItemStack(Material.ENDER_EYE));
        newRecipe.addIngredient(Material.BLAZE_ROD);
        newRecipe.addIngredient(Material.ENDER_PEARL);
        newRecipe.addIngredient(Material.NETHERITE_HOE);
        newRecipe.addIngredient(Material.BEETROOT_SOUP);
        newRecipe.addIngredient(Material.SCUTE);
        newRecipe.addIngredient(Material.WRITABLE_BOOK);
        newRecipe.addIngredient(Material.DRIED_KELP_BLOCK);
        newRecipe.addIngredient(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        newRecipe.addIngredient(Material.DEAD_BRAIN_CORAL);
        Bukkit.getServer().addRecipe(newRecipe);
    }
}

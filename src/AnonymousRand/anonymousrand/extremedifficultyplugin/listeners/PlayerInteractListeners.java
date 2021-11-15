package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityPiglin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class PlayerInteractListeners implements Listener {

    public static JavaPlugin plugin;
    private static final Random random = new Random();

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            Action action = event.getAction();
            Block bukkitBlock = event.getClickedBlock();
            Material type = bukkitBlock.getType();
            Player bukkitPlayer = event.getPlayer();
            EntityPlayer nmsPlayer = ((CraftPlayer)bukkitPlayer).getHandle();
            Location loc = bukkitPlayer.getLocation();
            World nmsWorld = ((CraftWorld)bukkitPlayer.getWorld()).getHandle();
            boolean containerBlock = type == Material.CHEST || type == Material.BARREL || type == Material.DISPENSER || type == Material.DROPPER || type == Material.ENDER_CHEST || type == Material.HOPPER || type == Material.CHEST_MINECART || type == Material.HOPPER_MINECART || type == Material.SHULKER_BOX || type == Material.TRAPPED_CHEST;

            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    if (type == Material.SPAWNER) { /**attempting to mine a spawner gives mining fatigue 2 for 7.5 seconds*/
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 150, 2));
                    }
                } else {
                    if (containerBlock) { /**right-clicking these blocks spawns a piglin and causes all piglins within 40 blocks horizontally to go into a frenzy for 10 seconds*/
                        CustomEntityPiglin newPiglin = new CustomEntityPiglin(nmsWorld);
                        new SpawnLivingEntity(nmsWorld, newPiglin, 1, null, loc, true);
                        newPiglin.setSlot(EnumItemSlot.MAINHAND, random.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); //give piglin a sword or crossbow
                        PiglinAI.a(newPiglin); //code from onInitialSpawn

                        List<Entity> nmsEntities = nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().grow(40.0, 128.0, 40.0), entity -> entity instanceof CustomEntityPiglin);

                        for (Entity entity : nmsEntities) {
                            ((CustomEntityPiglin)entity).veryAngryTicks += 200;
                        }
                    }

                    if (type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL || type == Material.SMITHING_TABLE) { /**right-clicking an anvil or smithing table causes it to explode 10 seconds later*/
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                        {
                            @Override
                            public void run() {
                                nmsWorld.createExplosion(null, bukkitBlock.getLocation().getX(), bukkitBlock.getLocation().getY(), bukkitBlock.getLocation().getZ(), 4.0F, true, Explosion.Effect.NONE);

                                try {
                                    bukkitBlock.setType(Material.AIR);
                                } catch (Exception e) {}
                            }
                        }, 200L);
                    }
                }
            }
        }
    }
}

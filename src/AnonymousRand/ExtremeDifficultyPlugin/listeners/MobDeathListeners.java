package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityChicken;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityChickenAggressive;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class MobDeathListeners implements Listener {

    Random rand = new Random();

    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        if (event.getEntityType() == CHICKEN) { /**chickens drop 20 eggs*/
            event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(Material.EGG, 20));

            if (((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityChickenAggressive) {
                if (rand.nextDouble() < 0.2) { /**aggressive chickens have a 20% chance to explode into 12-18 chickens*/
                    CustomEntityChicken newChicken;
                    Chicken chicken = (Chicken)event.getEntity();
                    Location loc = event.getEntity().getLocation();

                    for (int i = rand.nextInt(7); i <= 18; i++) {
                        newChicken = new CustomEntityChicken(((CraftWorld)chicken.getWorld()).getHandle());
                        newChicken.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((CraftWorld)chicken.getWorld()).getHandle().addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                    }
                }
            }
        }

        if (event.getEntityType() == COW) { /**cows explode with power 2 upon death*/
            Location loc = event.getEntity().getLocation();
            event.getEntity().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.0f, false);
        }

        if (event.getEntityType() == MUSHROOM_COW) { /**mooshrooms explode with power 15 upon death*/
            Location loc = event.getEntity().getLocation();
            event.getEntity().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 15.0f, false);
        }

        if (event.getEntityType() == SPIDER) {
            Location loc;
            EntitySpider spider = (EntitySpider)(((CraftLivingEntity)event.getEntity()).getHandle());

            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        loc = new Location(spider.getWorld().getWorld(), Math.floor(spider.locX()) + x, Math.floor(spider.locY()) + y, Math.floor(spider.locZ()) + z);
                        if (loc.getBlock().getType() == Material.AIR) { /**spiders lay down cobwebs that last 10 seconds when it dies in a 5 by 5 cube around itself*/
                            loc.getBlock().setType(Material.COBWEB);
                            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(loc.getBlock(), loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 10 seconds
                        }
                    }
                }
            }
        }

        //todo: change to custom mobs
        if (event.getEntityType() == WANDERING_TRADER) { /**wandering traders spawn 10 evokers and illusioners upon death*/
            WanderingTrader trader = (WanderingTrader)event.getEntity();
            Location loc = trader.getLocation();
            EntityIllagerIllusioner illusioner;
            EntityEvoker evoker;

            for (int i = 0; i < 10; i++) {
                illusioner = new EntityIllagerIllusioner(EntityTypes.ILLUSIONER, ((CraftWorld)trader.getWorld()).getHandle());
                evoker = new EntityEvoker(EntityTypes.EVOKER, ((CraftWorld)trader.getWorld()).getHandle());

                illusioner.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                ((CraftWorld)trader.getWorld()).getHandle().addEntity(illusioner, CreatureSpawnEvent.SpawnReason.NATURAL);

                evoker.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                ((CraftWorld)trader.getWorld()).getHandle().addEntity(evoker, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }
    }
}

package semicolon.murinn.module.placeable;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.item.Ingredients;
import semicolon.murinn.module.menu.impl.placeable.coffee.CoffeeMachine;
import semicolon.murinn.module.menu.impl.placeable.furnace.FurnaceMachine;
import semicolon.murinn.module.menu.impl.placeable.grinder.GrinderMachine;
import semicolon.murinn.module.menu.impl.placeable.print.PrintMachine;
import semicolon.murinn.module.util.PDCUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum PlaceableModule {
    COMMUNICATION("통신 모듈", Material.IRON_HORSE_ARMOR, 1,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0), new Vector(0, 1, 0)),
    GRINDER("분쇄기 모듈", Material.IRON_HORSE_ARMOR, 2,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0)),
    PRINT("프린트 모듈", Material.IRON_HORSE_ARMOR, 3,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0)),
    COFFEE_MACHINE("커피머신 모듈", Material.IRON_HORSE_ARMOR, 4,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0)),
    BATTERY("배터리 모듈", Material.IRON_HORSE_ARMOR, 5,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0)),
    FURNACE_OPEN("용광로 모듈(열림)", Material.IRON_HORSE_ARMOR, 6,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0), new Vector(0, 1, 0)),
    FURNACE_CLOSE("용광로 모듈", Material.IRON_HORSE_ARMOR, 7,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0), new Vector(0, 1, 0)),
    SOLAR("태양광 모듈", Material.IRON_HORSE_ARMOR, 8,
            new Vector(0, 0, 0), new Vector(1, 1, 1), new Vector(0, 0, 0), new Vector(0, 1, 0));

    private final String itemName;
    private final Material material;
    private final int cmd;
    private final Vector offset;
    private final Vector scale;
    private final List<Vector> barriers;

    PlaceableModule(String itemName, Material material, int customModelData, Vector offset, Vector scale, Vector... barriers) {
        this.itemName = itemName;
        this.material = material;
        this.cmd = customModelData;
        this.offset = offset;
        this.scale = scale;
        this.barriers = List.of(barriers);
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = new ItemStack(material, amount);
        item.editMeta(meta -> {
            meta.setCustomModelData(cmd);
            meta.itemName(Component.text(this.itemName).decoration(TextDecoration.ITALIC, false));
            AttributeModifier var1 = new AttributeModifier(new NamespacedKey(Main.plugin(), "hide_attribute"), 0.0, AttributeModifier.Operation.ADD_NUMBER);
            meta.addAttributeModifier(Attribute.ARMOR, var1);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        item.editPersistentDataContainer(pdc -> pdc.set(PDCUtil.SEMICOLON_ITEM, PersistentDataType.STRING, this.name()));
        return item;
    }

    public static boolean isPlaceableModule(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_HORSE_ARMOR || !item.hasItemMeta()) return false;
        return item.getPersistentDataContainer().has(PDCUtil.SEMICOLON_ITEM, PersistentDataType.STRING);
    }

    public boolean place(Location loc, Player player) {
        loc.set(loc.getBlockX() + 0.5, loc.getBlockY() + 1.5, loc.getBlockZ() + 0.5);

        float yaw = player.getLocation().getYaw() % 360;
        if (yaw < 0) yaw += 360;
        float reverse = ((Math.round(yaw / 90) * 90) + 180) % 360;

        for (Vector offset : barriers) {
            if (loc.clone().add(offset).getBlock().getType() != Material.AIR) {
                player.sendActionBar(Component.text("이곳에는 설치할 수 없습니다!", NamedTextColor.RED));
                return false;
            }
        }

        List<Location> barrierLocations = new ArrayList<>();
        for (Vector offset : barriers) {
            loc.clone().add(offset).getBlock().setType(Material.BARRIER);
            barrierLocations.add(loc.clone().add(offset));
        }

        ItemDisplay display = loc.getWorld().spawn(loc.clone().add(offset), ItemDisplay.class, i -> {
            ItemStack item = new ItemStack(material);
            item.editMeta(meta -> meta.setCustomModelData(cmd));

            i.setItemStack(item);
            i.setBillboard(Display.Billboard.FIXED);
            i.setRotation(reverse, 0f);
            i.setTransformation(new Transformation(
                    new Vector3f(),
                    new AxisAngle4f(),
                    new Vector3f((float) scale.getX(), (float) scale.getY(), (float) scale.getZ()),
                    new AxisAngle4f()
            ));
        });

        loc.getWorld().playSound(player, Sound.BLOCK_HEAVY_CORE_PLACE, 1.0f, 1.7f);
        player.swingMainHand();
        PlaceableModuleManager.registerModule(this, loc, barrierLocations, display.getUniqueId());

        return true;
    }

    public void interact(Player player, Block clickedBlock) {
        Location moduleLocation = PlaceableModuleManager.getModuleLocation(clickedBlock);
        if (moduleLocation == null) return;

        switch (this) {
            case GRINDER -> GrinderMachine.getInstance().openForPlayer(player, moduleLocation);
            case PRINT -> PrintMachine.getInstance().openForPlayer(player, moduleLocation);
            case COFFEE_MACHINE -> CoffeeMachine.getInstance().openForPlayer(player, moduleLocation);
            case FURNACE_CLOSE -> FurnaceMachine.getInstance().openForPlayer(player, moduleLocation);
        }
    }

    public static void removeModule(Location location) {
        PlaceableModule module = PlaceableModuleManager.getModuleAtLocation(location);
        if (module == null) return;

        List<Location> barrierLocations = PlaceableModuleManager.getBarrierLocations(location);
        if (barrierLocations == null) return;

        UUID displayUUID = PlaceableModuleManager.getDisplayUUID(location);
        dropUpgradeItems(location, module);

        for (Location barrierLoc : barrierLocations) {
            Block barrierBlock = barrierLoc.getBlock();
            if (barrierBlock.getType() == Material.BARRIER) {
                PlaceableModuleManager.clearBarrierData(barrierBlock);
                barrierBlock.setType(Material.AIR);
            }
        }

        if (displayUUID != null) {
            for (Entity entity : location.getChunk().getEntities()) {
                if (entity instanceof ItemDisplay && entity.getUniqueId().equals(displayUUID)) {
                    entity.remove();
                    break;
                }
            }
        }

        location.getWorld().dropItem(location, module.getItemStack(1));
        PlaceableModuleManager.removeModuleData(location);
    }

    private static void dropUpgradeItems(Location location, PlaceableModule module) {
        PersistentDataContainer modulePdc = PDCUtil.getModulePdc(location);
        if (modulePdc == null) return;

        switch (module) {
            case GRINDER -> {
                int grinderLevel = modulePdc.getOrDefault(PDCUtil.GRINDER_UPGRADE_LEVEL, PersistentDataType.INTEGER, 0);
                if (grinderLevel >= 1) {
                    location.getWorld().dropItem(location, Ingredients.GRINDER_EXTENSION.getItemStack(1));
                }
                if (grinderLevel >= 2) {
                    location.getWorld().dropItem(location, Ingredients.GEAR.getItemStack(1));
                }
                if (grinderLevel >= 3) {
                    location.getWorld().dropItem(location, Ingredients.DRILL.getItemStack(1));
                }
            }
            case FURNACE_CLOSE -> {
                int furnaceLevel = modulePdc.getOrDefault(PDCUtil.FURNACE_UPGRADE_LEVEL, PersistentDataType.INTEGER, 0);
                if (furnaceLevel >= 1) {
                    location.getWorld().dropItem(location, Ingredients.FURNACE_EXTENSION.getItemStack(1));
                }
                if (furnaceLevel >= 2) {
                    location.getWorld().dropItem(location, Ingredients.ICE_MOLD.getItemStack(1));
                }
                if (furnaceLevel >= 3) {
                    location.getWorld().dropItem(location, Ingredients.TORCH.getItemStack(1));
                }
            }
        }
    }
}
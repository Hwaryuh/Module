package semicolon.murinn.module.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;

@SuppressWarnings("UnstableApiUsage")
public class ItemUtil {
    public static ItemStack overlayItem() {
        ItemStack var1 = new ItemStack(Material.ENDER_EYE);

        var1.editMeta(meta -> {
            EquippableComponent var2 = meta.getEquippable();
            var2.setSlot(EquipmentSlot.HEAD);
            var2.setCameraOverlay(new NamespacedKey("minecraft", "misc/build_module_overlay"));
            meta.setEquippable(var2);
        });
        return var1;
    }

    public static void applyConsumable(ItemStack item, ItemUseAnimation animation, boolean particle) {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item.clone());

        Consumable.Builder builder = Consumable.builder()
                .animation(animation)
                .hasConsumeParticles(particle);

        switch (animation) {
            case EAT -> builder.sound(SoundEvents.GENERIC_EAT);
            case DRINK -> builder.sound(SoundEvents.GENERIC_DRINK);
        }

        Consumable c = builder.build();
        nmsStack.set(DataComponents.CONSUMABLE, c);

        item.setItemMeta(CraftItemStack.asBukkitCopy(nmsStack).getItemMeta());
    }

    public static void setMaxStack(ItemStack item, int maxStack) {
        item.editMeta(meta -> meta.setMaxStackSize(maxStack));
    }

    public static void setMaxDurability(ItemStack item, int durability) {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item.clone());
        nmsStack.set(DataComponents.MAX_DAMAGE, durability);
        Integer test = nmsStack.get(DataComponents.DAMAGE);

        item.setItemMeta(CraftItemStack.asBukkitCopy(nmsStack).getItemMeta());
    }
}
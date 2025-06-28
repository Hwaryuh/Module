package semicolon.murinn.module.menu.impl.placeable.print;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.item.Ingredients;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum PrintResult {
    PICKAXE_1("단단한 곡괭이", Material.LEATHER_HORSE_ARMOR, 1,
            Map.of(Ingredients.FE_INGOT, 10),
            Map.of()),
    PICKAXE_2("가벼운 채굴기(드릴)", Material.LEATHER_HORSE_ARMOR, 2,
            Map.of(Ingredients.AL_INGOT, 20, Ingredients.FE_INGOT, 10, Ingredients.CU_INGOT, 10, Ingredients.LI_INGOT, 5),
            Map.of(PrintResult.PICKAXE_1, 1)),
    PICKAXE_3("무거운 채굴기(드릴)", Material.LEATHER_HORSE_ARMOR, 3,
            Map.of(Ingredients.CU_INGOT, 10, Ingredients.LI_INGOT, 10, Ingredients.PT_INGOT, 10, Ingredients.NI_INGOT, 5, Ingredients.TI_INGOT, 5),
            Map.of(PrintResult.PICKAXE_2, 1)),
    HOE_1("단단한 괭이", Material.LEATHER_HORSE_ARMOR, 4,
            Map.of(Ingredients.FE_INGOT, 10),
            Map.of()),
    HOE_2("가볍고 단단한 괭이", Material.LEATHER_HORSE_ARMOR, 5,
            Map.of(Ingredients.AL_INGOT, 10, Ingredients.FE_INGOT, 10, Ingredients.CU_INGOT, 10),
            Map.of(PrintResult.HOE_1, 1)),
    HOE_3("자동화 괭이", Material.LEATHER_HORSE_ARMOR, 6,
            Map.of(Ingredients.CU_INGOT, 10, Ingredients.LI_INGOT, 10, Ingredients.FE_INGOT, 10, Ingredients.NI_INGOT, 5, Ingredients.TI_INGOT, 5),
            Map.of(PrintResult.HOE_2, 1)),
    WATERINGCAN_1("소형 물뿌리개", Material.LEATHER_HORSE_ARMOR, 7,
            Map.of(Ingredients.AL_INGOT, 10),
            Map.of()),
    WATERINGCAN_2("펌프형 물뿌리개", Material.LEATHER_HORSE_ARMOR, 8,
            Map.of(Ingredients.AL_INGOT, 15, Ingredients.FE_INGOT, 10, Ingredients.CU_INGOT, 5),
            Map.of()),
    CAPSULEGUN("캡슐건", Material.LEATHER_HORSE_ARMOR, 9,
            Map.of(Ingredients.CU_INGOT, 20, Ingredients.LI_INGOT, 20, Ingredients.PT_INGOT, 15),
            Map.of()),
    WEAPON_PIPE("파이프", Material.LEATHER_HORSE_ARMOR, 10,
            Map.of(Ingredients.FE_INGOT, 10),
            Map.of()),
    WEAPON_KNIFE("나이프", Material.LEATHER_HORSE_ARMOR, 11,
            Map.of(Ingredients.CU_INGOT, 10, Ingredients.LI_INGOT, 10, Ingredients.PT_INGOT, 10),
            Map.of()),
    WEAPON_LONG_SWORD("장도", Material.LEATHER_HORSE_ARMOR, 12,
            Map.of(Ingredients.AL_INGOT, 20, Ingredients.FE_INGOT, 10, Ingredients.CU_INGOT, 10),
            Map.of());

    private final String itemName;
    private final Material material;
    private final int cmd;
    private final Map<Ingredients, Integer> requiredMinerals;
    private final Map<PrintResult, Integer> requiredItems;

    PrintResult(String itemName, Material material, int customModelData, Map<Ingredients, Integer> requiredMinerals, Map<PrintResult, Integer> requiredItems) {
        this.itemName = itemName;
        this.material = material;
        this.cmd = customModelData;
        this.requiredMinerals = requiredMinerals;
        this.requiredItems = requiredItems;
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
        return item;
    }

    public boolean hasRequiredMaterials(Inventory inv) {
        for (Map.Entry<Ingredients, Integer> entry : requiredMinerals.entrySet()) {
            if (!hasEnoughMinerals(inv, entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        for (Map.Entry<PrintResult, Integer> entry : requiredItems.entrySet()) {
            if (!hasEnoughItems(inv, entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean hasEnoughMinerals(Inventory inv, Ingredients mineral, int required) {
        int available = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && isMineralItem(item, mineral)) {
                available += item.getAmount();
            }
        }
        return available >= required;
    }

    private boolean hasEnoughItems(Inventory inv, PrintResult printResult, int required) {
        int available = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && isPrintResultItem(item, printResult)) {
                available += item.getAmount();
            }
        }
        return available >= required;
    }

    private boolean isMineralItem(ItemStack item, Ingredients mineral) {
        if (item.getType() != mineral.getMaterial()) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == mineral.getCustomModelData();
    }

    private boolean isPrintResultItem(ItemStack item, PrintResult printResult) {
        if (item.getType() != printResult.material) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == printResult.cmd;
    }

    public void consumeMaterials(Inventory inv) {
        for (Map.Entry<Ingredients, Integer> entry : requiredMinerals.entrySet()) {
            consumeMineral(inv, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<PrintResult, Integer> entry : requiredItems.entrySet()) {
            consumeItem(inv, entry.getKey(), entry.getValue());
        }
    }

    private void consumeMineral(Inventory inv, Ingredients mineral, int needed) {
        int remaining = needed;
        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isMineralItem(item, mineral)) {
                int available = item.getAmount();
                if (available >= remaining) {
                    item.setAmount(available - remaining);
                    remaining = 0;
                } else {
                    inv.setItem(i, null);
                    remaining -= available;
                }
            }
        }
    }

    private void consumeItem(Inventory inv, PrintResult printResult, int needed) {
        int remaining = needed;
        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isPrintResultItem(item, printResult)) {
                int available = item.getAmount();
                if (available >= remaining) {
                    item.setAmount(available - remaining);
                    remaining = 0;
                } else {
                    inv.setItem(i, null);
                    remaining -= available;
                }
            }
        }
    }

    public int getCustomModelData() {
        return this.cmd;
    }

    public List<Component> getRecipeLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("필요 재료:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));

        for (Map.Entry<Ingredients, Integer> entry : requiredMinerals.entrySet()) {
            Component recipeLine = Component.text("- ", NamedTextColor.GRAY)
                    .append(entry.getKey().getItemName())
                    .append(Component.text(" x" + entry.getValue(), NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false);
            lore.add(recipeLine);
        }

        for (Map.Entry<PrintResult, Integer> entry : requiredItems.entrySet()) {
            Component recipeLine = Component.text("- " + entry.getKey().itemName + " x" + entry.getValue(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false);
            lore.add(recipeLine);
        }

        return lore;
    }
}
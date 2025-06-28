package semicolon.murinn.module.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public enum Ingredients {
    COFFEE_AG(Component.text("커피콩"), Material.BLACK_DYE, 4),
    COFFEE_AU(Component.text("커피콩"), Material.BLACK_DYE, 14),
    COFFEE_TI(Component.text("커피콩"), Material.BLACK_DYE, 24),
    MUG(Component.text("머그잔"), Material.BOWL, 1),
    COFFEE(Component.text("커피", NamedTextColor.GOLD), Material.BOWL, 2),

    GRINDER_EXTENSION(Component.text("분쇄기 용량 확장"), Material.SADDLE, 20),
    GEAR(Component.text("Al-Cu 합금 기어"), Material.SADDLE, 21),
    DRILL(Component.text("Ti-Pt-Au 합금 드릴"), Material.SADDLE, 22),

    MG(Component.text("마그네슘 광석"), Material.RED_DYE, 1),
    AL(Component.text("알루미늄 광석"), Material.RED_DYE, 2),
    FE(Component.text("철 광석"), Material.RED_DYE, 3),
    CU(Component.text("구리 광석"), Material.RED_DYE, 4),
    LI(Component.text("리튬 광석"), Material.RED_DYE, 5),
    AU(Component.text("금 광석"), Material.RED_DYE, 6),
    PT(Component.text("백금 광석"), Material.RED_DYE, 7),
    NI(Component.text("니켈 광석"), Material.RED_DYE, 8),
    TI(Component.text("티타늄 광석"), Material.RED_DYE, 9),

    MG_POWDER(Component.text("마그네슘 파우더"), Material.RED_DYE, 10),
    AL_POWDER(Component.text("알루미늄 파우더"), Material.RED_DYE, 11),
    FE_POWDER(Component.text("철 파우더"), Material.RED_DYE, 12),
    CU_POWDER(Component.text("구리 파우더"), Material.RED_DYE, 13),
    LI_POWDER(Component.text("리튬 파우더"), Material.RED_DYE, 14),
    AU_POWDER(Component.text("금 파우더"), Material.RED_DYE, 15),
    PT_POWDER(Component.text("백금 파우더"), Material.RED_DYE, 16),
    NI_POWDER(Component.text("니켈 파우더"), Material.RED_DYE, 17),
    TI_POWDER(Component.text("티타늄 파우더"), Material.RED_DYE, 18),

    MG_INGOT(Component.text("마그네슘 주괴"), Material.RED_DYE, 19),
    AL_INGOT(Component.text("알루미늄 주괴"), Material.RED_DYE, 20),
    FE_INGOT(Component.text("철 주괴"), Material.RED_DYE, 21),
    CU_INGOT(Component.text("구리 주괴"), Material.RED_DYE, 22),
    LI_INGOT(Component.text("리튬 주괴"), Material.RED_DYE, 23),
    AU_INGOT(Component.text("금 주괴"), Material.RED_DYE, 24),
    PT_INGOT(Component.text("백금 주괴"), Material.RED_DYE, 25),
    NI_INGOT(Component.text("니켈 주괴"), Material.RED_DYE, 26),
    TI_INGOT(Component.text("티타늄 주괴"), Material.RED_DYE, 27),

    AL_MG_INGOT(Component.text("합금 주괴"), Material.RED_DYE, 28),
    AL_CU_INGOT(Component.text("합금 주괴"), Material.RED_DYE, 29),
    AL_LI_INGOT(Component.text("합금 주괴"), Material.RED_DYE, 30),
    CU_AU_INGOT(Component.text("합금 주괴"), Material.RED_DYE, 31),
    NI_FE_INGOT(Component.text("합금 주괴"), Material.RED_DYE, 32),
    TI_PT_AU_INGOT(Component.text("합금 주괴"), Material.RED_DYE, 33),

    FURNACE_EXTENSION(Component.text("용광로 용해 슬롯 확장"), Material.SADDLE, 17),
    ICE_MOLD(Component.text("Cu-Au 합금 냉각 몰드"), Material.SADDLE, 18),
    TORCH(Component.text("Ni-Fe 합금 토치"), Material.SADDLE, 19),

    MG_RECIPE(Component.text("마그네슘 주괴 레시피"), Material.SADDLE, 23),
    AL_RECIPE(Component.text("알루미늄 주괴 레시피"), Material.SADDLE, 24),
    FE_RECIPE(Component.text("철 주괴 레시피"), Material.SADDLE, 25),
    CU_RECIPE(Component.text("구리 주괴 레시피"), Material.SADDLE, 26),
    LI_RECIPE(Component.text("리튬 주괴 레시피"), Material.SADDLE, 27),
    AU_RECIPE(Component.text("금 주괴 레시피"), Material.SADDLE, 28),
    PT_RECIPE(Component.text("백금 주괴 레시피"), Material.SADDLE, 29),
    NI_RECIPE(Component.text("니켈 주괴 레시피"), Material.SADDLE, 30),
    TI_RECIPE(Component.text("티타늄 주괴 레시피"), Material.SADDLE, 31),
    AL_MG_RECIPE(Component.text("Al-Mg 합금 주괴 레시피"), Material.SADDLE, 32),
    AL_CU_RECIPE(Component.text("Al-Cu 합금 레시피"), Material.SADDLE, 33),
    AL_LI_RECIPE(Component.text("Al-Li 합금 레시피"), Material.SADDLE, 34),
    CU_AU_RECIPE(Component.text("Cu-Au 합금 주괴 레시피"), Material.SADDLE, 35),
    NI_FE_RECIPE(Component.text("Ni-Fe 합금 주괴 레시피"), Material.SADDLE, 36),
    TI_PT_AU_RECIPE(Component.text("Ti-Pt-Au 합금 주괴 레시피"), Material.SADDLE, 37);


    private final Component itemName;
    private final Material material;
    private final int cmd;

    Ingredients(Component itemName, Material material, int customModelData) {
        this.itemName = itemName;
        this.material = material;
        this.cmd = customModelData;
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = new ItemStack(material, amount);
        item.editMeta(meta -> {
            meta.setCustomModelData(cmd);
            meta.itemName(this.itemName.decoration(TextDecoration.ITALIC, false));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        return item;
    }

    public Material getMaterial() {
        return this.material;
    }

    public int getCustomModelData() {
        return this.cmd;
    }

    public Component getItemName() {
        return this.itemName;
    }
}
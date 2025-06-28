package semicolon.murinn.module.util;

import semicolon.murinn.module.Main;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class TitleUtil {
    public static void sendTitle(Player player, Component title, Component subTitle, double fadeIn, double stay, double fadeOut) {
        Duration fi = Duration.ofMillis((long) ((fadeIn * 20) * 50L));
        Duration s = Duration.ofMillis((long) (stay * 20) * 50);
        Duration fo = Duration.ofMillis((long) (fadeOut * 20) * 50L);

        Component var1 = title != null ? title : Component.empty();
        Component var2 = subTitle != null ? subTitle : Component.empty();

        player.showTitle(Title.title(var1, var2, Title.Times.times(fi, s, fo)));
    }

    public static void callBackWithFade(Player player, double fadeIn, double stay, double fadeOut, Runnable callback, double progressPercent) {
        progressPercent = Math.max(0.0, Math.min(1.0, progressPercent));

        int fi = (int) (fadeIn * 20);
        int s = (int) (stay * 20);
        int fo = (int) (fadeOut * 20);
        long delay = (long) ((fi + s + fo) * progressPercent);

        Component var1 = Component.text("o", NamedTextColor.BLACK).font(Key.key("minecraft", "semicolon"));
        player.showTitle(Title.title(var1, Component.empty(), Title.Times.times(Duration.ofMillis(fi * 50L), Duration.ofMillis(s * 50L), Duration.ofMillis(fo * 50L))));

        if (callback != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    callback.run();
                }
            }.runTaskLater(Main.plugin(), delay);
        }
    }
}
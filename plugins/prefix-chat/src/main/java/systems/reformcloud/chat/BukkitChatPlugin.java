/*
 * This file is part of reformcloud, licensed under the MIT License (MIT).
 *
 * Copyright (c) ReformCloud <https://github.com/ReformCloud>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package systems.reformcloud.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BukkitChatPlugin extends JavaPlugin implements Listener {

  private String chatFormat;

  @Override
  public void onEnable() {
    if (Bukkit.getPluginManager().getPlugin("ReformCloudBukkitPermissions") == null) {
      System.err.println("[Chat] Unable to find permission plugin, do not load permission listeners");
      return;
    }

    super.getConfig().options().copyDefaults(true);
    super.saveConfig();
    this.chatFormat = super.getConfig().getString("format", "%display%%name% &7➤ &f%message%");

    Bukkit.getPluginManager().registerEvents(this, this);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void handle(final @NotNull AsyncPlayerChatEvent event) {
    String format = ChatFormatUtil.buildFormat(
      event.getPlayer().getUniqueId(),
      this.chatFormat,
      event.getMessage(),
      event.getPlayer().getName(),
      event.getPlayer().getDisplayName(),
      event.getPlayer()::hasPermission,
      (colorChar, message) -> ChatColor.translateAlternateColorCodes(colorChar, message)
    );
    if (format == null) {
      event.setCancelled(true);
    } else {
      event.setFormat(format);
    }
  }
}
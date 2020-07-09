/*
 * The MIT License
 * Copyright © 2015 Pixel Outlaw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.pixeloutlaw.battered.listeners;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import io.pixeloutlaw.battered.BatteredPlugin;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathListener implements Listener {

  private VersionedSmartYamlConfiguration config;
  private BatteredPlugin plugin;


  public DeathListener(BatteredPlugin plugin, VersionedSmartYamlConfiguration config) {
    this.plugin = plugin;
    this.config = config;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerDeathEvent(PlayerDeathEvent event) {
    event.setKeepInventory(true);
    event.getDrops().clear();
    if (event.getEntity().getKiller() != null) {
      return;
    }
    if (config.getStringList("ignored-worlds").contains(event.getEntity().getWorld().getName())) {
      return;
    }
    if (event.getEntity().getLevel() < 10) {
      MessageUtils.sendMessage(event.getEntity(), "");
      MessageUtils.sendMessage(event.getEntity(),
          "&eDue to being below &flevel 10&e, you have not lost any item durability or dropped items on death.");
      MessageUtils.sendMessage(event.getEntity(), "");
      return;
    }
    final Player player = event.getEntity();

    Inventory inventory = player.getInventory();
    ItemStack[] inventoryContents = inventory.getContents().clone();
    for (int i = 0; i < inventoryContents.length; i++) {
      ItemStack itemStack = inventoryContents[i];
      if (itemStack == null || itemStack.getType() == Material.AIR) {
        continue;
      }
      if (isSoulShard(itemStack)) {
        continue;
      }
      if (i <= 8 || (i >= 36 && i <= 40)) {
        if (Math.random() > 0.6) {
          continue;
        }
        if (itemStack.getAmount() == 1) {
          damageEquipmentItem(player, itemStack);
          continue;
        }
        int dropAmount = (int) Math.floor(itemStack.getAmount() * Math.random());
        int keepAmount = itemStack.getAmount() - dropAmount;

        ItemStack dropStack = itemStack.clone();
        itemStack.setAmount(keepAmount);
        dropStack.setAmount(dropAmount);

        Item item = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), dropStack);
        applyDropProtection(item, event.getEntity().getUniqueId(), 2400);
      } else {
        if (Math.random() > 0.2) {
          continue;
        }
        Item item = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack.clone());
        applyDropProtection(item, event.getEntity().getUniqueId(), 2400);
        itemStack.setAmount(0);
      }
    }
    player.updateInventory();
  }

  private static boolean isSoulShard(ItemStack itemStack) {
    if (itemStack.getType() != Material.QUARTZ) {
      return false;
    }
    if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
      return false;
    }
    return itemStack.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Soul Shard");
  }

  public static void damageEquipmentItem(Player player, ItemStack stack) {
    if (stack.getType().getMaxDurability() < 15 || stack.getType() == Material.SHEARS) {
      return;
    }
    double damage = (0.1 + Math.random() * 0.15) * stack.getType().getMaxDurability();
    short dura = (short) (damage + stack.getDurability());
    if (dura >= stack.getType().getMaxDurability()) {
      sendMessage(player, "&c[!] One of your items was dropped due to low durability!");
      player.getWorld().dropItemNaturally(player.getLocation(), stack.clone());
      stack.setAmount(0);
      return;
    }
    if (dura > stack.getType().getMaxDurability() * 0.75) {
      sendMessage(player,
          "&e[!] One of your items is in danger of dropping on death due to low durability!");
    }
    stack.setDurability((short) Math.min(dura, stack.getType().getMaxDurability()));
  }

  public void applyDropProtection(Item drop, UUID owner, long duration) {
    drop.setOwner(owner);
    Bukkit.getScheduler().runTaskLater(plugin, () -> clearDropProtection(drop), duration);
  }

  public void clearDropProtection(Item drop) {
    if (drop != null) {
      drop.setOwner(null);
    }
  }
}

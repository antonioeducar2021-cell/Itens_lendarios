package br.com.itenslendarios;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ItensLendarios extends JavaPlugin implements Listener {
    private NamespacedKey itemKey;
    private final Set<UUID> penaAtiva = new HashSet<>();

    @Override
    public void onEnable() {
        itemKey = new NamespacedKey(this, "item_lendario");
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("itenslendarios") != null) {
            getCommand("itenslendarios").setExecutor((sender, command, label, args) -> {
                if (!sender.hasPermission("itenslendarios.admin")) {
                    sender.sendMessage("§cVocê não tem permissão.");
                    return true;
                }
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage("§eUso: /itenslendarios <tridente|corno|pena> [jogador]");
                    return true;
                }
                Player alvo;
                if (args.length == 2) {
                    alvo = Bukkit.getPlayerExact(args[1]);
                    if (alvo == null) {
                        sender.sendMessage("§cJogador não encontrado.");
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    alvo = p;
                } else {
                    sender.sendMessage("§cInforme um jogador: /itenslendarios <item> <jogador>");
                    return true;
                }

                ItemStack item = switch (args[0].toLowerCase()) {
                    case "tridente", "z" -> criarItem(Material.TRIDENT, "§cTridente Z", "tridente_z");
                    case "corno", "x" -> criarItem(Material.GOAT_HORN, "§6Corno X", "corno_x");
                    case "pena", "w" -> criarItem(Material.FEATHER, "§bPena W", "pena_w");
                    default -> null;
                };
                if (item == null) {
                    sender.sendMessage("§cItem inválido. Use tridente, corno ou pena.");
                    return true;
                }
                alvo.getInventory().addItem(item).values().forEach(left ->
                        alvo.getWorld().dropItemNaturally(alvo.getLocation(), left));
                sender.sendMessage("§aItem entregue para " + alvo.getName() + ".");
                return true;
            });
        }

        // Atualiza a Pena W continuamente para detectar as duas mãos.
        Bukkit.getScheduler().runTaskTimer(this, this::atualizarPenas, 1L, 5L);
        getLogger().info("ITENS LENDARIOS - VERSAO DE TESTE 3 CARREGADA!");
    }

    @Override
    public void onDisable() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!viewer.equals(target)) viewer.showEntity(this, target);
            }
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        penaAtiva.clear();
    }

    private ItemStack criarItem(Material material, String nome, String id) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(nome);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);
        stack.setItemMeta(meta);
        return stack;
    }

    private String id(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    private boolean eh(ItemStack stack, String wanted) {
        return wanted.equals(id(stack));
    }

    private void atualizarPenas() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean ativa = eh(player.getInventory().getItemInMainHand(), "pena_w")
                    || eh(player.getInventory().getItemInOffHand(), "pena_w");
            if (ativa) {
                penaAtiva.add(player.getUniqueId());
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 0, false, false, false));
                // Oculta a entidade inteira para os demais jogadores, incluindo armadura e itens nas mãos.
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewer.equals(player)) viewer.hideEntity(this, player);
                }
            } else if (penaAtiva.remove(player.getUniqueId())) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewer.equals(player)) viewer.showEntity(this, player);
                }
            }
        }
    }

    @EventHandler
    public void aoArremessar(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        if (eh(player.getInventory().getItemInMainHand(), "tridente_z")) {
            trident.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, "tridente_z");
        }
    }

    @EventHandler
    public void danoDoTridente(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof org.bukkit.entity.Trident trident)) return;
        if (!"tridente_z".equals(trident.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING))) return;
        Entity victim = event.getEntity();
        if (victim instanceof Player target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0, false, false, true));
        }
    }

    @EventHandler
    public void aoUsarCorno(org.bukkit.event.player.PlayerInteractEvent event) {
        ItemStack hand = event.getItem();
        if (!eh(hand, "corno_x")) return;
        Player user = event.getPlayer();
        event.setCancelled(true);
        Vector direction = user.getLocation().getDirection().normalize();
        for (Entity entity : user.getNearbyEntities(15, 15, 15)) {
            if (!(entity instanceof Player target) || target.equals(user)) continue;
            Vector away = target.getLocation().toVector().subtract(user.getLocation().toVector());
            if (away.lengthSquared() <= 15 * 15) {
                Vector push = away.lengthSquared() == 0 ? direction : away.normalize();
                target.setVelocity(push.multiply(2.0).setY(0.65));
            }
        }
        user.getWorld().playSound(user.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.5f, 0.8f);
        user.getWorld().spawnParticle(Particle.GUST, user.getLocation().add(0, 1, 0), 30, 2, 1, 2, 0.15);
        user.setCooldown(Material.GOAT_HORN, 200);
    }

    @EventHandler
    public void aoTrocarMao(PlayerSwapHandItemsEvent event) {
        Bukkit.getScheduler().runTask(this, this::atualizarPenas);
    }

    @EventHandler
    public void aoMudarSlot(PlayerItemHeldEvent event) {
        Bukkit.getScheduler().runTask(this, this::atualizarPenas);
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, this::atualizarPenas, 2L);
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        penaAtiva.remove(event.getPlayer().getUniqueId());
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(event.getPlayer())) viewer.showEntity(this, event.getPlayer());
        }
    }
}

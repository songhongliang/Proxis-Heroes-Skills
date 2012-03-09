package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillDrain extends PassiveSkill {

    public SkillDrain(Heroes plugin) {
        super(plugin, "Drain");
        setDescription("Passive $1 mana drain on attack. CD:$2");
        setTypes(SkillType.COUNTER, SkillType.BUFF, SkillType.MANA);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        int drain = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-drain-per-attack", 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, "drain-increase", 0.0, false) * hero.getSkillLevel(this)));
        drain = drain > 0 ? drain : 0;
        int cooldown = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 500, false) -
                (SkillConfigManager.getUseSetting(hero, this, "cooldown-decrease", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        cooldown = cooldown > 0 ? cooldown : 0;
        String description = getDescription().replace("$1", drain + "").replace("$2", cooldown + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("mana-drain-per-attack", 4);
        node.set("drain-increase", 0);
        node.set(Setting.COOLDOWN.node(), 500);
        node.set("cooldown-decrease", 0);
        node.set("exp-per-drain", 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0)
                return;
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);

                if (hero.hasEffect("Drain")) {
                    if (hero.getCooldown("Drain") == null || hero.getCooldown("Drain") <= System.currentTimeMillis()) {
                        Hero tHero = plugin.getCharacterManager().getHero((Player) event.getEntity());
                        int drain = (int) (SkillConfigManager.getUseSetting(hero, skill, "mana-drain-per-attack", 4, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, "drain-increase", 0.0, false) * hero.getSkillLevel(skill)));
                        drain = drain > 0 ? drain : 0;
                        int cooldown = (int) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 500, false) -
                                (SkillConfigManager.getUseSetting(hero, skill, "cooldown-decrease", 0.0, false) * hero.getSkillLevel(skill)));
                        cooldown = cooldown > 0 ? cooldown : 0;
                        hero.setCooldown("Drain", cooldown + System.currentTimeMillis());
                        if (tHero.getMana() - drain <= 0) {
                            tHero.setMana(0);
                        } else {
                            tHero.setMana(tHero.getMana() - drain);
                        }
                        double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-drain", 0, false);
                        if (exp > 0) {
                            if (hero.hasParty()) {
                                hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                            } else {
                                hero.gainExp(exp, ExperienceType.SKILL);
                            }
                        }
                        return;
                    }
                }
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getCharacterManager().getHero(player);

                    if (hero.hasEffect("Drain")) {
                        if (hero.getCooldown("Drain") == null || hero.getCooldown("Drain") <= System.currentTimeMillis()) {
                            Hero tHero = plugin.getCharacterManager().getHero((Player) event.getEntity());
                            int drain = (int) (SkillConfigManager.getUseSetting(hero, skill, "mana-drain-per-attack", 4, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "drain-increase", 0.0, false) * hero.getSkillLevel(skill)));
                            drain = drain > 0 ? drain : 0;
                            int cooldown = (int) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 500, false) -
                                    (SkillConfigManager.getUseSetting(hero, skill, "cooldown-decrease", 0.0, false) * hero.getSkillLevel(skill)));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            hero.setCooldown("Drain", cooldown + System.currentTimeMillis());
                            if (tHero.getMana() - drain <= 0) {
                                tHero.setMana(0);
                            } else {
                                tHero.setMana(tHero.getMana() - drain);
                            }
                            double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-drain", 0, false);
                            if (exp > 0) {
                                if (hero.hasParty()) {
                                    hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                                } else {
                                    hero.gainExp(exp, ExperienceType.SKILL);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}
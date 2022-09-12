package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.List;

public interface PlayerService {
    Player getPlayer(Long id);

    void deletePlayer(Player player);

    Player updatePlayer(Player savedPlayer, Player player);

    boolean isPlayerValid(Player player);

    Player savePlayer(Player player);

    int calculateLevel(int exp);

    int calculateUntilNextLevel(int exp);

    List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel);

    List<Player> sort(List<Player> players, PlayerOrder order);

    List<Player> getPage(List<Player> sortedPlayers, Integer pageNumber, Integer pageSize);
}

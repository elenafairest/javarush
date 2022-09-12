package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService{
    private PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public Player updatePlayer(Player savedPlayer, Player player) {
        final String name = player.getName();
        if (name != null) {
            if (isNameValid(name)) {
                savedPlayer.setName(name);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        final String title = player.getTitle();
        if (title != null) {
            if (isTitleValid(title)) {
                savedPlayer.setTitle(title);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        final Race race = player.getRace();
        if (race != null) {
            savedPlayer.setRace(race);
        }
        final Profession profession = player.getProfession();
        if (profession != null) {
            savedPlayer.setProfession(profession);
        }
        Date birthDay = player.getBirthday();
        if (birthDay != null) {
            if (isBirthDayValid(birthDay)) {
                savedPlayer.setBirthday(birthDay);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        if (player.getBanned() != null) {
            savedPlayer.setBanned(player.getBanned());
        }
        final Integer experience = player.getExperience();
        if (experience != null) {
            if (isExperienceValid(experience)) {
                savedPlayer.setExperience(experience);
                savedPlayer.setLevel(calculateLevel(experience));
                savedPlayer.setUntilNextLevel(calculateUntilNextLevel(experience));
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        playerRepository.save(savedPlayer);
        return savedPlayer;
    }

    @Override
    public boolean isPlayerValid(Player player) {
        return player != null &&
                isNameValid(player.getName()) &&
                isTitleValid(player.getTitle()) &&
                isBirthDayValid(player.getBirthday()) &&
                isExperienceValid(player.getExperience());
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    private boolean isExperienceValid(Integer experience) {
        int minExp = 0;
        int maxExp = 10000000;
        return  experience != null && experience.compareTo(minExp) >= 0 && experience.compareTo(maxExp) <= 0;
    }

    private boolean isBirthDayValid(Date birthDay) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDay);
        int year = calendar.get(Calendar.YEAR);
        return year >= 2000 && year <= 3000;
    }

    private boolean isNameValid(String name) {
        return isStringValid(name, 12);
    }

    private boolean isTitleValid(String title) {
        return isStringValid(title, 30);
    }

    private boolean isStringValid(String s, int maxLength) {
        return s != null && ! s.isEmpty() && s.length() <= maxLength;
    }

    public int calculateLevel(int exp) {
        return (int) ((Math.sqrt(2500 + (200 * exp)) - 50)/100);
    }

    public int calculateUntilNextLevel(int exp) {
        return 50 * (calculateLevel(exp) + 1) * (calculateLevel(exp) + 2) - exp;
    }

    @Override
    public List<Player> getPlayers(String name, String title, Race race, Profession profession,
                                   Long after, Long before, Boolean banned, Integer minExperience,
                                   Integer maxExperience, Integer minLevel, Integer maxLevel) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);

        final List<Player> players = new ArrayList<>();
        playerRepository.findAll().forEach((player) -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            players.add(player);

        });
        return players;
    }

    @Override
    public List<Player> sort(List<Player> players, PlayerOrder order) {
        if (order == null) {
            order = PlayerOrder.ID;
        }
        PlayerOrder finalOrder = order;
        players.sort((x, y) -> {
            switch (finalOrder) {
                case ID: return x.getId().compareTo(y.getId());
                case NAME: return x.getName().compareTo(y.getName());
                case LEVEL: return x.getLevel().compareTo(y.getLevel());
                case EXPERIENCE: return x.getExperience().compareTo(y.getExperience());
                case BIRTHDAY: return x.getBirthday().compareTo(y.getBirthday());
                default: return 0;
            }
        });
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> sortedPlayers, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageSize == null) {
            pageSize = 3;
        }
        int from = pageNumber * pageSize;
        int to = from + pageSize;
        if (to > sortedPlayers.size()) {
            to = sortedPlayers.size();
        }
        return sortedPlayers.subList(from, to);
    }
}

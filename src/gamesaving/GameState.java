package src.gamesaving;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameState implements Serializable, Comparable<GameState> {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static int idCounter = 0;

    private final int saveID,
                      structureLevel,
                      geneticStrength,
                      feedingType;
    private final double resources,
                         attack,
                         defence;
    private final Date savingTime;


    public GameState(int structureLevel, int geneticStrength, int feedingType, double resources, double attack, double defence) {
        saveID = ++idCounter;
        this.structureLevel = structureLevel;
        this.geneticStrength = geneticStrength;
        this.feedingType = feedingType;
        this.resources = resources;
        this.attack = attack;
        this.defence = defence;
        savingTime = new Date();
    }

    public static int getIdCounter() {
        return idCounter;
    }

    @Override
    public String toString() {
        return ("""
                Состояние №%d от %s:
                уровень организации: %d,
                генетическая сила:%d,
                тип питания: %d;
                ресурсов: %.2f,
                нападение: %.2f,
                защита: %.2f.""").formatted(
                        saveID, timeFormat.format(savingTime),
                        structureLevel, geneticStrength, feedingType,
                        resources, attack, defence);
    }

    public int getSaveID() {
        return saveID;
    }


    @Override
    public int compareTo(GameState o) {
        return savingTime.compareTo(o.savingTime);
    }
}

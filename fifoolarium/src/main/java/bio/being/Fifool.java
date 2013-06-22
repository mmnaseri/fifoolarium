package bio.being;

import com.googlecode.lanterna.terminal.Terminal;

import java.util.Random;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/21, 15:54)
 */
public class Fifool {

    private final int x;
    private final int y;
    private int colony;
    private final Terminal.Color color;

    public Fifool(int x, int y, int colony) {
        this.x = x;
        this.y = y;
        this.colony = colony;
        final Terminal.Color[] enumConstants = Terminal.Color.class.getEnumConstants();
        this.color = enumConstants[new Random().nextInt(enumConstants.length)];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColony() {
        return colony;
    }

    public void setColony(int colony) {
        this.colony = colony;
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    public Terminal.Color getColor() {
        return color;
    }
}

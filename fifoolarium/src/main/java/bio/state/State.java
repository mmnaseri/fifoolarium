package bio.state;

import bio.being.Fifool;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenCharacterStyle;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.*;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/21, 15:35)
 */
public class State {

    private final List<List<Fifool>> grid = new ArrayList<List<Fifool>>();
    private int width;
    private int height;
    private final double seed;
    private int alive = 0;

    public State(int width, int height, double seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        initialize();
    }

    private void initialize() {
        int colony = 0;
        for (int i = 0; i < height; i ++) {
            final ArrayList<Fifool> row = new ArrayList<Fifool>();
            grid.add(row);
            for (int j = 0; j < width; j ++) {
                row.add(new Random().nextDouble() > seed ? new Fifool(j, i, colony ++) : null);
            }
        }
        getColonies();
    }

    public boolean isAlive(int x, int y) {
        return getFifool(x, y) != null;
    }

    public Fifool getFifool(int x, int y) {
        return x < 0 || y < 0 || x >= width || y >= height ? null : grid.get(y).get(x);
    }

    private boolean areAdjacent(Fifool first, Fifool second) {
        return first != null && second != null && Math.abs(first.getX() - second.getX()) <= 1 && Math.abs(first.getY() - second.getY()) <= 1;
    }

    private boolean areAdjacent(Set<Fifool> first, Set<Fifool> second) {
        for (Fifool fromFirst : first) {
            for (Fifool fromSecond : second) {
                if (areAdjacent(fromFirst, fromSecond)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<Integer, Set<Fifool>> mergeColonies(Map<Integer, Set<Fifool>> colonies) {
        Map.Entry<Integer, Integer> change;
        do {
            change = null;
            for (Integer first : colonies.keySet()) {
                for (Integer second : colonies.keySet()) {
                    if (first.equals(second)) {
                        continue;
                    }
                    if (areAdjacent(colonies.get(first), colonies.get(second))) {
                        change = new AbstractMap.SimpleEntry<Integer, Integer>(first, second);
                        break;
                    }
                }
                if (change != null) {
                    break;
                }
            }
            if (change != null) {
                final Set<Fifool> first = colonies.get(change.getKey());
                final Set<Fifool> second = colonies.get(change.getValue());
                first.addAll(second);
                for (Fifool fifool : first) {
                    fifool.setColony(change.getKey());
                }
                second.clear();
                colonies.remove(change.getValue());
            }
        } while (change != null);
        return colonies;
    }

    private Map<Integer, Set<Fifool>> getColonies() {
        final Map<Integer, Set<Fifool>> colonies = new HashMap<Integer, Set<Fifool>>();
        int i = 0;
        alive = 0;
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                if (!isAlive(x, y)) {
                    continue;
                }
                alive ++;
                final Fifool fifool = getFifool(x, y);
                fifool.setColony(i++);
                final HashSet<Fifool> set = new HashSet<Fifool>();
                set.add(fifool);
                colonies.put(fifool.getColony(), set);
            }
        }
        return mergeColonies(colonies);
    }

    public void kill(Fifool fifool, String reason) {
        System.out.println("[" + fifool.getX() + "," + fifool.getY() + "] is dead because " + reason + " :-(");
        grid.get(fifool.getY()).set(fifool.getX(), null);
    }

    private void considerPossibilities(Fifool fifool, Set<Map.Entry<Integer, Integer>> possibilities) {
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX() - 1, fifool.getY() - 1));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX() + 1, fifool.getY() - 1));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX(), fifool.getY() - 1));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX() - 1, fifool.getY()));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX() + 1, fifool.getY()));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX(), fifool.getY()));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX() - 1, fifool.getY() + 1));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX() + 1, fifool.getY() + 1));
        possibilities.add(new AbstractMap.SimpleEntry<Integer, Integer>(fifool.getX(), fifool.getY() + 1));
    }

    public void reproduce(Fifool first, Fifool second) {
        final Set<Map.Entry<Integer, Integer>> possibilities = new HashSet<Map.Entry<Integer, Integer>>();
        considerPossibilities(first, possibilities);
        considerPossibilities(second, possibilities);
        final Set<Map.Entry<Integer, Integer>> deleted = new HashSet<Map.Entry<Integer, Integer>>();
        for (Map.Entry<Integer, Integer> possibility : possibilities) {
            final Integer x = possibility.getKey();
            final Integer y = possibility.getValue();
            if (x < 0 || y < 0 || x >= width || y >= height || isAlive(x, y)) {
                deleted.add(possibility);
            }
        }
        possibilities.removeAll(deleted);
        System.out.println(first + " + " + second + " = ?");
        if (possibilities.isEmpty()) {
            System.out.println("No possible way for " + first + " and " + second + " to reproduce.");
            return;
        }
        System.out.println("Found " + possibilities.size() + " way(s) to reproduce");
        final Object[] objects = possibilities.toArray(new Object[possibilities.size()]);
        //noinspection unchecked
        final Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) objects[new Random().nextInt(objects.length)];
        final Integer y = entry.getValue();
        final Integer x = entry.getKey();
        System.out.println(first + " + " + second + " = [" + x + "," + y + "]");
        grid.get(y).set(x, new Fifool(x, y, first.getColony()));
    }

    public void evolve(Screen screen) {
        final Map<Integer, Set<Fifool>> colonies = getColonies();
        for (Integer colonyNumber : colonies.keySet()) {
            final Set<Fifool> colony = colonies.get(colonyNumber);
            if (colony.size() > 3) {
                while (true){
                    Fifool fifool = null;
                    for (Fifool check : colony) {
                        if (countNeighbors(check) > 3) {
                            fifool = check;
                        }
                    }
                    if (fifool == null) {
                        break;
                    } else {
                        colony.remove(fifool);
                        kill(fifool, "it was too weak");
                    }
                }
            }
            if (colony.size() == 1) {
                kill(colony.iterator().next(), "it was alone");
            } else if (colony.size() > 1) {
                final List<Fifool> list = new ArrayList<Fifool>();
                list.addAll(colony);
                if (list.size() % 2 == 1) {
                    final Fifool missing = list.get(new Random().nextInt(list.size()));
                    System.out.println(missing + " is missing out in the action");
                    list.remove(missing);
                }
                while (!list.isEmpty()) {
                    final Fifool first = list.get(new Random().nextInt(list.size()));
                    list.remove(first);
                    final Fifool second = list.get(new Random().nextInt(list.size()));
                    list.remove(second);
                    if (areAdjacent(first, second)) {
                        reproduce(first, second);
                    } else {
                        System.out.println("Match made in heaven didn't work out for " + first + " and " + second);
                    }
                }
            }
        }
    }

    private int countNeighbors(Fifool fifool) {
        int neighbors = 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX() - 1, fifool.getY() - 1)) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX() - 1, fifool.getY())) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX() - 1, fifool.getY() + 1)) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX(), fifool.getY() - 1)) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX(), fifool.getY())) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX(), fifool.getY() + 1)) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX() + 1, fifool.getY() - 1)) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX() + 1, fifool.getY())) ? 1 : 0;
        neighbors += areAdjacent(fifool, getFifool(fifool.getX() + 1, fifool.getY() + 1)) ? 1 : 0;
        return neighbors;
    }

    public void draw(Screen screen) {
        for (int i = 0; i < height; i ++) {
            for (int j = 0; j < width; j ++) {
                if (!isAlive(j, i)) {
                    screen.putString(j, i, " ", Terminal.Color.BLACK, Terminal.Color.WHITE, ScreenCharacterStyle.Bold);
                } else {
                    screen.putString(j, i, "◆", getFifool(j, i).getColor(), Terminal.Color.WHITE, ScreenCharacterStyle.Bold);
                }
            }
        }
        screen.refresh();
    }

    public int getAlive() {
        return alive;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void resize(int width, int height) {
        if (width < this.width) {
            for (int i = width; i < this.width; i ++) {
                for (int j = 0; j < this.height; j ++) {
                    if (isAlive(i, j)) {
                        kill(getFifool(i, j), "there wasn't enough space!");
                    }
                }
            }
        }
        if (height < this.height) {
            for (int i = 0; i < this.width; i ++) {
                for (int j = height; j < this.height; j ++) {
                    if (isAlive(i, j)) {
                        kill(getFifool(i, j), "there wasn't enough space!");
                    }
                }
            }
        }
        if (width > this.width) {
            for (int i = 0; i < height; i ++) {
                final List<Fifool> row = grid.get(i);
                for (int j = this.width; j < width; j ++) {
                    row.add(null);
                }
            }
        }
        this.width = width;
        if (height > this.height) {
            for (int i = this.height; i < height; i ++) {
                final ArrayList<Fifool> row = new ArrayList<Fifool>();
                for (int j = 0; j < this.width; j ++) {
                    row.add(null);
                }
                grid.add(row);
            }
        }
        this.height = height;
    }

    public double getSeed() {
        return seed;
    }
}

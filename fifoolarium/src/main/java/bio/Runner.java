package bio;

import bio.state.State;
import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenCharacterStyle;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/21, 15:55)
 */
public class Runner {

    private static class StatusPrintStream extends PrintStream {

        private final Screen screen;
        private final List<String> messages = new ArrayList<String>();

        public StatusPrintStream(Screen screen) {
            super(System.out);
            this.screen = screen;
        }

        @Override
        public void println(String x) {
            Logger.getLogger("fifoolarium").info(x);
            messages.add(x);
            while (messages.size() > 4) {
                messages.remove(0);
            }
            String clean = "";
            final TerminalSize terminalSize = screen.getTerminalSize();
            for (int i = 0; i < terminalSize.getColumns(); i ++) {
                clean += " ";
            }
            for (int i = messages.size() - 1; i >= 0; i --) {
                screen.putString(0, terminalSize.getRows() - 2 - i, clean, Terminal.Color.GREEN, Terminal.Color.BLACK);
                screen.putString(0, terminalSize.getRows() - 2 - i, messages.get(messages.size() - i - 1), Terminal.Color.GREEN, Terminal.Color.BLACK);
            }
            screen.refresh();
        }

    }

    public static void main(String[] args) throws Exception {
        final Screen screen = TerminalFacade.createScreen();
        System.setOut(new StatusPrintStream(screen));
        screen.getTerminal().setCursorVisible(false);
        final TerminalSize size = screen.getTerminalSize();
        final State state = new State(size.getColumns(), size.getRows() - 5, 0.925);
        screen.getTerminal().addResizeListener(new Terminal.ResizeListener() {
            @Override
            public void onResized(TerminalSize terminalSize) {
                System.err.println("RESIZE");
                state.resize(terminalSize.getColumns(), terminalSize.getRows() - 5);
            }
        });
        screen.startScreen();
        screen.putString(3, size.getRows() - 1, "Bomb", Terminal.Color.WHITE, Terminal.Color.BLACK, ScreenCharacterStyle.Bold);
        screen.putString(10, size.getRows() - 1, "Pause", Terminal.Color.WHITE, Terminal.Color.BLACK, ScreenCharacterStyle.Bold);
        screen.putString(20, size.getRows() - 1, "Exit", Terminal.Color.WHITE, Terminal.Color.BLACK, ScreenCharacterStyle.Bold);
        screen.putString(3, size.getRows() - 1, "B", Terminal.Color.RED, Terminal.Color.BLACK, ScreenCharacterStyle.Bold);
        screen.putString(10, size.getRows() - 1, "P", Terminal.Color.RED, Terminal.Color.BLACK, ScreenCharacterStyle.Bold);
        screen.putString(21, size.getRows() - 1, "x", Terminal.Color.RED, Terminal.Color.BLACK, ScreenCharacterStyle.Bold);
        screen.refresh();
        System.out.println(" Welcome to Fifoolarium!");
        System.out.println("  > play the god with fifools");
        System.out.println(" Vacancy probability is: %" + (double) Math.round(state.getSeed() * 1000) / 10);
        System.out.println(" Press any key to continue ...");
        while (screen.readInput() == null);
        System.out.println(">> Starting ecosystem");
        boolean stop = false;
        int day = 0;
        do {
            boolean ignore = false;
            state.draw(screen);
            int i = 0;
            Key key;
            while (i++ < 100) {
                key = screen.readInput();
                if (key != null) {
                    if (key.getCharacter() == 'x') {
                        stop = true;
                        break;
                    } else if (key.getCharacter() == 'p') {
                        System.out.println(">> PAUSED");
                        while (screen.readInput() == null);
                        System.out.println(">> RESUMED");
                        break;
                    } else if (key.getCharacter() == 'b') {
                        int bomb = 20;
                        int casualties = 0;
                        int startX = new Random().nextInt(state.getWidth() - bomb);
                        int startY = new Random().nextInt(state.getHeight() - bomb);
                        for (int m = startX; m < startX + bomb; m ++) {
                            for (int n = startY; n < startY + bomb; n ++) {
                                screen.putString(m, n, "*", Terminal.Color.YELLOW, Terminal.Color.RED, ScreenCharacterStyle.Bold);
                                if (state.isAlive(m, n)) {
                                    state.kill(state.getFifool(m, n), "it was bombed by terrorists");
                                    casualties ++;
                                }
                            }
                        }
                        screen.refresh();
                        System.out.println("The bomb had " + casualties + " casualties");
                        Thread.sleep(10);
                        for (int m = startX; m < startX + bomb; m ++) {
                            for (int n = startY; n < startY + bomb; n ++) {
                                screen.putString(m, n, " ", Terminal.Color.WHITE, Terminal.Color.WHITE, ScreenCharacterStyle.Bold);
                            }
                        }
                        screen.refresh();
                        ignore = true;
                    }
                }
                Thread.sleep(1);
            }
            if (ignore) {
                continue;
            }
            if (stop) {
                break;
            }
            day ++;
            System.out.println(">> Going to evolve ...");
            final TerminalSize terminalSize = screen.getTerminalSize();
            String expression = "                %" + ((Math.round((double) state.getAlive() * 10000 / (size.getColumns() * size.getRows()))) / 100d) + "|" + state.getAlive() + "|" + day;
            screen.putString(terminalSize.getColumns() - expression.length() - 1, terminalSize.getRows() - 1, expression, Terminal.Color.CYAN, Terminal.Color.DEFAULT);
            screen.refresh();
            if (state.getAlive() == 0) {
                System.out.println("Sadly, all fifools are dead.");
                System.out.println("Press any key to continue ...");
                while (screen.readInput() == null);
                break;
            }
            state.evolve(screen);
        } while (true);
        System.out.println(">> Evolution stopped.");
        Thread.sleep(1000);
        screen.stopScreen();
    }

}


import java.util.Arrays;

/* Written on Java SE 1.6.0 in NetBeans IDE 6.9.1 in august 2011, SPb, Russia.
 * Ideas and comments you can sent to PolarHare@gmail.com
 */

/**
 * Class allows to interpret BrainFuck source with run-time input and output
 * quite fast. ~O(n) where n - count of steps throw source.And constants also
 * quite small ;-).
 *
 * At first call constructor, then prepare new program(call prepareNewProgram(Source))
 * and by using interpret and addInput(if program need input-then interpret will
 * return true) execute program. To get output use getOutput as long as it will not
 * return -1. ;-)
 * 
 * @author Nickolay Polyarniy PolarHare
 */
public class BFInterpreter {

    private final int cellsCount;
    private final int outputMaxSize;
    private final int inputMaxSize;
    /**
     * Hold index of closing brake corresponding to i-opening braket
     */
    private int[] closingBrakets;
    /**
     * Hold index of opening brake corresponding to i-closing braket
     */
    private int[] openingBrakets;
    private char[] source;
    private int sourcePointer;
    /**
     * Hold memory position
     */
    private int pointer;
    private byte[] cells;
    private char[] output;
    private int nextOutput;
    private int nextUnreadedOutput;
    private char[] input;
    private int nextInput;
    private int nextUnreadedInput;

    public BFInterpreter() {
        cellsCount = 32768;
        outputMaxSize = 32768;
        inputMaxSize = 32768;
        initMemory();
    }

    public BFInterpreter(int cellsCount, int outputMaxSize, int inputMaxSize) {
        this.cellsCount = cellsCount;
        this.outputMaxSize = outputMaxSize;
        this.inputMaxSize = inputMaxSize;
        initMemory();
    }

    private void initMemory() {
        cells = new byte[cellsCount];
        output = new char[outputMaxSize];
        input = new char[inputMaxSize];
    }

    /**
     * You must call it every time, when you need to execute new source.
     * @param source - source code of BrainFuck program (all except ,.+-<>[] will
     * be ignored)
     * @throws Exception if source uncorrect(uncorect brackets)
     */
    public void prepareNewProgram(char[] source) throws Exception {
        this.source = source;
        openingBrakets = new int[source.length];
        closingBrakets = new int[source.length];
        sourcePointer = 0;
        pointer = 0;
        Arrays.fill(cells, (byte) 0);
        nextOutput = 0;
        nextInput = 0;
        nextUnreadedInput = 0;
        nextUnreadedOutput = 0;
        checkBrakets(0, 0);
    }

    /**
     * Here we are checking brakets correctness and initializing links to closing
     * brakets from opening brakets and visa versa.
     * @throws throws exception, if source code has too more closing or opening brakets
     * @param ind - index of first char in source in this brakets
     * @param sum - current sum of brakets( '[' == +1 and ']' == -1 )
     * @return index of closing braket
     */
    private int checkBrakets(int ind, int sum) throws Exception {
        for (int i = ind; i < source.length; i++) {
            if (source[i] == '[') {
                i = checkBrakets(i + 1, sum + 1) + 1;
            } else if (source[i] == ']') {
                if (sum == 0) {
                    System.err.println("ERROR: " + new String(source));
                    throw new Exception("Unopened braket.");
                } else {
                    openingBrakets[i] = ind - 1;
                    closingBrakets[ind - 1] = i;
                    return i;
                }
            }
        }
        if (sum > 0) {
            System.err.println("ERROR: " + new String(source));
            throw new Exception("Unclosed braket.");
        }
        return -239;
    }

    /**
     * @return If available return next output byte (you can convert it to char),
     *         else return -1.
     */
    public int getOutput() {
        if (nextUnreadedOutput < nextOutput) {
            nextUnreadedOutput++;
            return output[nextUnreadedOutput - 1];
        } else {
            return -1;
        }
    }

    public char[] getAllAvailableOutput() {
        if (nextUnreadedOutput < nextOutput) {
            char[] res = new char[nextOutput - nextUnreadedOutput];
            for (int i = 0; i < res.length; i++) {
                res[i] = output[nextUnreadedOutput];
                nextUnreadedOutput++;
            }
            return res;
        } else {
            return null;
        }
    }

    public void addInput(byte[] c) {
        for (int i = 0; i < c.length; i++) {
            addInput(c[i]);
        }
    }

    public void addInput(byte c) {
        input[nextInput] = (char) c;
        nextInput++;
    }

    public void addInput(char[] c) {
        for (int i = 0; i < c.length; i++) {
            addInput(c[i]);
        }
    }

    public void addInput(char c) {
        input[nextInput] = c;
        nextInput++;
    }

    /**
     * Start(or continue, if it was interrupted for input) to interpret program.
     * @throws if source was not setted or program already ended.
     * @return true - if program is waiting for input(and after entry the input
     *         data, you must call interpret() again), false - if program ended.
     */
    public boolean interpret() throws Exception {
        while (sourcePointer < source.length) {
            switch (source[sourcePointer]) {
                case '+': {
                    incrementValue();
                    break;
                }
                case '-': {
                    decrementValue();
                    break;
                }
                case '<': {
                    decrementPointer();
                    break;
                }
                case '>': {
                    incrementPointer();
                    break;
                }
                case '[': {
                    cycleStart();
                    break;
                }
                case ']': {
                    cycleEnd();
                    break;
                }
                case ',': {
                    if (input()) {
                        return true;
                    }
                    break;
                }
                case '.': {
                    output();
                    break;
                }
            }
        }
        return false;
    }

    private void incrementPointer() {
        pointer = (pointer + 1) % cellsCount;
        sourcePointer++;
    }

    private void decrementPointer() {
        pointer = (pointer - 1 + cellsCount) % cellsCount;
        sourcePointer++;
    }

    private void incrementValue() {
        cells[pointer]++;
        sourcePointer++;
    }

    private void decrementValue() {
        cells[pointer]--;
        sourcePointer++;
    }

    private void output() {
        output[nextOutput] = (char) cells[pointer];
        nextOutput++;
        sourcePointer++;
    }

    /**
     * @return true - if input chars ended and we need more input.
     */
    private boolean input() {
        if (nextUnreadedInput >= nextInput) {
            return true;
        } else {
            cells[pointer] = (byte) input[nextUnreadedInput];
            nextUnreadedInput++;
            sourcePointer++;
            return false;
        }
    }

    private void cycleStart() {
        if (cells[pointer] == 0) {
            sourcePointer = closingBrakets[sourcePointer] + 1;
        } else {
            sourcePointer++;
        }
    }

    private void cycleEnd() {
        sourcePointer = openingBrakets[sourcePointer];
        cycleStart();
    }
}

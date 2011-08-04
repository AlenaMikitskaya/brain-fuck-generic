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

    private final int CELLS_COUNT;
    private final int OUTPUT_MAX_SIZE;
    private final int INPUT_MAX_SIZE;

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
        CELLS_COUNT = 32768;
        OUTPUT_MAX_SIZE = 32768;
        INPUT_MAX_SIZE = 32768;
        initMemory();
    }

    public BFInterpreter(int cellsCount, int outputMaxSize, int inputMaxSize) {
        this.CELLS_COUNT = cellsCount;
        this.OUTPUT_MAX_SIZE = outputMaxSize;
        this.INPUT_MAX_SIZE = inputMaxSize;
        initMemory();
    }

    private void initMemory() {
        cells = new byte[CELLS_COUNT];
        output = new char[OUTPUT_MAX_SIZE];
        input = new char[INPUT_MAX_SIZE];
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
     * Interpret one step of program.(Usefull in debug - smth like F7).
     * @throws if source was not setted or program already ended.
     * @return true - if program is waiting for input(and after entry the input
     *         data, you must call interpret() again), false - if program ready
     *         to continue execution.
     */
    public boolean interpretOneStep() throws Exception {
        int result = interpretUpTo(sourcePointer);
        if (result == source.length) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Start(or continue, if it was interrupted for input) to interpret program
     * up to end or up to next input, if we will need more input.
     * @throws if source was not setted or program already ended.
     * @return true - if program is waiting for input(and after entry the input
     *         data, you must call interpret() again), false - if program ended.
     */
    public boolean interpret() throws Exception {
        int result = interpretUpTo(source.length - 1);
        if (result == source.length) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Start(or continue, if it was interrupted for input) to interpret program
     * up to choosed symbol.
     * @throws if source was not setted or program already ended.
     * @return index. If index == lastCommand + 1, then program interpreted up
     *         to choosed index. If index <= lastCommand, then program need more
     *         input.
     */
    public int interpretUpTo(int lastCommand) throws Exception {
        while (sourcePointer <= lastCommand) {
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
                        return sourcePointer;
                    }
                    break;
                }
                case '.': {
                    output();
                    break;
                }
            }
        }
        return sourcePointer;
    }

    private void incrementPointer() {
        pointer = (pointer + 1) % CELLS_COUNT;
        sourcePointer++;
    }

    private void decrementPointer() {
        pointer = (pointer - 1 + CELLS_COUNT) % CELLS_COUNT;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author Николай Полярный PolarHare
 */
public class InterpreterTesting {

    public static void main(String[] args) throws Exception {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        BFInterpreter bfi = new BFInterpreter();
        //выводит "abc"
        bfi.prepareNewProgram("++++++++[<++++++++++++>-]<+.+.+.".toCharArray());
        bfi.interpret();
        String output = new String(bfi.getAllAvailableOutput());
        System.out.println(output);

        //сортировка
        bfi.prepareNewProgram(">>,[>>,]<<[[-<+<]>[>[>>]<[.[-]<[[>>+<<-]<]>>]>]<<]".toCharArray());
        String s = input.readLine();
        int i = 0;
        while (bfi.interpret()) {
            //точку мы считаем символом с кодом 0, т.к. исполняемый алгоритм
            //считывает символы до встреченного байта 0. Т.е. если считывание идет
            //по байтно(из файла или просто числами), то все нормально, но здесь
            //считывание с клавиатуры символов, соответственно считать нулевой символ
            //сложновато. Поэтому точка считается нулевым символом.
            char readedChar;
            if (s.charAt(i) == '.') {
                readedChar = (char) 0;
            } else {
                readedChar = s.charAt(i);
            }
            bfi.addInput(readedChar);
            i++;
        }
        output = new String(bfi.getAllAvailableOutput());
        System.out.println(output);
    }
}

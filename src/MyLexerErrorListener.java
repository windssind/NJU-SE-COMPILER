import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;

public class MyLexerErrorListener extends BaseErrorListener {

    boolean hasError = false;
    // 存储所有的错误信息
    ArrayList<String> errorMsg = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errorMsg.add(String.format("Error type A at Line %d:[errorMessage]", line));
        hasError  = true;
    }
    public  void printLexerErrorInformation(){
        for (String msg: errorMsg){
            System.err.println(msg);
        }
    }
}

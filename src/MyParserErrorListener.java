import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;

public class MyParserErrorListener extends BaseErrorListener {
    boolean hasError = false;
    // 存储所有的错误信息
    ArrayList<String> errorMsg = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errorMsg.add(String.format("Error type B at Line %d:[%s]", line,msg));
        hasError  = true;
    }
    public  void printParserErrorInfomation(){
        for (String msg: errorMsg){
            System.err.println(msg);
        }
    }
}

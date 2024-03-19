
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import javax.print.DocFlavor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main
{
    private  static SysYLexer sysYLexer ;
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        sysYLexer = new SysYLexer(input);
        MyErrorListener myErrorListener = new MyErrorListener();
        sysYLexer.removeErrorListeners();
        sysYLexer.addErrorListener(myErrorListener);

        // 获得所有的Token
        List<? extends Token> myTokens = sysYLexer.getAllTokens();


        if (myErrorListener.hasError) {
            // 假设myErrorListener有一个错误信息输出函数printLexerErrorInformation.
            myErrorListener.printLexerErrorInformation();
        } else {
            for (Token t : myTokens) {
                printSysYTokenInformation(t);
            }
        }

    }

    private  static void printSysYTokenInformation(Token t){
        String tokenName = sysYLexer.VOCABULARY.getSymbolicName(t.getType());
        String text = t.getText();
        if (tokenName.equals("INTEGER_CONST")){
            if (text.startsWith("0x") || text.startsWith("0X")){
                text = String.valueOf(Integer.parseInt(text.substring(2),16));
            }else if (text.startsWith("0")){
                text = String.valueOf(Integer.parseInt(text,8));
            }
        }
        System.err.println(String.format("%s %s at Line %d.",tokenName,text,t.getLine()));
    }
}

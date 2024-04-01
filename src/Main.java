
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
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
        MyLexerErrorListener myLexerErrorListener = new MyLexerErrorListener();
        sysYLexer.removeErrorListeners();
        sysYLexer.addErrorListener(myLexerErrorListener);

        // 获得所有的Token
        List<? extends Token> myTokens = sysYLexer.getAllTokens();


        if (myLexerErrorListener.hasError) {
            // 假设myErrorListener有一个错误信息输出函数printLexerErrorInformation.
            myLexerErrorListener.printLexerErrorInformation();
            return;
        } else {
            for (Token t : myTokens) {
                printSysYTokenInformation(t);
            }
        }

        // Parser部分
        CommonTokenStream tokenStream = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokenStream);
        MyParserErrorListener myParserErrorListener = new MyParserErrorListener();
        // 添加错误处理器
        // 需要事先移除默认的Listener，不然会输出不需要的
        sysYParser.removeErrorListeners();
        sysYParser.addErrorListener(myParserErrorListener);

        // 获取编译的树
        ParseTree  tree = sysYParser.exp();

        if (myParserErrorListener.hasError) {
            // 假设myErrorListener有一个错误信息输出函数printLexerErrorInformation.
            myParserErrorListener.printParserErrorInfomation();
            return;// 直接返回
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

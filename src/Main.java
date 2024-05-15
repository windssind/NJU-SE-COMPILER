
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bytedeco.javacpp.BytePointer;

import java.io.IOException;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMDumpModule;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintModuleToFile;

public class Main
{
    private  static SysYLexer sysYLexer ;
    public static void main(String[] args)  throws Exception{
        if (args.length < 2) {
            System.err.println("input path is required");
        }
        String source = args[0];
        String filePath = args[1];
        CharStream input = CharStreams.fromFileName(source);
        sysYLexer = new SysYLexer(input);
        MyLexerErrorListener myLexerErrorListener = new MyLexerErrorListener();
        sysYLexer.removeErrorListeners();
        sysYLexer.addErrorListener(myLexerErrorListener);


        // Parser部分
        CommonTokenStream tokenStream = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokenStream);
        MyParserErrorListener myParserErrorListener = new MyParserErrorListener();
        // 添加错误处理器
        // 需要事先移除默认的Listener，不然会输出不需要的
        sysYParser.removeErrorListeners();

        // 获取编译的树
        ParseTree  tree = sysYParser.program();

        //throw new Exception(tree.getText());


        // 进行中间代码输出
        LLVMVistor llvmVistor = new LLVMVistor();
        llvmVistor.Init();
        llvmVistor.visit(tree);
        LLVMDumpModule(llvmVistor.module);
        BytePointer error = new BytePointer();
        LLVMPrintModuleToFile(llvmVistor.module,filePath,error);

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

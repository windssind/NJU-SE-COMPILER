
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Token;

import java.util.Stack;

public class MyParserVisitor extends SysYParserBaseVisitor<Void> {

    private int indentation = 0;
    private boolean isInDecl = false;

    private boolean isInStmtNotBlock = false;
    private boolean hasASpace = false;

    private boolean isInLineOne = true;

    private SGR_Name[] colorTable = {SGR_Name.LightRed, SGR_Name.LightGreen, SGR_Name.LightYellow, SGR_Name.LightBlue, SGR_Name.LightMagenta, SGR_Name.LightCyan};
    private int colorIndex = 0;

    private Stack<SGR_Name> colorStack = new Stack<>();

    @Override
    public Void visitTerminal(TerminalNode node) {
        Token nodeToken = node.getSymbol();
        ParseTree parentNode = node.getParent(); // 此时返回的是一个nonTerminalNode
        if (!(parentNode instanceof RuleNode)) {
            System.err.println("Error");
            return null;
        }
        switch (nodeToken.getType()) {
            case SysYLexer.BREAK:
            case SysYLexer.CONTINUE:
            case SysYLexer.IF:
            case SysYLexer.ELSE:
            case SysYLexer.INT:
            case SysYLexer.RETURN:
            case SysYLexer.WHILE:
            case SysYLexer.CONST:
            case SysYLexer.VOID:
                if (isELSE(node)) {
                    System.out.print("\n");
                    isInLineOne = false;
                    PrintIndentation();
                }
                PrintTerminalNode(SGR_Name.LightCyan, node.getText(), isInDecl);
                if ((!isCONTINUEBREAKRETURN(node) || (isRETURN(node) && !isRETURNNeighborsSEMICOLON(node)))) {
                    if (!hasASpace) {
                        if ((!isELSE(node)) ||((isELSE(node) && !isElseNeighborsASingleStmt(node)))){
                            System.out.print(" ");
                            hasASpace = true;
                        }
                    }
                }
                break;
            case SysYLexer.PLUS:
            case SysYLexer.MINUS:
            case SysYLexer.MUL:
            case SysYLexer.DIV:
            case SysYLexer.MOD:
            case SysYLexer.AND:
            case SysYLexer.OR:
            case SysYLexer.NOT:
            case SysYLexer.EQ:
            case SysYLexer.LE:
            case SysYLexer.GE:
            case SysYLexer.LT:
            case SysYLexer.GT:
            case SysYLexer.NEQ:
            case SysYLexer.ASSIGN:
            case SysYLexer.SEMICOLON:
            case SysYLexer.COMMA:
                if (isBinaryOp(node)) {
                    if (!hasASpace) {
                        System.out.print(" ");
                        hasASpace = true;
                    }
                }
                PrintTerminalNode(SGR_Name.LightRed, node.getText(), isInDecl);
                if (isCOMMA(node) || isBinaryOp(node)) {
                    if (!hasASpace) {
                        System.out.print(" ");
                        hasASpace = true;
                    }
                }
                break;
            case SysYLexer.INTEGER_CONST:
                PrintTerminalNode(SGR_Name.Magenta, node.getText(), isInDecl);
                break;
            case SysYLexer.IDENT:
                // 这个是函数定义
                int parentNodeIndex = ((RuleNode) parentNode).getRuleContext().getRuleIndex();
                boolean isFunc = !(parentNodeIndex == SysYParser.RULE_lVal || parentNodeIndex == SysYParser.RULE_constDef || parentNodeIndex == SysYParser.RULE_varDef || parentNodeIndex == SysYParser.RULE_funcFParam);
                if (isFunc) {
                    PrintTerminalNode(SGR_Name.LightYellow, node.getText(), isInDecl);
                } else {
                    SGR_Name color ;
                    if (isInDecl) {
                        color = SGR_Name.LightMagenta;
                    } else if(isInStmtNotBlock){
                        color = SGR_Name.White;
                    }else{
                        color = null;
                    }
                    PrintTerminalNode(color, node.getText(), isInDecl);
                }
                break;
            case SysYLexer.L_BRACE:
                // 如果是Block的子结点或者decl（{1,2,3,4}）的子结点这样，就不需要额外输出一个空格
                if (!isSonOfASingleBlock(node) && !isSonOfDecl(node)) {
                    if (!hasASpace) {
                        System.out.print(" ");
                        hasASpace = true;
                    }
                }
                /* 如果{前面跟的是if或者else，那么就不需要输出一个空格*/
                colorStack.push(colorTable[colorIndex]);
                PrintTerminalNode(colorTable[colorIndex], node.getText(), isInDecl);
                colorIndex++;
                colorIndex = colorIndex % colorTable.length;

                break;
            case SysYLexer.R_BRACE:
                if (isSonOfDecl(node)) {
                    PrintTerminalNode(colorStack.pop(), node.getText(), isInDecl);
                    colorIndex--;
                    colorIndex = (colorIndex + colorTable.length) % colorTable.length;
                } else {
                    System.out.print("\n");
                    isInLineOne = false;
                    hasASpace = false;
                    indentation -= 1;// 缩进暂时减去1,还要恢复,让控制整个block的缩进的逻辑放到visitBlock里面
                    PrintIndentation();
                    PrintTerminalNode(colorStack.pop(), node.getText(), isInDecl);
                    colorIndex--;
                    colorIndex = (colorIndex + colorTable.length) % colorTable.length;
                    indentation += 1;
                }
                break;
            case SysYLexer.L_PAREN:
            case SysYLexer.L_BRACKT:
                colorStack.push(colorTable[colorIndex]);
                PrintTerminalNode(colorTable[colorIndex], node.getText(), isInDecl);
                colorIndex++;
                colorIndex = colorIndex % colorTable.length;
                break;
            case SysYLexer.R_PAREN:
            case SysYLexer.R_BRACKT:
                PrintTerminalNode(colorStack.pop(), node.getText(), isInDecl);
                colorIndex--;
                colorIndex = (colorIndex + colorTable.length) % colorTable.length;
                break;
            default:
                break;
        }
        return null;
    }

    // 直接输出一个空行就行了
    @Override
    public Void visitDecl(SysYParser.DeclContext ctx) {
        if (!isInLineOne){
            System.out.println();
            PrintIndentation();
        }
        isInDecl = true;
        super.visitDecl(ctx);
        isInDecl = false;
        isInLineOne = false;
        return null;
    }


    @Override
    public Void visitStmt(SysYParser.StmtContext ctx) {
        // 如果这个语句是if或者else或者while后面接的stmt并且这个是一个block 或者这是一个If开头跟在ELSE后面的Stmt，那么就不需要输出一个空行
        // 对！这个逻辑就顺畅了 基本确定是正确的，不要再改动了
        if (isStmtSingleINWhileIfAndNotABlockOrIfNotFollowsElse(ctx)) {
            indentation += 1;
        }
        if (!isIFELSEWHILEStmtAndIsABlock(ctx) &&  !isStmtFollowsElseAndBeginsWithIf(ctx) && !isInLineOne) {
            System.out.println();
            isInLineOne = false;
            PrintIndentation();
        }
        // TODO:如果是if后面只带了一个单行的stmt，就indentation+1
        if (InStmtNotBlock(ctx)){
            isInStmtNotBlock = true;
        }
        super.visitStmt(ctx);
        if (isStmtSingleINWhileIfAndNotABlockOrIfNotFollowsElse(ctx)) {
            indentation -= 1;
        }
        isInStmtNotBlock = false;
        isInLineOne = false;
        return null;
    }

    @Override
    public Void visitBlock(SysYParser.BlockContext ctx) {
        // 如果是单独的block或者函数里面的block，就让indentation+1,if else等控制的block让if else给indention增加
        // 错误的，单独的block不需要indentation +1
        indentation += 1;
        // 如果是单独的块，就输出一个空行
        // 在执行visitBlock之前已经执行了visitStmt，所以已经换行了
        super.visitBlock(ctx);
        indentation -= 1;
        return null;
    }

    @Override
    public Void visitFuncDef(SysYParser.FuncDefContext ctx) {
        // 如果是函数定义不在第一行,就输出一个空行
        if (!isInLineOne) {
            System.out.println();
            System.out.println();
            isInLineOne = false;
            hasASpace = false;
        }
        isInLineOne = false;
        super.visitFuncDef(ctx);

        return null;
    }



    // 用于判断左括号和右括号是否是decl 的子结点
    // 实际上直接判断父亲是否是初值就行了
    private boolean isSonOfDecl(ParseTree node) {
        ParseTree parentNode = node.getParent();
        if (!(parentNode instanceof RuleNode)) {
            System.err.println("wrong");
            System.exit(0);
        }

        return (((RuleNode) parentNode).getRuleContext().getRuleIndex() == SysYParser.RULE_constInitVal || ((RuleNode) parentNode).getRuleContext().getRuleIndex() == SysYParser.RULE_initVal);
    }

    private void PrintIndentation() {
        for (int i = 0; i < indentation; ++i) {
            System.out.print("    ");
            hasASpace = false;
        }
    }

    // 判断{是否是一个单独的代码块的子结点
    private boolean isSonOfASingleBlock(ParseTree node) {
        ParseTree parentNode = node.getParent();
        if (!(parentNode instanceof RuleNode)) {
            System.err.println("wrong");
            System.exit(0);
        }
        /*
        for (int i = 0; i < 10; ++i) {
            if (((RuleNode) parentNode).getRuleContext().getRuleIndex() == SysYParser.RULE_block) {
                parentNode = parentNode.getParent();
                if (((RuleNode) parentNode).getRuleContext().getRuleIndex() == SysYParser.RULE_stmt) {
                    parentNode = parentNode.getParent(); // 走到stmt的stmt环节
                    return parentNode.getChildCount() == 1;
                } else {
                    return false;
                }
            }
            parentNode = parentNode.getParent();
            if (parentNode == null) return false;
        }*/
        if (((RuleNode) parentNode).getRuleContext().getRuleIndex() == SysYParser.RULE_block) {
            parentNode = parentNode.getParent();
            if (((RuleNode) parentNode).getRuleContext().getRuleIndex() == SysYParser.RULE_stmt) {
                parentNode = parentNode.getParent(); // 走到stmt的stmt环节
                return parentNode.getChildCount() == 1;
            } else {
                return false;
            }
        }
        return false;
    }

    // 该函数用于判断stmt是否需要输出一个空行
    // 如果是IFELSEWHILE里面的stmt并且自己的孩子结点是一个block，那么就不需要换行
    /*

    IF L_PAREN cond R_PAREN stmt ( ELSE stmt ) // if语句
    | WHILE L_PAREN cond R_PAREN stmt // while语句
    // 如果是if语句或者while语句,stmt就不需要输出一个空行
     */

    // 但是要注意,if (cond) stmt这样的语句也需要输出一个空行，不要误伤
    //判断我这个stmt是否是if语句或者while语句

    //TODO: 整理一下这个的思路，逻辑有点混乱了
    // 如果是if 里面的stmt或者是while里面的stmt，就不需要输出一个空行
    // if 和 while里面的stmt只要不是block，
    // 如果是 if (cond) stmt(不是block的stmt) while(cond)stmt，就输出真
    private boolean isIFELSEWHILEStmtAndIsABlock(ParseTree node) {
        ParseTree parentNode = node.getParent();
        if (!(parentNode instanceof RuleNode)) {
            System.err.println("wrong");
            System.exit(0);
        }

        ParseTree brother1 = parentNode.getChild(0);
        if (!(brother1 instanceof TerminalNode)) {
            return false;
        }
        boolean isIFELSEWHILE = false;
        boolean isStmtABlock = false;
        if (((TerminalNode) brother1).getSymbol().getType() == SysYLexer.IF || ((TerminalNode) brother1).getSymbol().getType() == SysYLexer.WHILE) {
            isIFELSEWHILE = true;
        }
        if (parentNode.getChildCount() >= 5) {
            ParseTree brother5 = parentNode.getChild(5);
            if (brother5 instanceof TerminalNode) {
                if (((TerminalNode) brother5).getSymbol().getType() == SysYLexer.ELSE) {
                    isIFELSEWHILE = true;
                }
            }
        }
        ParseTree childNode = node.getChild(0);
        if (childNode instanceof RuleNode) {
            if (((RuleNode) childNode).getRuleContext().getRuleIndex() == SysYParser.RULE_block) {
                isStmtABlock = true;
            }
        }
        return isStmtABlock && isIFELSEWHILE;
    }

    private boolean isCONTINUEBREAKRETURN(TerminalNode node) {
        return (node.getSymbol().getType() == SysYLexer.CONTINUE || node.getSymbol().getType() == SysYLexer.BREAK || node.getSymbol().getType() == SysYLexer.RETURN);
    }

    private boolean isRETURN(TerminalNode node) {
        return (node.getSymbol().getType() == SysYLexer.RETURN);
    }

    private boolean isRETURNNeighborsSEMICOLON(TerminalNode node) {
        ParseTree parentNode = node.getParent();
        if (!(parentNode instanceof RuleNode)) {
            System.err.print("wrong");
            System.exit(0);
        }
        ParseTree child1 = parentNode.getChild(1);
        if (!(child1 instanceof TerminalNode)) {
            return false;
        }
        String child1Text = ((TerminalNode) child1).getText();
        if (child1Text.compareTo(";") == 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isBinaryOp(TerminalNode node) {
        return (((node.getSymbol().getType() == SysYLexer.PLUS || node.getSymbol().getType() == SysYLexer.MINUS) && ((RuleNode) node.getParent()).getChildCount() == 3) || node.getSymbol().getType() == SysYLexer.MUL || node.getSymbol().getType() == SysYLexer.DIV || node.getSymbol().getType() == SysYLexer.MOD || node.getSymbol().getType() == SysYLexer.AND || node.getSymbol().getType() == SysYLexer.OR || node.getSymbol().getType() == SysYLexer.EQ || node.getSymbol().getType() == SysYLexer.NEQ || node.getSymbol().getType() == SysYLexer.LE || node.getSymbol().getType() == SysYLexer.LT || node.getSymbol().getType() == SysYLexer.GE || node.getSymbol().getType() == SysYLexer.GT || node.getSymbol().getType() == SysYLexer.ASSIGN);
    }


    private boolean isELSE(TerminalNode node) {
        return node.getSymbol().getType() == SysYLexer.ELSE;
    }

    private boolean isCOMMA(TerminalNode node) {
        return node.getSymbol().getType() == SysYLexer.COMMA;
    }


    private void PrintTerminalNode(SGR_Name color, String text, boolean isUnderScore) {
        if (color == null){
            System.out.print(text);
            return;
        }
        if (isUnderScore) {
            System.out.print("\033[" + color.getValue() + ";" + "4m" + text + "\033[0m");
        } else {
            System.out.print("\033[" + color.getValue() + "m" + text + "\033[0m");
        }
        hasASpace = false;
    }


    // 用于控制缩进，IFWHILE后面不是一个单独的block就需要手动+缩进，或者else后面没有if也需要手动+缩进
    // 新的思考方向: 这个if else是嵌套在别的if else里面的
    private boolean isStmtSingleINWhileIfAndNotABlockOrIfNotFollowsElse(ParseTree node) {
        // 刚开始判断这个stmt是否是while或者if中的语句
        ParseTree parentNode = node.getParent();
        if (!(parentNode instanceof RuleNode)) {
            System.err.println("wrong");
            System.exit(0);
        }

        ParseTree brother1 = parentNode.getChild(0);
        if (!(brother1 instanceof TerminalNode)) {
            return false;
        }
        boolean isIfWhile = false;
        boolean isStmtABlock = false;
        boolean beginsWIthIf = false;
        // 判断是否是IF或者While后面的stmt
        if (parentNode.getChild(4) == node &&(((TerminalNode) brother1).getSymbol().getType() == SysYLexer.IF || ((TerminalNode) brother1).getSymbol().getType() == SysYLexer.WHILE) ){
            isIfWhile = true;
        }
        ParseTree childNode = node.getChild(0);
        if (childNode instanceof RuleNode) {
            if (((RuleNode) childNode).getRuleContext().getRuleIndex() == SysYParser.RULE_block) {
                isStmtABlock = true;
            }
        }
        if ((isIfWhile) ) return !isStmtABlock; // 说明是if while 后面的stmt并且不是block，可以缩进+1

        // 到了这里说明这个stmt是else之后的
        if (parentNode.getChildCount() >= 5) {
            ParseTree brother5 = parentNode.getChild(5);
            ParseTree brother6 = parentNode.getChild(6);
            if (brother6 == node && brother5 instanceof TerminalNode) {
                isIfWhile = false;
                if (((TerminalNode) brother5).getSymbol().getType() == SysYLexer.ELSE) {
                    if (childNode instanceof TerminalNode){
                        if (((TerminalNode) childNode).getSymbol().getType() == SysYLexer.IF){
                            beginsWIthIf = true;
                        }
                    }
                }
            }
        }
        return !beginsWIthIf;
    }


    // 如果这个stmt结点是一个单独的block，就返回false
    private boolean InStmtNotBlock(ParseTree node){
        ParseTree childNode = node.getChild(0);
        if (childNode instanceof RuleNode) {
            return  !(((RuleNode) childNode).getRuleContext().getRuleIndex() == SysYParser.RULE_block);
        }else{
            return true;
        }
    }

    // 用于去除Else带单个语句后面的空格
    // 如果Else后面带的是SingleStmt(就是不是block),就不需要输出空格
    private boolean isElseNeighborsASingleStmt(TerminalNode node){
        ParseTree parentNode = node.getParent();
        if (!(parentNode instanceof RuleNode)) {
            System.err.println("wrong");
            System.exit(0);
        }
        ParseTree brother6 = parentNode.getChild(6);
        if ((brother6 instanceof TerminalNode)) {
            return false;
        }
        return brother6.getChildCount() > 1 && brother6.getChild(brother6.getChildCount() - 1) instanceof TerminalNode &&((TerminalNode)(brother6.getChild(brother6.getChildCount() - 1)) ).getSymbol().getType() == SysYLexer.SEMICOLON;
    }

    // 这个也是Stmt不需要输出空行
    private boolean isStmtFollowsElseAndBeginsWithIf(ParseTree node){
        ParseTree parentNode = node.getParent();
        ParseTree brother6 = parentNode.getChild(5);
        if (node != parentNode.getChild(6)){
            return false;
        }
        if (brother6 instanceof TerminalNode) {
            if (((TerminalNode) brother6).getSymbol().getType() == SysYLexer.ELSE) {
                ParseTree childNode = node.getChild(0);
                if (childNode instanceof TerminalNode) {
                    if (((TerminalNode) childNode).getSymbol().getType() == SysYLexer.IF) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

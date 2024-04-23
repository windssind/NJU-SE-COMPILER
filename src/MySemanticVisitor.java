import java.util.*;

public class MySemanticVisitor extends SysYParserBaseVisitor<Void> {
    // 压栈的时机要搞清楚，应该是执行新的函数调用的时候才将curSymbolTable压栈，并且要新建一个栈作为curSymbolTable
    // 只有visitBlock的时候才会压栈
    private Stack<SymbolTable> symbolTableStack;
    private SymbolTable bottomTable; // 最底部的符号表，记录了各种全局变量以及函数的声明

    private SymbolTable currentTable;

    public ErrorReporter errorReporter;

    private Type curFuncType;

    private SymbolTable tmpSymbolTable; // 用于解决传输函数参数的临时符号

    MySemanticVisitor() {
        bottomTable = new SymbolTable();
        currentTable = bottomTable;
        errorReporter = new ErrorReporter();
        // 先要压存储全局变量以及函数的symbol
        symbolTableStack = new Stack<SymbolTable>();
        symbolTableStack.push(bottomTable);
        // 用于传递函数参数
        tmpSymbolTable = new SymbolTable();
    }

    public Void visitFuncDef(SysYParser.FuncDefContext ctx) {
        // 要维持一个从左到右的关系
        Type retType;
        String typeStr = ctx.funcType().getChild(0).getText();
        if (typeStr.equals("int")) {
            curFuncType = retType = new IntType();
        } else { //VOID类型
            curFuncType = retType = new VoidType();
        }
        visit(ctx.funcType());

        String funcName = ctx.funcName().IDENT().getText();
        if (currentTable.GetSymbol(funcName) != null) { // curScope为当前的作用域
            errorReporter.report(ErrorReporter.ErrorType.RedefinedFunc, ctx.getStart().getLine(), funcName);
            return null;
            // 直接跳过，后面的block也不需要再检测了
        }
        //初始化参数列表
        ArrayList<Type> paramsTyList = new ArrayList<>();


        if (ctx.funcFParams() != null) {
            InitializeFParamsList(ctx.funcFParams(), paramsTyList);
            visit(ctx.funcFParams());
        }

        // 给符号表添加一个函数信息
        currentTable.AddSymbol(funcName, new FunctionType(retType, paramsTyList), true);

        visit(ctx.block());
        return null;
    }

    @Override
    public Void visitBlock(SysYParser.BlockContext ctx) {
        // 如果父亲是一个函数就需要创建新的符号表,否则就是把父亲的表给复制下来
        // block都需要把父亲的符号表复制下来
        SymbolTable newTable = currentTable.getCopy();
        symbolTableStack.push(currentTable);
        currentTable = newTable;
        if (ctx.getParent() instanceof SysYParser.FuncDefContext) {
            tmpSymbolTable.GetSymbols().forEach(symbol -> {
                currentTable.AddSymbol(symbol.name, symbol.type, symbol.isGlobal);
            });
            tmpSymbolTable.ClearSymbols();
        }
        ctx.blockItem().forEach(this::visit); // 依次visit block中的节点
        // 再重新更换当前符号表
        currentTable = symbolTableStack.peek();
        symbolTableStack.pop();
        return null;
    }

    @Override
    public Void visitConstDef(SysYParser.ConstDefContext ctx) {
        String constStr = ctx.IDENT().getText();
        boolean isGlobal = isGlobalDecl((SysYParser.DeclContext) ctx.getParent().getParent());
        if (currentTable.HasSymbol(constStr)) {
            // 如果我是一个全局变量，就报错
            if (isGlobal) {
                errorReporter.report(ErrorReporter.ErrorType.RedefinedFunc, ctx.getStart().getLine(), constStr);
                return null;
            } else {
                // 如果我是一个局部变量，看看表里面的是一个全局还是局部，全局就覆盖，局部就报错
                if (currentTable.GetSymbol(constStr).isGlobal()) { // 转为局部变量
                    currentTable.ToLocal(constStr);
                } else {
                    errorReporter.report(ErrorReporter.ErrorType.RedefinedFunc, ctx.getStart().getLine(), constStr);
                    return null;
                }
            }
        }
        visit(ctx.IDENT());

        if (!ctx.constExp().isEmpty()) { // 如果是数组
            // 1. 获取数组的维度
            int dim = ctx.constExp().size();
            Type elementType = null;
            Type curInnerType = new IntType();
            for (int i = 0; i < dim; i++) {
                elementType = new ArrayType(curInnerType, 0);
                curInnerType = elementType;
            }
            currentTable.AddSymbol(constStr, elementType, isGlobal);
        } else {
            currentTable.AddSymbol(constStr, new IntType(), isGlobal);
        }
        visitConstInitVal(ctx.constInitVal());
        return null;
    }

    @Override
    public Void visitVarDef(SysYParser.VarDefContext ctx) {
        String varStr = ctx.IDENT().getText();
        boolean isGlobal = isGlobalDecl((SysYParser.DeclContext) ctx.getParent().getParent());
        if (currentTable.HasSymbol(varStr)) {
            // 如果我是一个全局变量，就报错
            if (isGlobal) {
                errorReporter.report(ErrorReporter.ErrorType.RedefinedVar, ctx.getStart().getLine(), varStr);
                return null;
            } else { // 重点，涉及到覆盖
                // 如果我是一个局部变量，就删除掉这个变量的声明，重新加上去
                if (currentTable.GetSymbol(varStr).isGlobal()) {
                    currentTable.DeleteSymbol(varStr);
                } else {
                    errorReporter.report(ErrorReporter.ErrorType.RedefinedVar, ctx.getStart().getLine(), varStr);
                    ;
                }
            }
        }
        visit(ctx.IDENT());

        Type lvalType;
        Type expType;
        if (!ctx.constExp().isEmpty()) { // 如果是数组
            // 1. 获取数组的维度
            int dim = ctx.constExp().size();
            Type elementType = null;
            Type curInnerType = new IntType();
            for (int i = 0; i < dim; i++) {
                elementType = new ArrayType(curInnerType, 0);
                curInnerType = elementType;
            }
            currentTable.AddSymbol(varStr, elementType, isGlobal);
            lvalType = elementType;
        } else {
            currentTable.AddSymbol(varStr, new IntType(), isGlobal);
            lvalType = new IntType();
        }

        if (ctx.initVal() != null) {
            visitInitVal(ctx.initVal());
            if (ctx.initVal().exp() != null) {
                expType = getTypeOfExp(ctx.initVal().exp());
                if (lvalType == null || expType == null) {
                    errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchedForAssignment, ctx.getStart().getLine(), ctx.getText());
                    return null;
                }
                if (!isValAssignLegal(lvalType, expType)) {
                    if (lvalType.getType().equals("function")) {
                        errorReporter.report(ErrorReporter.ErrorType.LeftHandSideAFunc, ctx.getStart().getLine(), ctx.getText());
                    } else {
                        errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchedForAssignment, ctx.getStart().getLine(), ctx.getText());
                    }
                }
            }

        }


        return null;
    }

    @Override
    public Void visitLVal(SysYParser.LValContext ctx) {
        SymbolTable.Symbol lvalSymbol = currentTable.GetSymbol(ctx.IDENT().getText());
        if (lvalSymbol == null) {
            errorReporter.report(ErrorReporter.ErrorType.UndefinedVar, ctx.getStart().getLine(), ctx.IDENT().getText());
            return null;
        }
        if (lvalSymbol.type.getType().equals("int")) { // int带有[]
            if (!ctx.L_BRACKT().isEmpty()) {
                errorReporter.report(ErrorReporter.ErrorType.NotAnArray, ctx.getStart().getLine(), ctx.IDENT().getText());
                return null;
            }
        } else if (lvalSymbol.type.getType().equals("array")) { // array没有带[]
            int dim = getDimOfArray((ArrayType) (lvalSymbol.getType()));
            int BracketSize = ctx.L_BRACKT().size();
            if (dim < BracketSize) {
                errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchForOp, ctx.getStart().getLine(), ctx.IDENT().getText());
                return null;
            }
        }else{
            // 这里是函数
            // 很怪，这里加了反而会扣分
            errorReporter.report(ErrorReporter.ErrorType.NotAnArray, ctx.getStart().getLine(), ctx.IDENT().getText());
        }
        return null;
    }

    @Override
    public Void visitExp(SysYParser.ExpContext ctx) {
        // 1.函数的调用是否合法
        if (ctx.funcName() != null) {
            visit(ctx.funcName()); // 这里会检查funcName是否是一个函数
            // 只有符号表中有这个函数才会继续接下来
            if (currentTable.GetSymbol(ctx.funcName().IDENT().getText()) != null) {
                if (currentTable.GetSymbol(ctx.funcName().IDENT().getText()).type.getType().equals("function")) { //简单判断一下这个结点是否是funcType，如果是就继续下一步的参数检测
                    // 这里确保了这是一个函数，然后执行接下来的参数检测
                    int paramSize = ctx.funcRParams() != null ? ctx.funcRParams().param().size() : 0;
                    int needParamSize = ((FunctionType) currentTable.GetSymbol(ctx.funcName().IDENT().getText()).type).getParamsType().size();
                    if (paramSize != needParamSize) {
                        // 传的参数不等
                        errorReporter.report(ErrorReporter.ErrorType.FuncParamFalse, ctx.getStart().getLine(), ctx.funcName().IDENT().getText());
                        return null;
                    }
                    // 传的参数个数相同，看一下参数类型能否匹配
                    for (int i = 0; i < paramSize; i++) {
                        visit(ctx.funcRParams().param(i));
                        if (currentTable.GetSymbol(ctx.funcName().IDENT().getText() )== null){
                            errorReporter.report(ErrorReporter.ErrorType.UndefinedFunc, ctx.funcName().getStart().getLine(), ctx.funcName().IDENT().getText());
                            return null;
                        }
                        if (!currentTable.GetSymbol(ctx.funcName().IDENT().getText()).getType().getType().equals("function") ){
                            errorReporter.report(ErrorReporter.ErrorType.NotAFunction, ctx.funcName().getStart().getLine(), ctx.funcName().IDENT().getText());
                            return null;
                        }
                        Type needFuncFParam = ((FunctionType)currentTable.GetSymbol(ctx.funcName().IDENT().getText()).getType()).getParamsType().get(i);
                        Type funcRParam = getTypeOfExp(ctx.funcRParams().param(i).exp());
                        if (funcRParam == null){
                            // 说明exp是一个有问题的东西
                            return null;
                        }
                        if (!needFuncFParam.getType().equals(funcRParam.getType())){
                            errorReporter.report(ErrorReporter.ErrorType.FuncParamFalse, ctx.funcRParams().param(i).getStart().getLine(), ctx.funcName().IDENT().getText());
                            return null;
                        }
                    }
                }
            }
        } else if (!ctx.exp().isEmpty()) {// 2. 判断exp两侧的类型是否相同
            // 这里有问题
            ctx.exp().forEach(this::visit);

            if (ctx.unaryOp() != null) {
                if (getTypeOfExp(ctx.exp().get(0)) != null && getTypeOfExp(ctx.exp().get(0)).getType().equals("int")) {
                    errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchForOp, ctx.getStart().getLine(), ctx.getText());
                    return null;
                }
            } else { // 需要两个exp都是int
                if ((getTypeOfExp(ctx.exp().get(0)) != null && !getTypeOfExp(ctx.exp().get(0)).getType().equals("int")) || (getTypeOfExp(ctx.exp().get(1)) != null && !getTypeOfExp(ctx.exp().get(1)).getType().equals("int"))) {
                    errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchForOp, ctx.getStart().getLine(), ctx.getText());
                    return null;
                }
            }
        } else if (ctx.lVal() != null) {
            visit(ctx.lVal());
        }
        return null;
    }

    /*
    这个是特定用于函数调用时候的funcName，声明时不会使用
     */
    @Override
    public Void visitFuncName(SysYParser.FuncNameContext ctx) {
        if (!currentTable.HasSymbol(ctx.IDENT().getText())) {
            errorReporter.report(ErrorReporter.ErrorType.UndefinedFunc, ctx.getStart().getLine(), ctx.IDENT().getText());
            return null;
        } else {
            // 有表，但是不是函数，是一个变量或者数组
            if (!currentTable.GetSymbol(ctx.IDENT().getText()).type.getType().equals("function")) {
                errorReporter.report(ErrorReporter.ErrorType.NotAFunction, ctx.getStart().getLine(), ctx.IDENT().getText());
                return null;
            }
        }
        return null;
    }

    @Override
    public Void visitStmt(SysYParser.StmtContext ctx) {
        if (ctx.lVal() != null) {
            visit(ctx.lVal()); // 这里会检查左值是否在SymbolTable中
            visit(ctx.exp());

            Type lvalType = getTypeOflval(ctx.lVal());
            Type expType = getTypeOfExp(ctx.exp());

            //如果lvalType或者expType为null，说明lval或者exp没有在SymbolTable中或者获取返回值报错了，直接返回，不需要进行下一步(但这里的其实是冗余了)
            if (lvalType == null || expType == null)
                return null;
            if (!isValAssignLegal(lvalType, expType)) {
                if (lvalType.getType().equals("function")) {
                    errorReporter.report(ErrorReporter.ErrorType.LeftHandSideAFunc, ctx.getStart().getLine(), ctx.getText());
                } else {
                    errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchedForAssignment, ctx.getStart().getLine(), ctx.getText());
                }
            }
            return null;
        }
        if (ctx.block() != null) {
            visit(ctx.block());
            return null;
        }

        if (ctx.RETURN() != null) {
            if (ctx.exp() != null)
                visit(ctx.exp());
            if (ctx.exp() == null) {
                // Return后面不带任何东西,看看函数的返回值是不是也是void
                if (!curFuncType.getType().equals("void")) {
                    errorReporter.report(ErrorReporter.ErrorType.ReturnTypeFalse, ctx.getStart().getLine(), ctx.getText());
                    ;
                }
            } else {
                // Return后面带了东西,如果exp不是int或者函数接受的返回值不是int就报错
                if (!curFuncType.getType().equals("int") || ((getTypeOfExp(ctx.exp()) != null && !getTypeOfExp(ctx.exp()).getType().equals("int")))) {
                    errorReporter.report(ErrorReporter.ErrorType.ReturnTypeFalse, ctx.getStart().getLine(), ctx.getText());
                }
            }

            return null;
        }

        if (ctx.exp() != null)
            visit(ctx.exp());
        if (ctx.block() != null)
            visit(ctx.block());
        if (ctx.cond() != null)
            visit(ctx.cond());
        if (ctx.stmt() != null)
            ctx.stmt().forEach(this::visit);
        return null;
    }

    @Override
    public Void visitFuncFParam(SysYParser.FuncFParamContext ctx) {
        if (ctx.getChildCount() > 2) { // 这个是数组
            int dim = ctx.L_BRACKT().size();
            Type elementType = null;
            Type curInnerType = new IntType();
            for (int i = 0; i < dim; i++) {
                elementType = new ArrayType(curInnerType, 0);
                curInnerType = elementType;
            }
            tmpSymbolTable.AddSymbol(ctx.IDENT().getText(), elementType, false);
        } else {
            tmpSymbolTable.AddSymbol(ctx.IDENT().getText(), new IntType(), false);
        }
        return null;
    }

    @Override
    public Void visitCond(SysYParser.CondContext ctx) {
        if (getTypeOfCond(ctx) == null || !getTypeOfCond(ctx).getType().equals("int")) {
            errorReporter.report(ErrorReporter.ErrorType.TypeMisMatchForOp, ctx.getStart().getLine(), ctx.getText());
            return null;
        }
        return null;
    }

    private void InitializeFParamsList(SysYParser.FuncFParamsContext ctx, ArrayList<Type> paramList) {
        if (ctx == null) {
            return;
        }
        int childCount = ctx.funcFParam().size();
        int FParamCount = 0;
        HashSet<String> paramNames = new HashSet<String>();
        for (int i = 0; i < childCount; ++i) {
            // 如果已经有了同名的参数，就跳过
            if (paramNames.contains(ctx.funcFParam().get(i).IDENT().getText())) {
                errorReporter.report(ErrorReporter.ErrorType.RedefinedVar, ctx.getStart().getLine(), ctx.funcFParam().get(i).IDENT().getText());
                continue;
            } else {
                paramNames.add(ctx.funcFParam().get(i).IDENT().getText());
            }
            // 本次实验为了降低难度，最多只有一维数组
            if (ctx.funcFParam(i).getChildCount() > 2) {
                paramList.add(new ArrayType(new IntType(), 0)); //TODO:后面的elementNum还需要考虑
            } else {
                paramList.add(new IntType());
            }
        }
    }

    private boolean isGlobalDecl(SysYParser.DeclContext ctx) {
        return ctx.getParent() instanceof SysYParser.CompUnitContext;
    }

    // 返回给定的字面类型(也就是你的exp的结构展现出来的你的类型)，还要在出口函数处和Symbol的比较
    private Type getTypeOfExp(SysYParser.ExpContext ctx) {
        if (!ctx.exp().isEmpty()) {
            return getTypeOfExp(ctx.exp().get(0));
        } else if (ctx.lVal() != null) {
            return getTypeOflval(ctx.lVal());
        } else if (ctx.number() != null) {
            return new IntType();
        } else { // 这里是函数调用
            if ((currentTable.GetSymbol(ctx.funcName().IDENT().getText())) == null) {
                return null;
            } else if (currentTable.GetSymbol(ctx.funcName().IDENT().getText()).getType() instanceof FunctionType) {
                return ((FunctionType) currentTable.GetSymbol(ctx.funcName().IDENT().getText()).getType()).getRetTy();
            } else { // 这里是数组
                return null;
            }
        }
    }

    // 如果返回的是一个null，就说明获取TYPE失效（也就是该lval不是一个合法的type）
    // 其实这个是有问题的，应该是通过结构获取type而不是表
    private Type getTypeOflval(SysYParser.LValContext ctx) {
        SymbolTable.Symbol symbol = currentTable.GetSymbol(ctx.IDENT().getText());
        if (symbol != null) {
            Type curType = symbol.getType();
            if (symbol.getType().getType().equals("array")) {
                int BracketSize = ctx.L_BRACKT().size();
                for (int i = 0; i < BracketSize; i++) {
                    if (curType instanceof ArrayType) {
                        curType = ((ArrayType) curType).getElementType();
                    } else {
                        return null; // 这里就是已经报错了
                    }
                }
            } else {
                if (!ctx.L_BRACKT().isEmpty()) {
                    return null;
                }
            }
            return curType;
        } else
            return null;

        /*
        if (ctx.exp().isEmpty()){
            // 说明只是一个普通的左值
            return new IntType();
        }else{
            // 这个是数组
            // 先要从符号表中找到，然后根据层数剥离
            if (currentTable.GetSymbol(ctx.IDENT().getText()) != null){
                // 成功找到
                int dim = ctx.exp().size();
                Type currentType = currentTable.GetSymbol(ctx.IDENT().getText()).getType();
                for (int i = 0; i < dim; ++i){
                    if (currentType instanceof ArrayType){
                        currentType = ((ArrayType) currentType).getElementType();
                    }else{
                        return null;
                    }
                }
                return currentType;
            }else{
                // 符号表中没找到，直接返回null
                return null;
            }
        }*/
    }

    private boolean isValAssignLegal(Type lval, Type exp) {
        if (lval.getType().equals("function") || exp.getType().equals("function")) {
            return false;
        } else if (lval.getType().equals("int") && exp.getType().equals("int")) {
            return true;
        } else if (lval.getType().equals("array") && exp.getType().equals("array")) {
            return getDimOfArray((ArrayType) lval) == getDimOfArray((ArrayType) exp);
        } else {
            // 一个int一个array，肯定是wrong
            return false;
        }
    }

    private int getDimOfArray(ArrayType arrayType) {
        int dim = 0;
        Type curType = arrayType;
        while (curType instanceof ArrayType) {
            curType = ((ArrayType) curType).getElementType();
            dim++;
        }
        return dim;
    }

    private Type getTypeOfCond(SysYParser.CondContext ctx) {
        if (ctx.exp() != null) {
            visit(ctx.exp());
            return getTypeOfExp(ctx.exp());
        } else {
            Type cond1Type = getTypeOfCond(ctx.cond().get(0));
            Type cond2Type = getTypeOfCond(ctx.cond().get(1));
            if (cond1Type == null || cond2Type == null) {
                return null;
            }
            if (cond1Type.getType().equals("int") && cond2Type.getType().equals("int")) {
                return new IntType();
            } else {
                return null;
            }
        }
    }


}

import org.antlr.v4.runtime.tree.RuleNode;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLVMVistor extends SysYParserBaseVisitor<LLVMValueRef>{
    //初始化LLVM
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");

    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();

    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();

    //当前的基本块
    LLVMBasicBlockRef currentBlock = null;


    //
    HashMap<String,LLVMValueRef> symbolTable = new HashMap<String,LLVMValueRef>();
    public void Init(){
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        //生成返回值类型
        LLVMTypeRef returnType = i32Type;

        //生成函数参数类型
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);

        //生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, 0,0);

        //生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module, ctx.funcName().getText(), ft);
        currentBlock = LLVMAppendBasicBlock(function, ctx.funcName().getText() + "Entry");
        LLVMPositionBuilderAtEnd(builder, currentBlock);//后续生成的指令将追加在block1的后面
        visit(ctx.block()); // 只需要访问Block就好了
        return function;
    }

    @Override
    public LLVMValueRef visitExp(SysYParser.ExpContext ctx) {
        if (ctx.number() != null){
            if (ctx.number().INTEGER_CONST().getText().contains("0x")||ctx.number().INTEGER_CONST().getText().contains("0X")){
                return LLVMConstInt(i32Type, Integer.parseInt(ctx.number().INTEGER_CONST().getText().substring(2), 16), 0);
            }else if (ctx.number().INTEGER_CONST().getText().startsWith("0")){
                return LLVMConstInt(i32Type, Integer.parseInt(ctx.number().INTEGER_CONST().getText().substring(1), 8), 0);
            }else {
                return LLVMConstInt(i32Type, Integer.parseInt(ctx.number().INTEGER_CONST().getText()), 0);
            }
        }else if (ctx.unaryOp() != null){
            if (ctx.unaryOp().PLUS() != null){
                return visit(ctx.exp(0));
            }else if (ctx.unaryOp().MINUS() != null){
                LLVMValueRef operand = visit(ctx.exp(0));
                return LLVMBuildSub(builder, LLVMConstInt(i32Type, 0, 0), operand, "minus");
            }else {
                LLVMValueRef operand = visit(ctx.exp(0));
                LLVMValueRef _tmp = LLVMBuildICmp(builder, LLVMIntNE, LLVMConstInt(i32Type, 0, 0), operand, "tmp_");
                _tmp = LLVMBuildXor(builder, _tmp, LLVMConstInt(LLVMInt1Type(), 1, 0), "tmp_");
                return _tmp = LLVMBuildZExt(builder, _tmp, i32Type, "tmp_");
            }
        }else if (ctx.exp() != null && ctx.exp().size() == 2){
            LLVMValueRef lhs = visit(ctx.exp(0));
            LLVMValueRef rhs = visit(ctx.exp(1));
            switch (ctx.op.getText()){
                case "+":
                    return LLVMBuildAdd(builder, lhs, rhs, "add");
                case "-":
                    return LLVMBuildSub(builder, lhs, rhs, "sub");
                case "*":
                    return LLVMBuildMul(builder, lhs, rhs, "mul");
                case "/":
                    return LLVMBuildSDiv(builder, lhs, rhs, "div");
                case "%":
                    return LLVMBuildSRem(builder, lhs, rhs, "rem");
                }
        }else if (ctx.exp() != null && ctx.exp().size() == 1){
            return visit(ctx.exp(0));
        }else if (ctx.lVal() != null){
            return LLVMBuildLoad(builder, symbolTable.get(ctx.lVal().IDENT().getText()), ctx.lVal().IDENT().getText());
        }
        return null;
    }

    /*
    @Override
    public LLVMValueRef visitUnaryExp(SysYParser.UnaryExpContext ctx) {
        if (ctx.unaryOp().PLUS() != null){
            return visitExp(ctx.exp());
        }else if (ctx.unaryOp().MINUS() != null){
            LLVMValueRef operand = visitExp(ctx.exp());
            return LLVMBuildSub(builder, LLVMConstInt(i32Type, 0, 0), operand, "minus");
        }else {
            LLVMValueRef operand = visitExp(ctx.exp());
            LLVMValueRef _tmp = LLVMBuildICmp(builder, LLVMIntNE, LLVMConstInt(i32Type, 0, 0), operand, "tmp_");
            _tmp = LLVMBuildXor(builder, _tmp, LLVMConstInt(LLVMInt1Type(), 1, 0), "tmp_");
            return _tmp = LLVMBuildZExt(builder, _tmp, i32Type, "tmp_");
        }
    }*/

    /*
    @Override
    public LLVMValueRef visitReturnStmt(SysYParser.ReturnStmtContext ctx) {
        return LLVMBuildRet(builder, visit(ctx.exp()));
    }
    */
    @Override
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx) {
        if (ctx.ASSIGN() != null){
            return LLVMBuildStore(builder, visit(ctx.exp()), symbolTable.get(ctx.lVal().IDENT().getText()));
        }else if (ctx.RETURN() != null){
            return LLVMBuildRet(builder, visit(ctx.exp()));
        }
        return null;
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        return super.visitBlock(ctx);
    }


    @Override
    public LLVMValueRef visitVarDecl(SysYParser.VarDeclContext ctx) {
        boolean isGlobal = isGlobalDecl(ctx);
        for (int i = 0; i < ctx.varDef().size(); i++){
            if (isGlobal){
                LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type,ctx.varDef().get(i).IDENT().getText());
                symbolTable.put(ctx.varDef().get(i).IDENT().getText(),globalVar);
            }else{
                symbolTable.put(ctx.varDef().get(i).IDENT().getText(),LLVMBuildAlloca(builder, i32Type, ctx.varDef().get(i).IDENT().getText()));
            }
            visit(ctx.varDef(i));
        }
        return null;
    }

    @Override
    public LLVMValueRef visitConstDecl(SysYParser.ConstDeclContext ctx) {
        boolean isGlobal = isGlobalDecl(ctx);
        for (int i = 0; i < ctx.constDef().size(); i++){
            if (isGlobal){
                LLVMValueRef globalVar = LLVMAddGlobal(module, i32Type,ctx.constDef().get(i).IDENT().getText());
                symbolTable.put(ctx.constDef().get(i).IDENT().getText(),globalVar);
            }else{
                symbolTable.put(ctx.constDef().get(i).IDENT().getText(),LLVMBuildAlloca(builder, i32Type, ctx.constDef().get(i).IDENT().getText()));
            }
            visit(ctx.constDef(i));
        }
        return null;
    }

    @Override
    public LLVMValueRef visitVarDef(SysYParser.VarDefContext ctx) {
        boolean isGlobalDef = isGlobalDef(ctx);
        if (ctx.ASSIGN() != null){
            LLVMValueRef rhs = visit(ctx.initVal());
            if (isGlobalDef){
                LLVMSetInitializer(symbolTable.get(ctx.IDENT().getText()), rhs);
            }else{
                LLVMBuildStore(builder, rhs, symbolTable.get(ctx.IDENT().getText()));
            }
            //symbolTable.put(ctx.IDENT().getText(),rhs);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitConstDef(SysYParser.ConstDefContext ctx) {
        boolean isGlobalDef = isGlobalDef(ctx);
        if (ctx.ASSIGN() != null){
            LLVMValueRef rhs = visit(ctx.constInitVal());
            if (isGlobalDef){
                LLVMSetInitializer(symbolTable.get(ctx.IDENT().getText()), rhs);
            }else{
                LLVMBuildStore(builder, rhs, symbolTable.get(ctx.IDENT().getText()));
            }
            //symbolTable.put(ctx.IDENT().getText(),rhs);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitInitVal(SysYParser.InitValContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public LLVMValueRef visitConstInitVal(SysYParser.ConstInitValContext ctx) {
        return visit(ctx.constExp());
    }

    @Override
    public LLVMValueRef visitConstExp(SysYParser.ConstExpContext ctx) {
        return visit(ctx.exp());
    }

    private boolean isGlobalDecl(RuleNode ctx){
        return ctx.getParent().getParent() instanceof SysYParser.CompUnitContext;
    }

    private boolean isGlobalDef(RuleNode ctx){
        return ctx.getParent().getParent().getParent() instanceof SysYParser.CompUnitContext;
    }

}

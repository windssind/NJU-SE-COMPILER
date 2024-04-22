public class ErrorReporter {
    public enum ErrorType {
        UndefinedVar,
        UndefinedFunc,
        RedefinedVar,
        RedefinedFunc,
        NotAFunction,
        FuncParamFalse,
        TypeMisMatchedForAssignment,
        TypeMisMatchForOp,
        ReturnTypeFalse,
        arrayDimFalse,
        unaryOpNotLegal,
        opNumNotLegal,
        NotAnArray,
        LeftHandSideAFunc
    }

    public boolean hasError = false;
    private int curErrorLine ;
    private int curErrorNO;
    public void report(ErrorType type, int lineNO, String name)
    {
        if (lineNO == curErrorLine && type.ordinal() != curErrorNO){
            return;
        }
        switch (type)
        {
            case RedefinedFunc:
                System.err.println("Error type 4 at Line " + lineNO + ": Redefined function: " + name + ".");
                break;
            case RedefinedVar:
                System.err.println("Error type 3 at Line " + lineNO + ": Redefined variable: " + name + ".");
                break;
            case UndefinedVar:
                System.err.println("Error type 1 at Line " + lineNO + ": Undefined variable: " + name + ".");
                break;
            case UndefinedFunc:
                System.err.println("Error type 2 at Line " + lineNO + ": Undefined function: " + name + ".");
                break;
            case NotAFunction:
                System.err.println("Error type 10 at Line " + lineNO + ": Not a function: " + name + ".");
                break;
            case FuncParamFalse:
                System.err.println("Error type 8 at Line " + lineNO  + ": Function is not applicable for arguments.");
                break;
            case TypeMisMatchedForAssignment:
                System.err.println("Error type 5 at Line " + lineNO + ": Type mismatched for assignment.");
                break;
            case ReturnTypeFalse:
                System.err.println("Error type 7 at Line " + lineNO + ": Type mismatched for return.");
                break;
            case NotAnArray:
                System.err.println("Error type 9 at Line " + lineNO + ": Not an array: " + name);
                break;
            case LeftHandSideAFunc:
                System.err.println("Error type 11 at Line " + lineNO + ": The left-hand side of an assignment must be a variable.");
                break;
            case TypeMisMatchForOp:
                System.err.println("Error type 6 at Line " + lineNO + ": Type mismatched for operands.");
                break;
        }
        hasError = true;
        curErrorLine = lineNO;
        curErrorNO = type.ordinal();
    }
}

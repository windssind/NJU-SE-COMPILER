public class ErrorReporter {
    public enum ErrorType {
        RedefinedFunc,
        RedefinedVar,
        UndefinedVar,
        UndefinedFunc,
        NotAFunction,
        FuncParamFalse,
        ValAssignNotLegal,
        TypeMisMatchForOp,
        ReturnTypeFalse,
        arrayDimFalse,
        unaryOpNotLegal,
        opNumNotLegal,
        NotAnArray,
        LeftHandSideAFunc
    }

    public void report(ErrorType type, int lineNO, String name)
    {
        switch (type)
        {
            case RedefinedFunc:
                System.out.println("Error type 4 at Line " + lineNO + " :Redefined function: " + name + ".");
                break;
            case RedefinedVar:
                System.out.println("Error type 3 at Line " + lineNO + " :Redefined variable: " + name + ".");
                break;
            case UndefinedVar:
                System.out.println("Error type 1 at Line " + lineNO + " :Undefined variable: " + name + ".");
                break;
            case UndefinedFunc:
                System.out.println("Error type 2 at Line " + lineNO + ": Undefined function: " + name + ".");
                break;
            case NotAFunction:
                System.out.println("Error type 10 at Line " + lineNO + " :Not a function: " + name + ".");
                break;
            case FuncParamFalse:
                System.out.println("Error type 8 at Line " + lineNO  + " :Function is not applicable for arguments.");
                break;
            case ValAssignNotLegal:
                System.out.println("Error type 5 at Line " + lineNO + ": Type mismatched for assignment.");
                break;
            case ReturnTypeFalse:
                System.out.println("Error type 7 at Line " + lineNO + ": Type mismatched for return.");
                break;
            case arrayDimFalse:
                System.out.println("Error type 9 at Line " + lineNO + "Not an array: " + name);
                break;
            case NotAnArray:
                System.out.println("Error type 9 at Line " + lineNO + " :Not an array: " + name);
                break;
            case LeftHandSideAFunc:
                System.out.println("Error type 11 at Line " + lineNO + ": The left-hand side of an assignment must be a variable.");
                break;
            case TypeMisMatchForOp:
                System.out.println("Error type 6 at Line " + lineNO + ": Type mismatched for operands.");
                break;
        }
    }
}

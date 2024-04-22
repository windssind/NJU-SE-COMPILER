import java.util.ArrayList;

public class FunctionType extends Type {
    Type retTy;
    ArrayList<Type> paramsType;

    public FunctionType(Type retTy, ArrayList<Type> paramsType) {
        this.retTy = retTy;
        this.paramsType = paramsType;
    }

    public FunctionType(Type retTy) {
        this.retTy = retTy;
        paramsType = null;
    }

    // 返回的是一个


    @Override
    public String getType() {
        return "function";
    }

    public ArrayList<Type> getParamsType() {
        return paramsType;
    }

    public Type getRetTy(){
        return retTy;
    }
}
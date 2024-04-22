import java.util.ArrayList;

public class SymbolTable {
    public class Symbol {
        public String name;
        public Type type;
        public boolean isGlobal; // 判断一个变量是否是全局变量，如果是func就肯定为true

        public  boolean isGlobal(){
            return isGlobal;
        }

        public Type getType(){
            return type;
        }
    }

    private ArrayList<Symbol> symbols;

    public SymbolTable()
    {
        symbols = new ArrayList<Symbol>();
    }

    public SymbolTable getCopy()
    {
        SymbolTable copy = new SymbolTable();
        for (Symbol symbol : symbols){
            copy.AddSymbol(symbol.name, symbol.type,symbol.isGlobal);
        }
        return copy;
    }

    public void AddSymbol(String name, Type type,boolean isGlobal)
    {
        Symbol symbol = new Symbol();
        symbol.name = name;
        symbol.type = type;
        symbol.isGlobal = isGlobal;
        symbols.add(symbol);
    }

    public boolean HasSymbol(String name)
    {
        for (Symbol symbol : symbols){
            if (symbol.name.equals(name)){
                return true;
            }
        }
        return false;
    }

    public Symbol GetSymbol(String name)
    {
        for (Symbol symbol : symbols){
            if (symbol.name.equals(name)){
                return symbol;
            }
        }
        return null;
    }

    public void ToLocal(String symbolName){
        for (Symbol symbol : symbols){
            if (symbol.name.equals(symbolName)){
                symbol.isGlobal = false;
                return;
            }
        }
    }

    public void ToIntType(String symbolName){
        for (Symbol symbol : symbols){
            if (symbol.name.equals(symbolName)){
                symbol.type = new IntType();
                return;
            }
        }
    }

    public void DeleteSymbol(String name){
        for (Symbol symbol : symbols){
            if (symbol.name.equals(name)){
                symbols.remove(symbol);
                return;
            }
        }
    }
}

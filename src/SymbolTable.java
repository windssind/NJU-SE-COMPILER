import java.util.ArrayList;

public class SymbolTable {
    public class Symbol {
        public String name;
        public Type type;
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
            copy.AddSymbol(symbol.name, symbol.type);
        }
        return copy;
    }

    public void AddSymbol(String name, Type type)
    {
        Symbol symbol = new Symbol();
        symbol.name = name;
        symbol.type = type;
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
}

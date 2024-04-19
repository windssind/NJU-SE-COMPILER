public class ArrayType extends Type{
    private Type elementType;//Type of elements , maybe Int Or Array
    private int elementNum;

    @Override
    public String getType() {
        return "Array";
    }

    public ArrayType(Type elementType, int elementNum)
    {
        if (elementType.getType() .compareTo("Int") != 0  && elementType.getType() .compareTo("Array") != 0 ){
            throw new RuntimeException("ArrayType: elementType must be Int or Array");
        }
        this.elementType = elementType;
        this.elementNum = elementNum;
    }
}

package ru.regenix.jphp.compiler.jvm.runtime.memory;

import ru.regenix.jphp.compiler.jvm.runtime.type.HashTable;

abstract public class Memory {
    public enum Type {
        NULL, BOOL, INT, DOUBLE, STRING, ARRAY, REFERENCE, INVALID;

        public Class toClass(){
            if (this == DOUBLE)
                return Double.TYPE;
            else if (this == INT)
                return Long.TYPE;
            else if (this == STRING)
                return String.class;
            else if (this == BOOL)
                return Boolean.TYPE;
            else if (this == ARRAY)
                return HashTable.class;
            else if (this == REFERENCE)
                return Memory.class;

            return null;
        }

        public static Type valueOf(Class clazz){
            if (clazz == Long.TYPE)
                return INT;
            if (clazz == Double.TYPE)
                return DOUBLE;
            if (clazz == String.class)
                return STRING;
            if (clazz == Boolean.TYPE)
                return BOOL;
            if (clazz == HashTable.class)
                return ARRAY;

            return REFERENCE;
        }

        public boolean isConstant(){
            return this != REFERENCE/* && this != ARRAY && this != OBJECT*/;
        }
    }

    public final Type type;

    protected Memory(Type type) {
        this.type = type;
    }

    public static final Memory NULL = NullMemory.INSTANCE;
    public static final Memory FALSE = FalseMemory.INSTANCE;
    public static final Memory TRUE = TrueMemory.INSTANCE;

    public static final Memory CONST_INT_0 = new LongMemory(0);
    public static final Memory CONST_INT_1 = new LongMemory(1);
    public static final Memory CONST_INT_2 = new LongMemory(2);
    public static final Memory CONST_INT_3 = new LongMemory(3);
    public static final Memory CONST_INT_4 = new LongMemory(4);
    public static final Memory CONST_INT_5 = new LongMemory(5);

    public static final Memory CONST_DOUBLE_0 = new DoubleMemory(0.0);
    public static final Memory CONST_DOUBLE_1 = new DoubleMemory(1.0);

    public boolean isNull(){
        return type == Type.NULL;
    }

    abstract public long toLong();
    abstract public double toDouble();
    abstract public boolean toBoolean();
    abstract public Memory toNumeric();
    abstract public String toString();

    // <value>[index]
    public Memory valueOfIndex(Memory index) { return new ArrayItemMemory(HashTable.toKey(index)); }
    public Memory valueOfIndex(long index) { return new ArrayItemMemory(index); }
    public Memory valueOfIndex(double index) { return new ArrayItemMemory((long)index); }
    public Memory valueOfIndex(String index) { return new ArrayItemMemory(index); }
    public Memory valueOfIndex(boolean index) { return new ArrayItemMemory(index ? 0L : 1L); }

    // INC DEC
    abstract public Memory inc(Memory memory);
    public Memory inc(long value){ return new LongMemory(toLong() + value); }
    public Memory inc(double value){ return new DoubleMemory(toDouble() + value); }
    public Memory inc(String value){ return inc(StringMemory.toNumeric(value)); }

    public Memory dec(Memory memory){ return inc(memory.negative()); }
    public Memory dec(long value){ return inc(-value); }
    public Memory dec(double value){ return inc(-value); }
    public Memory dec(String value) {  return inc(StringMemory.toNumeric(value).negative()); }

    // NEGATIVE
    abstract public Memory negative();

    // CONCAT
    public String concat(Memory memory){  return toString() + memory.toString(); }
    public String concat(long value) { return toString() + value; }
    public String concat(double value) { return toString() + value; }
    public String concat(boolean value) { return toString() + boolToString(value); }
    public String concat(String value) { return toString() + value; }

    // PLUS
    abstract public Memory plus(Memory memory);
    public Memory plus(long value){ return new LongMemory(toLong() + value); }
    public Memory plus(double value){ return new DoubleMemory(toDouble() + value); }
    public Memory plus(boolean value){ return new LongMemory(toLong() + (value ? 1 : 0)); }
    public Memory plus(String value){ return plus(StringMemory.toNumeric(value)); }

    // MINUS
    abstract public Memory minus(Memory memory);
    public Memory minus(long value){ return new LongMemory(toLong() - value); }
    public Memory minus(double value){ return new DoubleMemory(toDouble() - value); }
    public Memory minus(boolean value){ return new LongMemory(toLong() - (value ? 1 : 0)); }
    public Memory minus(String value){ return minus(StringMemory.toNumeric(value)); }

    // MUL
    abstract public Memory mul(Memory memory);
    public Memory mul(long value){ return new LongMemory(toLong() * value); }
    public Memory mul(double value){ return new DoubleMemory(toDouble() * value); }
    public Memory mul(boolean value){ return new LongMemory(toLong() * (value ? 1 : 0)); }
    public Memory mul(String value){ return mul(StringMemory.toNumeric(value)); }

    // DIV
    abstract public Memory div(Memory memory);
    public Memory div(long value){ if(value==0) return FALSE; return new DoubleMemory(toDouble() / value); }
    public Memory div(double value){ if(value==0.0) return FALSE; return new DoubleMemory(toDouble() / value); }
    public Memory div(boolean value){ if(!value) return FALSE; return new LongMemory(toLong()); }
    public Memory div(String value){ return div(StringMemory.toNumeric(value)); }

    // MOD
    abstract public Memory mod(Memory memory);
    public Memory mod(long value){ if (value==0) return FALSE; return new LongMemory(toLong() % value); }
    public Memory mod(double value){ return mod((long)value); }
    public Memory mod(boolean value){ if (!value) return FALSE; return new LongMemory(toLong() % (value ? 1 : 0)); }
    public Memory mod(String value){ return div(StringMemory.toNumeric(value)); }

    // EQUAL
    abstract public boolean equal(Memory memory);
    public boolean equal(long value){ return toLong() == value; }
    public boolean equal(double value) { return toDouble() == value; }
    public boolean equal(boolean value) { return toBoolean() == value; }
    public boolean equal(String value) { return toString().equals(value); }

    // NOT EQUAL
    abstract public boolean notEqual(Memory memory);
    public boolean notEqual(long value){ return toLong() != value; }
    public boolean notEqual(double value) { return toDouble() != value; }
    public boolean notEqual(boolean value) { return toBoolean() != value; }
    public boolean notEqual(String value) { return !toString().equals(value); }

    // SMALLER
    abstract public boolean smaller(Memory memory);
    public boolean smaller(long value) { return toDouble() < value; }
    public boolean smaller(double value) { return toDouble() < value; }
    public boolean smaller(boolean value) { return toDouble() < (value ? 1 : 0); }
    public boolean smaller(String value) { return this.smaller(StringMemory.toNumeric(value)); }

    // SMALLER EQ
    abstract public boolean smallerEq(Memory memory);
    public boolean smallerEq(long value) { return toDouble() <= value; }
    public boolean smallerEq(double value) { return toDouble() <= value; }
    public boolean smallerEq(boolean value) { return toDouble() <= (value ? 1 : 0); }
    public boolean smallerEq(String value) { return this.smallerEq(StringMemory.toNumeric(value)); }

    // GREATER
    abstract public boolean greater(Memory memory);
    public boolean greater(long value) { return toDouble() > value; }
    public boolean greater(double value) { return toDouble() > value; }
    public boolean greater(boolean value) { return toDouble() > (value ? 1 : 0); }
    public boolean greater(String value) { return this.smaller(StringMemory.toNumeric(value)); }

    // GREATER EQ
    abstract public boolean greaterEq(Memory memory);
    public boolean greaterEq(long value) { return toDouble() >= value; }
    public boolean greaterEq(double value) { return toDouble() >= value; }
    public boolean greaterEq(boolean value) { return toDouble() >= (value ? 1 : 0); }
    public boolean greaterEq(String value) { return this.greaterEq(StringMemory.toNumeric(value)); }

    // ASSIGN
    public Memory assign(Memory memory){ return memory; }
    public Memory assign(long value){ return LongMemory.valueOf(value); }
    public Memory assign(double value) { return new DoubleMemory(value); }
    public Memory assign(boolean value) { return value ? TRUE : FALSE; }
    public Memory assign(String value){ return new StringMemory(value); }

    // ASSIGN REF
    public void assignRef(Memory memory){ }
    public void assignRef(long value){ }
    public void assignRef(double value){ }
    public void assignRef(boolean value){ }
    public void assignRef(String value){ }

    public void unset(){  }

    public Memory toImmutable(){
        return this;
    }

    public boolean isImmutable(){
        return true;
    }

    public void concatAssign(Memory memory){}
    public void concatAssign(String value){}
    public void concatAssign(long value){}
    public void concatAssign(double value){}
    public void concatAssign(boolean value){}

    /********** RIGHT ******************/
    public Memory minusRight(long value){ return new LongMemory(value - toLong()); }
    public Memory minusRight(double value){ return new DoubleMemory(value - toDouble()); }
    public Memory minusRight(boolean value){ return new LongMemory((value ? 1 : 0) - toLong()); }
    public Memory minusRight(String value){ return StringMemory.toNumeric(value).minus(this); }

    public Memory divRight(long value){ return new DoubleMemory(value / toDouble()); }
    public Memory divRight(double value){ return new DoubleMemory(value / toDouble()); }
    public Memory divRight(boolean value){ if(!value) return CONST_INT_0; return new DoubleMemory((value ? 1 : 0) / toDouble()); }
    public Memory divRight(String value){ return StringMemory.toNumeric(value).div(this); }

    public Memory modRight(long value){ return new LongMemory(value % toLong()); }
    public Memory modRight(double value){ return new DoubleMemory(value % toDouble()); }
    public Memory modRight(boolean value){ return new LongMemory((value ? 1 : 0) % toLong()); }
    public Memory modRight(String value){ return StringMemory.toNumeric(value).mod(this); }

    public String concatRight(long value) { return value + toString(); }
    public String concatRight(double value) { return value + toString(); }
    public String concatRight(boolean value) { return boolToString(value) + toString(); }
    public String concatRight(String value) { return value + toString(); }

    /****************************************************************/
    /** Static *****/

    public static Memory minusRight(long value, Memory memory){ return memory.minusRight(value); }
    public static Memory minusRight(double value, Memory memory){ return memory.minusRight(value); }
    public static Memory minusRight(boolean value, Memory memory){ return memory.minusRight(value); }
    public static Memory minusRight(String value, Memory memory){ return memory.minusRight(value); }

    public static Memory divRight(long value, Memory memory){ return memory.divRight(value); }
    public static Memory divRight(double value, Memory memory){ return memory.divRight(value); }
    public static Memory divRight(boolean value, Memory memory){ return memory.divRight(value); }
    public static Memory divRight(String value, Memory memory){ return memory.divRight(value); }

    public static Memory modRight(long value, Memory memory){ return memory.modRight(value); }
    public static Memory modRight(double value, Memory memory){ return memory.modRight(value); }
    public static Memory modRight(boolean value, Memory memory){ return memory.modRight(value); }
    public static Memory modRight(String value, Memory memory){ return memory.modRight(value); }

    public static String concatRight(long value, Memory memory){ return memory.concatRight(value); }
    public static String concatRight(double value, Memory memory){ return memory.concatRight(value); }
    public static String concatRight(boolean value, Memory memory){ return memory.concatRight(value); }
    public static String concatRight(String value, Memory memory){ return memory.concatRight(value); }


    public static void assignRight(Memory value, Memory memory){ memory.assign(value); }
    public static void assignRight(long value, Memory memory){ memory.assign(value); }
    public static void assignRight(double value, Memory memory){ memory.assign(value); }
    public static void assignRight(boolean value, Memory memory){ memory.assign(value); }
    public static void assignRight(String value, Memory memory){ memory.assign(value); }
    ////

    public static String boolToString(boolean value){
        return value ? "1" : "";
    }

    public static Memory toArrayValue(Memory value){
        if (value instanceof ArrayItemMemory){
            return ((ArrayItemMemory)value).table;
        } else {
            return value;
        }
    }
}
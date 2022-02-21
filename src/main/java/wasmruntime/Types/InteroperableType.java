package wasmruntime.Types;

import java.util.HashMap;
import java.util.Map;

public enum InteroperableType {
    Bool(1),
    U8(2),
    I8(3),
    U16(4),
    I16(5),
    U32(6),
    I32(7),
    U64(8),
    I64(9),
    F32(10),
    F64(11),
    String(12);

    private final int type;

    InteroperableType(int type) {
        this.type = type;
    }

    public static InteroperableType fromId(byte type) {
        switch (type) {
            case 1:
                return Bool;
            case 2:
                return U8;
            case 3:
                return I8;
            case 4:
                return U16;
            case 5:
                return I16;
            case 6:
                return U32;
            case 7:
                return I32;
            case 8:
                return U64;
            case 9:
                return I64;
            case 10:
                return F32;
            case 11:
                return F64;
            case 12:
                return String;
            default:
                throw new RuntimeException("Unknown type ID");
        }
    }

    public static Map<Byte, InteroperableType> idMap = new HashMap<>();

    static {
        idMap.put((byte) 1, Bool);
        idMap.put((byte) 2, U8);
        idMap.put((byte) 3, I8);
        idMap.put((byte) 4, U16);
        idMap.put((byte) 5, I16);
        idMap.put((byte) 6, U32);
        idMap.put((byte) 7, I32);
        idMap.put((byte) 8, U64);
        idMap.put((byte) 9, I64);
        idMap.put((byte) 10, F32);
        idMap.put((byte) 11, F64);
        idMap.put((byte) 12, String);
    }

    public int getNum() {
        return type;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
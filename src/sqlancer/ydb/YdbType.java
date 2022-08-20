package sqlancer.ydb;

import com.yandex.ydb.table.values.*;
import sqlancer.Randomly;

import java.util.*;

public class YdbType {
    private final Type type;

    public static Map<Type.Kind, List<Type>> types;
    public static List<Type.Kind> typesKinds;

    public static Map<Type.Kind, List<Type>> typesSupportedInColumns;
    public static List<Type.Kind> typesSupportedInColumnsKinds;

    public static Map<Type.Kind, List<Type>> typesSupportedAsPrimary;
    public static List<Type.Kind> typesSupportedAsPrimaryKinds;

    static {
        types = new HashMap<>();
        typesKinds = new ArrayList<>();

        typesSupportedInColumns = new HashMap<>();
        typesSupportedInColumnsKinds = new ArrayList<>();

        typesSupportedAsPrimary = new HashMap<>();
        typesSupportedAsPrimaryKinds = new ArrayList<>();
    }

    static {
        typesKinds.add(Type.Kind.PRIMITIVE);
        types.put(Type.Kind.PRIMITIVE, Arrays.asList(
                PrimitiveType.uint8(),
                PrimitiveType.uint16(),
                PrimitiveType.uint32(),
                PrimitiveType.uint64(),
                PrimitiveType.int8(),
                PrimitiveType.int16(),
                PrimitiveType.int32(),
                PrimitiveType.int64(),
                PrimitiveType.float32(),
                PrimitiveType.float64(),
                PrimitiveType.bool()
        ));
    }

    static {
        typesSupportedInColumnsKinds.add(Type.Kind.PRIMITIVE);
        typesSupportedInColumns.put(Type.Kind.PRIMITIVE, Arrays.asList(
                PrimitiveType.uint8(),
                PrimitiveType.uint32(),
                PrimitiveType.uint64(),
                PrimitiveType.int32(),
                PrimitiveType.int64(),
                PrimitiveType.float32(),
                PrimitiveType.float64(),
                PrimitiveType.bool()
        ));
    }

    static {
        typesSupportedAsPrimaryKinds.add(Type.Kind.PRIMITIVE);
        typesSupportedAsPrimary.put(Type.Kind.PRIMITIVE, Arrays.asList(
                PrimitiveType.uint8(),
                PrimitiveType.uint32(),
                PrimitiveType.uint64(),
                PrimitiveType.int32(),
                PrimitiveType.int64(),
                PrimitiveType.bool()
        ));
    }
    YdbType(Type type) {
        this.type = type;
    }

    public Type getYdbType() {
        return this.type;
    }

    public static YdbType getRandom() {
        Type.Kind kind = Randomly.fromList(typesKinds);
        return new YdbType(Randomly.fromList(types.get(kind)));
    }

    public static YdbType getRandomColumnType(boolean primary) {
        if (primary) {
            Type.Kind kind = Randomly.fromList(typesSupportedAsPrimaryKinds);
            return new YdbType(Randomly.fromList(typesSupportedAsPrimary.get(kind)));
        } else {
            Type.Kind kind = Randomly.fromList(typesSupportedInColumnsKinds);
            return new YdbType(Randomly.fromList(typesSupportedInColumns.get(kind)));
        }
    }

    public static boolean canBePrimary(Type t) {
        return typesSupportedAsPrimary.get(t.getKind()).contains(t);
    }

}
package transfer.deserializer;

import transfer.Inputable;
import transfer.core.EnumInfo;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.exception.IllegalClassTypeException;
import transfer.exception.IllegalTypeException;
import transfer.exception.UnsupportDeserializerTypeException;
import transfer.utils.BitUtils;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 带标签的枚举解析器
 * Created by Jake on 2015/2/25.
 */
public class TagEnumDeserializer implements Deserializer {


    /**
     * 枚举名解析器
     */
    private static final ShortStringDeserializer STRING_DESERIALIZER = ShortStringDeserializer.getInstance();


    @Override
    public <T> T deserialze(Inputable inputable, Type type, byte flag, IntegerMap referenceMap) {

        byte typeFlag = PersistConfig.getType(flag);

        if (typeFlag != Types.ENUM) {
            throw new IllegalTypeException(typeFlag, Types.ENUM, type);
        }

        // 读取枚举类型
        int enumType = BitUtils.getInt2(inputable);

        Class<?> rawClass;

        if (type == null || type == Object.class || type == Enum.class) {

            rawClass = PersistConfig.getClass(enumType);

        } else {

            rawClass = TypeUtils.getRawClass(type);
        }

        if (rawClass == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        EnumInfo enumInfo = (EnumInfo) PersistConfig.getOrCreateClassInfo(rawClass);

        if (enumInfo == null) {
            throw new UnsupportDeserializerTypeException(rawClass);
        }

        if (enumType != enumInfo.getClassId()) {
            throw new IllegalClassTypeException(enumType, type);
        }

        // 读取枚举名
        String enumName = STRING_DESERIALIZER.deserialze(inputable, String.class, inputable.getByte(), referenceMap);

        return (T) enumInfo.toEnum(enumName);// 不存在的枚举则返回null
    }


    private static TagEnumDeserializer instance = new TagEnumDeserializer();

    public static TagEnumDeserializer getInstance() {
        return instance;
    }

}
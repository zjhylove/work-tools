package com.zjhy.love.worktools.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.lang.reflect.ActualTypeMapperPool;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author zhengjun
 */
public class MockUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockUtil.class);
    /**
     * 对于集合数据结构默认生成10条数据
     */
    private static final int COLLECTION_SIZE = 2;

    private MockUtil() {
    }

    /**
     * 模拟方法响应对象
     *
     * @param returnType 响应类型
     * @return 响应对象
     */
    public static Object mockObject(Type returnType) {
        Map<Type, Type> genericMap = ActualTypeMapperPool.get(returnType);
        return mockInstance(returnType, genericMap, 0);
    }

    /**
     * 模拟方法响应对象
     *
     * @param typeReference Type类型参考
     * @param <T>           需要自定义的参考类型
     * @return 模拟对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T mockObject(TypeReference<T> typeReference) {
        return (T) mockObject(typeReference.getType());
    }

    /**
     * 模拟方法响应对象
     *
     * @param mockClass 类型
     * @param <T>       需要自定义的参考类型
     * @return 模拟对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T mockObject(Class<T> mockClass) {
        return (T) mockObject((Type) mockClass);
    }

    /**
     * 构建模拟实例
     * 模拟类型支持 基本类型以及他们的包装类型、常见时间对象、List Set 集合、数组、以及可以无参构造器构建的对象
     * 但是不包括 Map集合与枚举类型
     *
     * @param returnType         模拟类型
     * @param genericMap         模拟类型泛型集合
     * @param defaultNumberValue 数字类型对象默认取值
     * @return 模拟实例
     */
    public static Object mockInstance(Type returnType, Map<Type, Type> genericMap, Object defaultNumberValue) {
        Type newType = genericMap.get(returnType);
        if (Objects.nonNull(newType)) {
            returnType = newType;
            genericMap.putAll(ActualTypeMapperPool.get(returnType));
        }
        Class<?> returnClazz = TypeUtil.getClass(returnType);
        //boolean、char、byte、short、int、long、float、double、number
        if (ClassUtil.isBasicType(returnClazz) || Number.class.isAssignableFrom(returnClazz)) {
            return Convert.convert(returnClazz, defaultNumberValue);
        }
        //Date、LocalDate、LocalTime、LocalDateTime
        List<Class<?>> timeClasses = CollUtil.newArrayList(Date.class, LocalDate.class, LocalTime.class, LocalDateTime.class);
        if (timeClasses.contains(returnClazz)) {
            return Convert.convert(returnClazz, System.currentTimeMillis());
        }
        // List
        if (List.class.isAssignableFrom(returnClazz)) {
            ArrayList<Object> arrayList = new ArrayList<>();
            ParameterizedType type = (ParameterizedType) returnType;
            for (int i = 0; i < COLLECTION_SIZE; i++) {
                arrayList.add(mockInstance(type.getActualTypeArguments()[0], genericMap, i));
            }
            return arrayList;
        }
        //Set
        if (Set.class.isAssignableFrom(returnClazz)) {
            HashSet<Object> hashSet = new HashSet<>();
            ParameterizedType type = (ParameterizedType) returnType;
            for (int i = 0; i < COLLECTION_SIZE; i++) {
                hashSet.add(mockInstance(type.getActualTypeArguments()[0], genericMap, i));
            }
            return hashSet;
        }
        // 数组转换
        if (returnClazz.isArray()) {
            Class<?> componentType = returnClazz.getComponentType();
            Object o = Array.newInstance(componentType, 10);
            for (int i = 0; i < COLLECTION_SIZE; i++) {
                Array.set(o, 0, mockInstance(componentType, genericMap, i));
            }
            return o;
        }
        //String 针对list 与set 集合 返回String 模拟
        if (String.class.isAssignableFrom(returnClazz)) {
            return "String" + defaultNumberValue;
        }
        //HashMap、Enum
        if (returnClazz.isEnum() || HashMap.class.isAssignableFrom(returnClazz)) {
            throw new IllegalArgumentException("请修改你的接口对象[" + returnClazz.getName() + "]hrms 规范不允许再接口参数交互中使用枚举与map 进行参数传递");
        }
        //其它引用类型
        try {
            Object instance = returnClazz.newInstance();
            for (Field field : ReflectUtil.getFields(returnClazz)) {
                //ignore final filed
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (field.getType() == String.class) {
                    ReflectUtil.setFieldValue(instance, field, "test" + defaultNumberValue + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                } else {
                    ReflectUtil.setFieldValue(instance, field, mockInstance(field.getGenericType(), genericMap, 0));
                }
            }
            return instance;
        } catch (Exception e) {
            LOGGER.error("生成模拟数据失败", e);
            return null;
        }
    }
}

package com.zjhy.love.worktools.service;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.lang.reflect.ActualTypeMapperPool;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.*;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;
import com.zjhy.love.worktools.common.util.MockUtil;
import com.zjhy.love.worktools.model.ApiDocConfig;
import com.zjhy.love.worktools.model.ApiField;
import com.zjhy.love.worktools.model.ApiInfo;
import com.zjhy.love.worktools.model.NodeInfo;
import org.apache.commons.collections4.SetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ApiDocService {

    private static final Logger LOGGER = LogManager.getLogger(ApiDocService.class);

    public List<ApiInfo> generateApiDoc(ApiDocConfig config) throws Exception {
        //解压jar包
        File unzip = ZipUtil.unzip(config.getSourceJarPath());
        String rootDir = unzip.getAbsolutePath();

        //加载jar包中classes 文件
        JarClassLoader jarClassLoader = ClassLoaderUtil.getJarClassLoader(new File(rootDir + "/BOOT-INF/classes"));

        //加载解析当前jar包接口所需的依赖
        List<File> requiredJarList = FileUtil.loopFiles(rootDir + "/BOOT-INF/lib",
                pathname -> config.getDependencyJars().stream().anyMatch(t -> pathname.getName().startsWith(t)));
        requiredJarList.forEach(jarClassLoader::addJar);

        //加载需要解析的注解
        Class requestMappingClazz = jarClassLoader.loadClass("org.springframework.web.bind.annotation.RequestMapping");
        Class postMappingClazz = jarClassLoader.loadClass("org.springframework.web.bind.annotation.PostMapping");

        Class apiModelPropertyClazz = jarClassLoader.loadClass("io.swagger.annotations.ApiModelProperty");
        Class apiOperationClazz = jarClassLoader.loadClass("io.swagger.annotations.ApiOperation");

        //循环解析
        Map<String, List<String>> classPathMapping = config.getClassPathMapping();
        return classPathMapping.entrySet().stream().flatMap(c -> {
            Class<?> clazz;
            try {
                clazz = jarClassLoader.loadClass(c.getKey());
            } catch (ClassNotFoundException e) {
                return Stream.empty();
            }
            Method[] publicMethods = ReflectUtil.getPublicMethods(clazz);
            if (Objects.isNull(publicMethods)) {
                return Stream.empty();
            }
            return Arrays.stream(publicMethods).filter(m -> {
                Object annotationValue = AnnotationUtil.getAnnotationValue(m, postMappingClazz);
                if (Objects.isNull(annotationValue)) {
                    return false;
                }
                if (Objects.isNull(c.getValue())) {
                    return true;
                }
                String[] value = (String[]) annotationValue;
                HashSet<String> pSet = SetUtils.hashSet(value);
                return c.getValue().stream().map(p -> {
                    String prefix = StrPool.SLASH;
                    if (!p.startsWith(prefix)) {
                        return prefix + p.trim();
                    }
                    return p.trim();
                }).anyMatch(pSet::contains);
            }).map(m -> {
                ApiInfo apiInfo = new ApiInfo();
                //解析io.swagger.annotations.ApiOperation 注解 values 属性
                Object annotationValue = AnnotationUtil.getAnnotationValue(m, apiOperationClazz);
                if (Objects.nonNull(annotationValue)) {
                    apiInfo.setApiName(annotationValue.toString());
                } else {
                    apiInfo.setApiName("");
                }
                apiInfo.setD(config.getServiceName());

                //解析org.springframework.web.bind.annotation.RequestMapping 注解value 属性
                annotationValue = AnnotationUtil.getAnnotationValue(m.getDeclaringClass(), requestMappingClazz);
                if (Objects.nonNull(annotationValue)) {
                    // 示例值 /sportGameRank/v1.0/
                    String value = ((String[]) annotationValue)[0];
                    if (value.endsWith(StrPool.SLASH)) {
                        value = value.substring(0, value.lastIndexOf(StrPool.SLASH));
                    }
                    int index = value.lastIndexOf(StrPool.SLASH);
                    String cValue = value.substring(0, index);
                    cValue = CharSequenceUtil.replaceFirst(cValue, StrPool.SLASH, CharSequenceUtil.EMPTY);
                    cValue = CharSequenceUtil.replaceLast(cValue, StrPool.SLASH, CharSequenceUtil.EMPTY);
                    apiInfo.setC(cValue);
                    apiInfo.setV(CharSequenceUtil.replaceFirst(value.substring(index), StrPool.SLASH, CharSequenceUtil.EMPTY));
                } else {
                    apiInfo.setC("");
                    apiInfo.setV("");
                }

                //解析org.springframework.web.bind.annotation.PostMapping 注解value 属性
                annotationValue = AnnotationUtil.getAnnotationValue(m, postMappingClazz);
                if (Objects.nonNull(annotationValue)) {
                    // 示例值 /queryRankChange
                    String value = ((String[]) annotationValue)[0];
                    String mValue = CharSequenceUtil.replaceFirst(value, StrPool.SLASH, "");
                    mValue = CharSequenceUtil.replaceLast(mValue, StrPool.SLASH, "");
                    apiInfo.setM(mValue);
                } else {
                    apiInfo.setM("");
                }

                //请求信息
                Type parameterClazz = m.getGenericParameterTypes()[0];
                Set<NodeInfo> reqNodeList = new LinkedHashSet<>();
                Map<Type, Type> genericMap = ActualTypeMapperPool.get(parameterClazz);
                parseClazz(parameterClazz.getTypeName(), parameterClazz, genericMap, apiModelPropertyClazz, reqNodeList);
                if (m.getParameterTypes()[0].getTypeParameters().length > 0) {
                    Optional<NodeInfo> optional = reqNodeList.stream().filter(t -> Objects.equals("data", t.getNodeName())).findFirst();
                    optional.ifPresent(t -> apiInfo.setReqFieldList(t.getRespFieldList()));
                } else {
                    //文档模板暂时不支持多个请求节点显示
                    apiInfo.setReqFieldList(CollUtil.getFirst(reqNodeList).getRespFieldList());
                }
                apiInfo.setReqExample(toExample(reqNodeList, parameterClazz));

                //响应信息
                reqNodeList.clear();
                Type returnType = m.getGenericReturnType();
                genericMap = ActualTypeMapperPool.get(returnType);
                parseClazz(returnType.getTypeName(), returnType, genericMap, apiModelPropertyClazz, reqNodeList);
                apiInfo.setNodeList(reqNodeList);
                apiInfo.setRespExample(toExample(reqNodeList, returnType));
                return apiInfo;
            });
        }).collect(Collectors.toList());
    }

    private String toExample(Set<NodeInfo> nodeList, Type topClass) {
        Optional<NodeInfo> optional = nodeList.stream().filter(t -> Objects.equals(TypeUtil.getClass(topClass), t.getNodeType())).findFirst();
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        optional.ifPresent(t -> toJsonMap(nodeList, t, jsonMap));
        return JSONUtil.toJsonStr(jsonMap);
    }

    private Map<String, Object> toJsonMap(Set<NodeInfo> nodeList, NodeInfo topNode, Map<String, Object> topMap) {
        List<ApiField> apiFieldList = topNode.getRespFieldList();
        for (ApiField f : apiFieldList) {
            Optional<NodeInfo> op = nodeList.stream().filter(t -> t.getNodeType().equals(f.getClazzType())).findFirst();
            if (op.isEmpty()) {
                topMap.put(f.getFieldName(), f.getExampleValue() == null ? MockUtil.mockObject(f.getOriginClazzType()) : Convert.convert(f.getOriginClazzType(), f.getExampleValue()));
            } else {
                Class<?> originClazzType = f.getOriginClazzType();
                Object value;
                if (List.class.isAssignableFrom(originClazzType)) {
                    List<Object> arrayList = new ArrayList<>();
                    arrayList.add(toJsonMap(nodeList, op.get(), new LinkedHashMap<>(2)));
                    value = arrayList;
                } else if (Set.class.isAssignableFrom(originClazzType)) {
                    Set<Object> hashSet = new HashSet<>();
                    hashSet.add(toJsonMap(nodeList, op.get(), new LinkedHashMap<>(2)));
                    value = hashSet;
                } else if (originClazzType.isArray()) {
                    Object o = Array.newInstance(f.getClazzType(), 1);
                    Array.set(o, 0, toJsonMap(nodeList, op.get(), new LinkedHashMap<>(2)));
                    value = o;
                } else {
                    value = toJsonMap(nodeList, op.get(), new LinkedHashMap<>(2));
                }
                topMap.put(f.getFieldName(), value);
            }
        }
        return topMap;
    }

    private void parseClazz(String name, Type originClazz, Map<Type, Type> genericMap, Class apiModelPropertyClazz, Set<NodeInfo> nodeList) {
        Class<?> clazz = TypeUtil.getClass(originClazz);
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeType(clazz);
        nodeInfo.setNodeName(name);
        nodeInfo.setNodeDesc(clazz.getSimpleName());
        List<ApiField> fieldList = new ArrayList<>();
        nodeInfo.setRespFieldList(fieldList);
        nodeList.add(nodeInfo);
        Field[] fields;
        try {
            fields = ReflectUtil.getFields(clazz);
        } catch (Exception e) {
            LOGGER.error(() -> "反射获取类【" + clazz.getName() + "】属性字段出错", e);
            return;
        }
        Arrays.stream(fields).forEach(field -> {
            if (Modifier.isFinal(field.getModifiers())) {
                return;
            }
            Type genericType = field.getGenericType();
            Type newType = genericMap.get(genericType);
            if (Objects.nonNull(newType)) {
                genericType = newType;
                genericMap.putAll(ActualTypeMapperPool.get(genericType));
            }
            Class propertyType = TypeUtil.getClass(genericType);
            ApiField apiField = new ApiField();
            apiField.setFieldName(field.getName());
            apiField.setFieldType(propertyType.getSimpleName());
            apiField.setClazzType(propertyType);
            apiField.setOriginClazzType(propertyType);
            // value、required、example
            Map<String, Object> valueMap = AnnotationUtil.getAnnotationValueMap(field, apiModelPropertyClazz);
            boolean required = MapUtil.getBool(valueMap, "required", false);
            apiField.setRequired(required ? "是" : "否");
            String comment = MapUtil.getStr(valueMap, "value", "");
            apiField.setComment(HtmlUtil.escape(comment));
            String example = MapUtil.getStr(valueMap, "example", "");
            if (CharSequenceUtil.isEmpty(example)) {
                Object defaultValue = ClassUtil.getDefaultValue(propertyType);
                example = Objects.nonNull(defaultValue) ? defaultValue.toString() : null;
            }
            apiField.setExampleValue(example);
            if (isCollection(propertyType)) {
                Type returnType = field.getGenericType();
                if (returnType instanceof ParameterizedType) {
                    returnType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                }
                if (returnType instanceof Class) {
                    apiField.setClazzType((Class<?>) returnType);
                } else if (genericType instanceof ParameterizedType) {
                    returnType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                }
                if (returnType instanceof Class) {
                    apiField.setClazzType((Class<?>) returnType);
                } else {
                    Type type = genericMap.get(returnType);
                    apiField.setClazzType((Class<?>) type);
                }
                apiField.setFieldType(apiField.getFieldType() + "[" + apiField.getClazzType().getSimpleName() + "]");
            }
            if (propertyType.isArray()) {
                Class<?> type = propertyType.getComponentType();
                apiField.setClazzType(type);
            }
            fieldList.add(apiField);
            boolean stopFlag = ClassUtil.isSimpleTypeOrArray(apiField.getClazzType());
            if (stopFlag) {
                return;
            }
            parseClazz(field.getName(), apiField.getClazzType(), genericMap, apiModelPropertyClazz, nodeList);
        });
    }

    private boolean isCollection(Class<?> propertyType) {
        return List.class.isAssignableFrom(propertyType) || Set.class.isAssignableFrom(propertyType);
    }
} 
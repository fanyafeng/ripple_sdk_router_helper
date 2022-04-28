package com.ripple.sdk.router.hepler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Author： fanyafeng
 * Date： 2020-01-07 18:05
 * Email: fanyafeng@live.cn
 * <p>
 * https://www.jianshu.com/p/f85e5212be55
 */
@AutoService(Processor.class)
public class PageRouterNameProcessor extends AbstractProcessor {

    /**
     * Tag
     */
    private static String TAG = PageRouterNameProcessor.class.getSimpleName();
    /**
     * 文件相关的辅助类
     */
    private Filer mFiler;

    /**
     * 元素相关的辅助类
     */
    private Elements mElementUtils;

    /**
     * 日志相关的辅助类
     */
    private Messager mMessager;

    /**
     * 包名自己配置
     */
    private final static String PACKAGE_NAME = "com.router.annotation";

    private String packageName = PACKAGE_NAME;

    /**
     * 类的名称
     */
    private final String CLASS_NAME = "PageConfig";

    /**
     * 方法的名称
     */
    private static final String METHOD_NAME = "getPage";
    /**
     * 获取pageName
     * 通过value()注解获取
     */
    private static final String METHOD_PAGE_NAME = "pageNameMap";
    /**
     * 获取forwardName
     * 获取跳转链接，全名截取
     */
    private static final String METHOD_PAGE_FORWARD_NAME = "forwardNameMap";
    /**
     * 通过反射获取类的实例
     * 通过类的全名去获取
     */
    private static final String METHOD_PAGE_CLASS_NAME = "pageClassNameMap";
    /**
     * 获取当前类是否需要登录
     * 通过needLogin()注解获取
     */
    private static final String METHOD_PAGE_NEED_LOGIN = "needLoginMap";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.size() == 0) {
            return true;
        }

        Map<String, PageRouterNameModel> map = getPageNameData(roundEnvironment.getElementsAnnotatedWith(PageRouterName.class));
        generateCode(map);

        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, CLASS_NAME + " generated");
        return false;
    }

    private Map<String, PageRouterNameModel> getPageNameData(Set<? extends Element> elements) {
        Map<String, PageRouterNameModel> resultMap = new HashMap<>();
        for (Element element : elements) {
            String value = element.getAnnotation(PageRouterName.class).value();
            String pageName = element.getAnnotation(PageRouterName.class).pageName();
            String note = element.getAnnotation(PageRouterName.class).note();
            String fullName = mElementUtils.getBinaryName((TypeElement) element).toString();
            Boolean needLogin = element.getAnnotation(PageRouterName.class).needLogin();
            String forwardName = fullName.substring(fullName.lastIndexOf(".") + 1);
            resultMap.put(value, new PageRouterNameModel(fullName, pageName, forwardName, needLogin, note));
        }
        return resultMap;
    }

    /**
     * 构造函数体,如下：
     * public static Map<String,String> methodName()
     *
     * @param methodName   方法名
     * @param map          解析的map
     * @param variableName 变量名
     * @param mapValue     Map<K,V> V的方法名
     * @return 构造整个函数体
     */
    private MethodSpec generateMapMethod(String methodName, Map<String, PageRouterNameModel> map, String variableName, String mapValueMethodName) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Map.class);

        Method method = null;
        try {
            method = PageRouterNameModel.class.getDeclaredMethod(mapValueMethodName);
            String toData = getReflectType(method.getGenericReturnType().getTypeName());


            methodSpecBuilder.addStatement("$T<String," + toData + "> " + variableName + " = new $T<>()", Map.class, HashMap.class);
            for (Map.Entry<String, PageRouterNameModel> item : map.entrySet()) {

                if (method.invoke(item.getValue()) instanceof Boolean) {
                    Boolean value = (Boolean) method.invoke(item.getValue());
                    methodSpecBuilder.addStatement(variableName + ".put($S,$L)", item.getKey(), value);
                } else {
                    String value = (String) method.invoke(item.getValue());
                    if (null != value && !value.equals("")) {
                        methodSpecBuilder.addStatement(variableName + ".put($S,$S)", item.getKey(), value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        methodSpecBuilder.addStatement("return $N", variableName);
        MethodSpec methodSpec = methodSpecBuilder.build();

        return methodSpec;
    }

    private void generateCode(Map<String, PageRouterNameModel> map) {

        //创建类
        TypeSpec.Builder typeSpecBuilder = TypeSpec
                .classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(generateMapMethod(METHOD_PAGE_CLASS_NAME, map, "pageClassNameMap", "getPageClassName"))
                .addMethod(generateMapMethod(METHOD_PAGE_NAME, map, "pageNameMap", "getPageName"))
                .addMethod(generateMapMethod(METHOD_PAGE_FORWARD_NAME, map, "forwardNameMap", "getForwardName"))
                .addMethod(generateMapMethod(METHOD_PAGE_NEED_LOGIN, map, "pageNeedLoginMap", "isNeedLogin"));

        //创建成员变量
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
        for (Map.Entry<String, PageRouterNameModel> item : map.entrySet()) {
            String fieldName = item.getKey();
            PageRouterNameModel pageRouterNameModel = item.getValue();
            FieldSpec fieldSpec = FieldSpec
                    .builder(String.class, fieldName.toUpperCase(), modifiers)
                    .initializer("$S", fieldName)
                    .addJavadoc("{@link $N} \n\n$N \n", pageRouterNameModel.getPageClassName(), pageRouterNameModel.getNote())
                    .build();
            typeSpecBuilder.addField(fieldSpec);
        }
        TypeSpec typeSpec = typeSpecBuilder.build();

        JavaFile javaFile = JavaFile
                .builder(packageName, typeSpec)
                .build();

        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(PageRouterName.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 只能获取基础类型
     *
     * @param fromData
     * @return toData
     */
    private String getReflectType(String fromDataType) {
        String toDataType = null;
//        final String[] types = {
//                "java.lang.Integer",
//                "java.lang.Double",
//                "java.lang.Float",
//                "java.lang.Long",
//                "java.lang.Short",
//                "java.lang.Byte",
//                "java.lang.Boolean",
//                "java.lang.Character",
//                "java.lang.String",
//                "int", "double", "long", "short", "byte", "boolean", "char", "float"};
        switch (fromDataType) {
            case "java.lang.Integer":
            case "int":
                toDataType = "Integer";
                break;
            case "java.lang.Double":
            case "double":
                toDataType = "Double";
                break;
            case "java.lang.Float":
            case "float":
                toDataType = "Float";
                break;
            case "java.lang.Long":
            case "long":
                toDataType = "Long";
                break;
            case "java.lang.Short":
            case "short":
                toDataType = "Short";
                break;
            case "java.lang.Byte":
            case "byte":
                toDataType = "Byte";
                break;
            case "java.lang.Boolean":
            case "boolean":
                toDataType = "Boolean";
                break;
            case "java.lang.Character":
            case "char":
                toDataType = "Character";
                break;
            case "java.lang.String":
                toDataType = "String";
                break;
        }
        return toDataType;
    }
}

package com.au.module_flyablejson.apt;

import com.au.module_flyablejson.annotation.BeanFlyable;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FlyableJsonProcessor extends AbstractProcessor {
    /**
     * 生成文件的工具类
     */
    private Filer filer;
    /**
     * 打印信息
     */
    private Messager messager;
    /**
     * 元素相关
     */
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    /**
     * 设置支持的版本
     *
     * @return 这里用最新的就好
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    /**
     * 设置支持的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //添加支持的注解
        HashSet<String> set = new HashSet<>();
        set.add(BeanFlyable.class.getCanonicalName());
        return set;
    }

    /**
     * 注解内部逻辑的实现
     * <p>
     * Element代表程序的一个元素，可以是package, class, interface, method.只在编译期存在
     * TypeElement：变量；TypeElement：类或者接口
     *
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "process->>> " + annotations.size());
        var elements = roundEnv.getElementsAnnotatedWith(BeanFlyable.class);
        if (elements == null || elements.size() < 1) {//process()方法会调用3次，只有第一次有效，第2，3次调用的话生成.java文件会发生异常
            return true;
        }

        //2、按类来划分注解元素，因为每个使用注解的类都会生成相应的代理类
        for (Element element : elements) {
            //获取被注解的成员变量
            //这里被注解的类型只能是变量，所以可以直接强转
            VariableElement variableElement = (VariableElement) element;
            //获取该元素的父元素，这里是父类
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //获取全类名
            String className = typeElement.getQualifiedName().toString();
            //获取被注解元素的包名
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            //获取注解的参数
            //element.getAnnotation(FlyableBeanAnnotation.class).value();

            //生成ProxyInfo对象
            //一个类里面的注解都在一个ProxyInfo中处理
            ProxyInfo proxyInfo = proxyInfoMap.get(className);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(typeElement, packageName);
                proxyInfoMap.put(className, proxyInfo);
            }
            proxyInfo.viewVariableElement.put(resourceId, variableElement);
        }

        //3、生成注解逻辑处理类
        for (String key : proxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = proxyInfoMap.get(key);
            JavaFile javaFile = JavaFile.builder(proxyInfo.packageName, proxyInfo.generateProxyClass())
                    .addFileComment("auto generateProxyClass code,can not modify")
                    .build();


            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

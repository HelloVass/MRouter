package com.chiclaim.modularization.router.compiler;

import com.chiclaim.modularization.router.annotation.Autowire;
import com.chiclaim.modularization.router.annotation.Constant;
import com.chiclaim.modularization.router.compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;


@AutoService(Processor.class)
@SupportedOptions(Constant.KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AutowireProcessor extends AbstractProcessor {

    private static final String supportTypes =
            "byte, byte[],char,char[],short,short[],int,int[],List<Integer>,ArrayList<Integer>," +
                    "long,long[],float,float[],double[],boolean,boolean[],String,String[],List<String>,ArrayList<String>," +
                    "Parcelable,Parcelable[],List<Parcelable>,ArrayList<Parcelable>,Serializable";

    private Types types;
    private Elements elements;

    private Filer filter;
    private Messager messager;
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.
        filter = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        processingEnvironment.getOptions();
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            moduleName = options.get(Constant.KEY_MODULE_NAME);
            if (moduleName != null && moduleName.length() > 0) {
                moduleName = ProcessorUtils.filterModuleName(moduleName);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Autowire.class.getCanonicalName());
        return set;
    }

    private void printElement(Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        note(element, "========annotation 所在的类完整名称 " + enclosingElement.getQualifiedName());
        note(element, "========annotation 所在类的类名 " + enclosingElement.getSimpleName());
        note(element, "========annotation 所在类的父类 " + enclosingElement.getSuperclass());
        note(element, "        annotation所在的类 " + enclosingElement.asType());
        note(element, "        annotation所在的字段类型 " + element.asType());
        note(element, "        annotation 上的值 " + element.getAnnotation(Autowire.class).name());
        note(element, "        type is Activity " + ProcessorUtils.isInActivity(elements, types, enclosingElement));
    }

    private void error(Element element, String message, Object... args) {
        printMessage(element, Diagnostic.Kind.ERROR, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(element, Diagnostic.Kind.NOTE, message, args);
    }

    private void printMessage(Element element, Diagnostic.Kind kind, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(kind, message, element);
    }

    private void checkFieldModifier(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            error(element, "@%s %s must not be private or static. (%s.%s)",
                    Autowire.class.getSimpleName(), "fields", enclosingElement.getQualifiedName(),
                    element.getSimpleName());
        }
    }

    private void checkSupportType(Element element, TypeKind kind, String fieldName) {
        if (kind == TypeKind.UNKNOWN) {
            String errorMessage = "The field " + fieldName + "'s type do not support. " +
                    "Please check the support type list : [ " + supportTypes + " ]";
            error(element, errorMessage);
        }
    }

    private void checkAutowireTargetClass(Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        if (!ProcessorUtils.isInActivity(elements, types, enclosingElement) &&
                !ProcessorUtils.isInFragment(elements, types, enclosingElement)) {
            error(element, "The place to use the annotation @%s must be in Activity or Fragment"
                    , Autowire.class.getSimpleName());
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Map<TypeElement, AutowireRouteClass> map = new LinkedHashMap<>();
        Set<? extends Element> autowireElements = roundEnvironment.getElementsAnnotatedWith(Autowire.class);
        for (Element element : autowireElements) {
            //printElement(element);
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            AutowireRouteClass autowireRouteClass = map.get(enclosingElement);
            if (autowireRouteClass == null) {
                autowireRouteClass = AutowireRouteClass.createWhenApplyField(element);
                map.put(enclosingElement, autowireRouteClass);
            }
            String annotationValue = element.getAnnotation(Autowire.class).name();
            String fieldName = element.getSimpleName().toString();
            TypeName fieldType = TypeName.get(element.asType());

            TypeKind kind = ProcessorUtils.getElementType(element, types, elements);

            checkSupportType(element, kind, fieldName);
            checkFieldModifier(element);
            checkAutowireTargetClass(element);

            boolean isActivity = ProcessorUtils.isInActivity(elements, types, enclosingElement);
            String assignStatement = ProcessorUtils.getAssignStatementByTypeKind(kind, isActivity);
            AutowireField viewBinding = AutowireField.create(fieldName, fieldType, annotationValue, assignStatement, kind);
            autowireRouteClass.addAnnotationField(viewBinding);

        }

        for (Map.Entry<TypeElement, AutowireRouteClass> entry : map.entrySet()) {
            try {
                entry.getValue().preJavaFile().writeTo(filter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

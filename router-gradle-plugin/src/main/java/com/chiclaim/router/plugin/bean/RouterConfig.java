package com.chiclaim.router.plugin.bean;

import com.intellij.openapi.util.text.StringUtil;

public class RouterConfig {

    /**
     * 组件中的初始化类都实现了 componentInterface
     */
    private String componentInterface;

    /**
     * 为了提高扫描效率，可以配置 每个组件中的初始化类的包名。这样就不用读取每个 class .
     * <p>
     * 这样的话可以通过包名来判断是否要读取 class，然后判断父接口是否是 componentInterface
     * <p>
     * 例如 MRouter 各个组件的初始化类包名都是 com.chiclaim.modularization.router
     */
    private String componentPackage;

    /**
     * 将所有组件的初始化调用注入到 routerInitClass 中
     */
    private String routerInitClass;

    /**
     * 注入到 routerInitClass 的 routerInitMethod 方法中（静态方法）
     */
    private String routerInitMethod;

    /**
     * routerInitMethod 方法的描述符，可以不配置。默认为 ()V ，也就是没有返回值和入参的方法
     */
    private String routerInitMethodDescriptor;

    public String getComponentInterface() {
        return componentInterface;
    }

    public void setComponentInterface(String componentInterface) {
        this.componentInterface = dot2slash(componentInterface);
    }

    public String getComponentPackage() {
        return componentPackage;
    }

    public void setComponentPackage(String componentPackage) {
        this.componentPackage = dot2slash(componentPackage);
    }

    public String getRouterInitClass() {
        return routerInitClass;
    }

    public void setRouterInitClass(String routerInitClass) {
        this.routerInitClass = dot2slash(routerInitClass);
    }

    public String getRouterInitMethod() {
        return routerInitMethod;
    }

    public void setRouterInitMethod(String routerInitMethod) {
        this.routerInitMethod = routerInitMethod;
    }

    public String getRouterInitMethodDescriptor() {
        if (StringUtil.isEmpty(routerInitMethodDescriptor)) {
            return "()V";
        }
        return routerInitMethodDescriptor;
    }

    public void setRouterInitMethodDescriptor(String routerInitMethodDescriptor) {
        this.routerInitMethodDescriptor = routerInitMethodDescriptor;
    }

    @Override
    public String toString() {
        return "RouterConfig {\n\t" +
                " componentInterface='" + componentInterface + '\'' +
                ", \n\t componentPackage='" + componentPackage + '\'' +
                ", \n\t routerInitClass='" + routerInitClass + '\'' +
                ", \n\t routerInitMethod='" + routerInitMethod + '\'' +
                ", \n\t routerInitMethodDescriptor='" + routerInitMethodDescriptor + '\'' +
                "\n}";
    }


    private static String dot2slash(String str) {
        if (str == null) return null;
        return str.replaceAll("\\.", "/");
    }
}

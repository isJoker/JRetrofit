package cn.com.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import cn.com.retrofit.http.Field;
import cn.com.retrofit.http.GET;
import cn.com.retrofit.http.POST;
import cn.com.retrofit.http.Query;
import okhttp3.Call;
import okhttp3.HttpUrl;

/**
 * Created by JokerWan on 2019-09-23.
 * Function: 保存方法所有内容：方法名、方法注解、参数注解、参数值
 */
class ServiceMethod {

    // OkHttpClient唯一实现接口
    private final Call.Factory callFactory;
    // 接口请求地址
    private final HttpUrl baseUrl;
    // 方法的请求方式（"GET"、"POST"）
    private final String httpMethod;
    // 方法的注解的值（"/ip/ipNew"）
    private final String relativeUrl;
    // 方法参数的数组（每个对象包含：参数注解值、参数值）
    private final ParameterHandler[] parameterHandlers;
    // 是否有请求体（GET方式没有）
    private final boolean hasBody;

    private ServiceMethod(Call.Factory callFactory, HttpUrl baseUrl, String httpMethod,
                          String relativeUrl, ParameterHandler[] parameterHandlers, boolean hasBody) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.httpMethod = httpMethod;
        this.relativeUrl = relativeUrl;
        this.parameterHandlers = parameterHandlers;
        this.hasBody = hasBody;
    }

    Call toCall(Object... args) {
        // 实例化RequestBuilder对象，拼接完整请求url（包含参数名和参数值）
        RequestBuilder requestBuilder = new RequestBuilder(httpMethod, baseUrl, relativeUrl, hasBody);
        ParameterHandler[] handlers = parameterHandlers;
        int argumentCount = args != null ? args.length : 0;
        // 校验Proxy方法的参数个数是否等于参数的数组（手动添加）的长度
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }

        // 循环拼接每个参数名和参数值
        for (int i = 0; i < argumentCount; i++) {
            // 方法参数的数组中每个对象已经调用了对应实现方法
            handlers[i].apply(requestBuilder, args[i].toString());
        }

        // 创建请求
        return callFactory.newCall(requestBuilder.build());
    }

    static final class Builder {

        // OkHttpClick封装构建
        final Retrofit retrofit;
        // 带注解的方法
        final Method method;
        // 方法的所有注解（方法可能有多个注解）
        final Annotation[] methodAnnotations;
        // 方法参数的所有注解（一个方法有多个参数，一个参数有多个注解）
        final Annotation[][] parameterAnnotationsArray;
        // 方法的请求方式（"GET"、"POST"）
        private String httpMethod;
        // 方法的注解的值（"/ip/ipNew"）
        private String relativeUrl;
        // 方法参数的数组（每个对象包含：参数注解值、参数值）
        private ParameterHandler[] parameterHandlers;
        // 是否有请求体（GET方式没有）
        private boolean hasBody;

        Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            // 获取方法的所有注解
            this.methodAnnotations = method.getAnnotations();
            // 获取方法参数的所有注解
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        ServiceMethod build() {
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            // 定义方法参数的数组长度
            // 定义方法参数的数组长度
            int parameterCount = parameterAnnotationsArray.length;
            // 初始化方法参数的数组
            parameterHandlers = new ParameterHandler[parameterCount];
            // 遍历方法的参数（我们只列举Query或者Field注解）
            for (int i = 0; i < parameterCount; i++) {
                // 获取每个参数的所有注解
                Annotation[] parameterAnnotations = parameterAnnotationsArray[i];
                if (parameterAnnotations == null) {
                    throw new IllegalArgumentException("No Retrofit annotation found." + " (parameter #" + (i + 1) + ")");
                }

                parameterHandlers[i] = parseParameter(i, parameterAnnotations);
            }

            return new ServiceMethod(retrofit.callFactory(), retrofit.baseUrl(), httpMethod,
                    relativeUrl, parameterHandlers, hasBody);
        }

        /**
         * 解析方法的注解，可能是GET或者POST或者其他，目前暂且只考虑GET、POST
         *
         * @param annotation 方法的每个注解
         */
        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof GET) {
                // 注意：GET方式没有请求体RequestBody
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            }
        }

        /**
         * 通过方法的注解，获取方法的请求方式、方法的注解的值
         */
        private void parseHttpMethodAndPath(String httpMethod, String relativeUrl, boolean hasBody) {
            // 方法的请求方式（"GET"、"POST"）
            this.httpMethod = httpMethod;
            // 方法的注解的值（"/ip/ipNew"）
            this.relativeUrl = relativeUrl;
            // 是否有请求体
            this.hasBody = hasBody;
        }

        /**
         * 解析参数的所有注解
         *
         * @param i                    参数index
         * @param parameterAnnotations 参数的所有注解
         * @return ParameterHandler
         */
        private ParameterHandler parseParameter(int i, Annotation[] parameterAnnotations) {
            ParameterHandler result = null;
            // 遍历参数的注解，如：(@Query("ip") @Field("ip") String ip)
            for (Annotation annotation : parameterAnnotations) {
                // 注解可能是Query或者Field
                ParameterHandler annotationAction = parseParameterAnnotation(annotation);
                if (annotationAction == null) {
                    continue;
                }
                result = annotationAction;
            }
            if (result == null) {
                throw new IllegalArgumentException("No Retrofit annotation found." + " (parameter #" + (i + 1) + ")");
            }
            return result;
        }

        /**
         *  解析参数的注解，可能是Query或者Field
         */
        private ParameterHandler parseParameterAnnotation(Annotation annotation) {
            if (annotation instanceof Query) {
                Query query = (Query) annotation;
                // 参数的注解
                String name = query.value();
                return new ParameterHandler.Query(name);
            } else if(annotation instanceof Field) {
                Field field = (Field) annotation;
                // 参数的注解
                String name = field.value();
                return new ParameterHandler.Field(name);
            }
            return null;
        }

    }
}

package cn.com.retrofit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Created by JokerWan on 2019-09-23.
 * Function:
 */
public class Retrofit {

    // 接口请求地址
    private final HttpUrl baseUrl;
    // OkHttpClient唯一实现接口
    private final Call.Factory callFactory;
    // 缓存请求方法
    // key：请求方法，如：host.get()  value：该方法的属性封装，如：方法名、方法注解、参数注解、参数
    private final Map<Method, ServiceMethod> serviceMethodCache = new ConcurrentHashMap<>();

    private Retrofit(HttpUrl baseUrl, Call.Factory callFactory) {
        this.baseUrl = baseUrl;
        this.callFactory = callFactory;
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 获取方法所有内容：方法名、方法注解、参数注解、参数
                ServiceMethod serviceMethod = loadServiceMethod(method);
                return new OkHttpCall(serviceMethod, args);
            }
        });
    }

    /**
     * 获取方法所有内容：方法名、方法注解、参数注解、参数
     * 通过反射获取，有一定开销，存map里面，防止重复获取，减少开销
     */
    private ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod result = serviceMethodCache.get(method);
        if (result != null) return result;

        // 线程安全同步锁
        synchronized (serviceMethodCache) {
            // 排队等待的result为空，出来result已经赋值了
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }


    okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    HttpUrl baseUrl() {
        return baseUrl;
    }

    public static class Builder {
        // 接口请求地址
        private HttpUrl baseUrl;
        // OkHttpClient唯一实现接口
        private Call.Factory callFactory;

        public Builder baseUrl(String baseUrl) {
            if (baseUrl.isEmpty()) {
                throw new NullPointerException("baseUrl == null");
            }
            this.baseUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        public Builder baseUrl(HttpUrl baseUrl) {
            if (baseUrl == null) {
                throw new NullPointerException("baseUrl == null");
            }
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder callFactory(Call.Factory callFactory) {
            this.callFactory = callFactory;
            return this;
        }

        // 属性校验和初始化
        public Retrofit build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            return new Retrofit(baseUrl, callFactory);
        }
    }
}

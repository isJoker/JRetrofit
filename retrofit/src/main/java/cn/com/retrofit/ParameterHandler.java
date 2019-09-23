package cn.com.retrofit;

import androidx.annotation.Nullable;

/**
 * Created by JokerWan on 2019-09-23.
 * Function:    (@Field("ip") String ip)
 *              保存参数的注解值、参数值，用于拼接请求
 */
abstract class ParameterHandler {

    /**
     * 抽象方法，自我实现。外部赋值和调用
     *
     * @param builder 请求构建者
     * @param value 方法参数值（外部循环）
     */
    abstract void apply(RequestBuilder builder, @Nullable String value);

    static final class Query extends ParameterHandler {
        private final String name;

        Query(String name) {
            if(name.isEmpty()) {
                throw new IllegalArgumentException("name == null");
            }
            this.name = name;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable String value) {
            if (value == null) return;
            // 拼接Query参数，此处name为参数注解的值，value为参数值
            builder.addQueryParam(name, value);
        }
    }

    static final class Field extends ParameterHandler {
        private final String name;

        // 注意：传过来的是注解的值，并非参数值
        Field(String name) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name == null");
            }
            this.name = name;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable String value) {
            if (value == null) return;
            // 拼接Query参数，此处name为参数注解的值，value为参数值
            builder.addFormField(name, value);
        }
    }
}

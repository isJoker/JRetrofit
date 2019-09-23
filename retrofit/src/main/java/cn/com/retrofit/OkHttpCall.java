package cn.com.retrofit;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

/**
 * Created by JokerWan on 2019-09-23.
 * Function:
 */
public class OkHttpCall implements Call {

    private final ServiceMethod serviceMethod;
    private final Object[] args;
    private final okhttp3.Call rawCall;

    /**
     * 实例化okhttp3.Call rawCall对象
     *
     * @param serviceMethod 调用toCall(args)方法
     * @param args 方法参数
     */
    OkHttpCall(ServiceMethod serviceMethod, @Nullable Object[] args) {
        this.serviceMethod = serviceMethod;
        this.args = args;
        this.rawCall = serviceMethod.toCall(args);
    }

    @Override
    public void cancel() {
        rawCall.cancel();
    }

    @NotNull
    @Override
    public Call clone() {
        return new OkHttpCall(serviceMethod, args);
    }

    @Override
    public void enqueue(@NotNull Callback callback) {
        rawCall.enqueue(callback);
    }

    @NotNull
    @Override
    public Response execute() throws IOException {
        return rawCall.execute();
    }

    @Override
    public boolean isCanceled() {
        return rawCall.isCanceled();
    }

    @Override
    public boolean isExecuted() {
        return rawCall.isExecuted();
    }

    @NotNull
    @Override
    public Request request() {
        return rawCall.request();
    }

    @NotNull
    @Override
    public Timeout timeout() {
        return rawCall.timeout();
    }
}

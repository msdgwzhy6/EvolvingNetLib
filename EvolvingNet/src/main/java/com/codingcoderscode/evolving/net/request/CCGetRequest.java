package com.codingcoderscode.evolving.net.request;


import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.request.retry.FlowableRetryWithDelay;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.response.convert.CCDefaultResponseBodyConvert;
import com.codingcoderscode.evolving.net.util.CCNetUtil;

import org.reactivestreams.Publisher;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by ghc on 2017/10/26.
 * <p>
 * GET类型请求类
 */

public class CCGetRequest<T> extends CCRequest<T, CCGetRequest<T>> {

    public CCGetRequest(String url) {
        this.apiUrl = url;
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        return Flowable.create(new FlowableOnSubscribe<Call<ResponseBody>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<ResponseBody>> e) throws Exception {
                Call<ResponseBody> call = CCRxNetManager.getCcNetApiService().executeGet(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getParamMap());

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<Call<ResponseBody>, Publisher<CCBaseResponse<T>>>() {

                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Call<ResponseBody> responseBodyCall) throws Exception {

                        T realResponse = null;
                        Response<ResponseBody> retrofitResponse;
                        Headers headers = null;
                        try {
                            retrofitResponse = responseBodyCall.clone().execute();

                            headers = retrofitResponse.headers();

                            //realResponse = CCDefaultResponseBodyConvert.<T>convertResponse(retrofitResponse.body(), responseBeanType);

                            realResponse = convertResponse(retrofitResponse.body());

                        } catch (Exception exception) {
                            throw exception;
                        }


                        return Flowable.just(new CCBaseResponse<T>(realResponse, headers, false, false, false));
                    }
                }).retryWhen(new FlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
        /*
        Call<ResponseBody> call = CCRxNetManager.getCcNetApiService().executeGet(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getParamMap());

        return Flowable.just(call)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<Call<ResponseBody>, Publisher<CCBaseResponse<T>>>() {

                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Call<ResponseBody> responseBodyCall) throws Exception {

                        T realResponse = null;
                        Response<ResponseBody> retrofitResponse;
                        Headers headers = null;
                        try {
                            retrofitResponse = responseBodyCall.clone().execute();

                            headers = retrofitResponse.headers();

                            //realResponse = CCDefaultResponseBodyConvert.<T>convertResponse(retrofitResponse.body(), responseBeanType);

                            realResponse = convertResponse(retrofitResponse.body());

                            int a = 10;

                            int b = a / 0;

                        } catch (Exception exception) {
                            throw exception;
                        }


                        return Flowable.just(new CCBaseResponse<T>(realResponse, headers, false, false, false));
                    }
                }).retryWhen(new FlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
        */
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.GET;
    }
}

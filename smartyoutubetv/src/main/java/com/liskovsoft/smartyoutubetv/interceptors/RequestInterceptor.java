package com.liskovsoft.smartyoutubetv.interceptors;

import android.content.Context;
import android.webkit.WebResourceResponse;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.okhttp.OkHttpHelpers;
import com.liskovsoft.smartyoutubetv.R;
import com.liskovsoft.smartyoutubetv.misc.HeaderManager;
import okhttp3.MediaType;
import okhttp3.Response;

import java.io.InputStream;

public abstract class RequestInterceptor {
    private static final String TAG = RequestInterceptor.class.getSimpleName();
    private final Context mContext;
    private final HeaderManager mManager;

    public abstract boolean test(String url);
    public abstract WebResourceResponse intercept(String url);

    public RequestInterceptor(Context context) {
        mContext = context;
        mManager = new HeaderManager(context);
    }

    private String getMimeType(MediaType contentType) {
        String type = contentType.type();
        String subtype = contentType.subtype();
        return String.format("%s/%s", type, subtype);
    }

    private String getCharset(MediaType contentType) {
        if (contentType.charset() == null) {
            return null;
        }
        return contentType.charset().name();
    }

    protected WebResourceResponse createResponse(MediaType mediaType, InputStream is) {
        WebResourceResponse resourceResponse = new WebResourceResponse(
                getMimeType(mediaType),
                getCharset(mediaType),
                is
        );
        return resourceResponse;
    }

    protected WebResourceResponse prependResponse(String url, InputStream toPrepend) {
        Response response = OkHttpHelpers.doOkHttpRequest(url);

        if (response == null) {
            Log.e(TAG, "Oops... unknown error inside OkHttpHelpers: " + url);
            return null;
        }

        InputStream responseStream = response.body().byteStream();
        return createResponse(response.body().contentType(), Helpers.appendStream(toPrepend, responseStream));
    }

    protected WebResourceResponse appendResponse(String url, InputStream toAppend) {
        Response response = OkHttpHelpers.doOkHttpRequest(url);

        if (response == null) {
            MessageHelpers.showLongMessageEndPause(mContext, R.string.fix_clock_msg);
        }

        InputStream responseStream = response.body().byteStream();
        return createResponse(response.body().contentType(), Helpers.appendStream(responseStream, toAppend));
    }

    protected InputStream getUrlData(String url) {
        Response response = OkHttpHelpers.doGetOkHttpRequest(url, mManager.getHeaders());

        return response == null ? null : response.body().byteStream();
    }

    protected InputStream postUrlData(String url, String body) {
        Response response = OkHttpHelpers.doPostOkHttpRequest(url, mManager.getHeaders(), body, "application/json");

        return response == null ? null : response.body().byteStream();
    }
}

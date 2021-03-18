package com.jinung.edu.sims.data;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

class ByteRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;

    public ByteRequest(String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(0, url, errorListener);
        setShouldCache(false);
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(byte[] response) {
        this.mListener.onResponse(response);
    }

    /* access modifiers changed from: protected */
    public Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}

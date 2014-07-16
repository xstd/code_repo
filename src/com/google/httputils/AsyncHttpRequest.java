package com.google.httputils;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;

class AsyncHttpRequest implements Runnable {
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final HttpUriRequest request;
    private final AsyncHttpResponseHandler responseHandler;
    private int executionCount;

    public AsyncHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, AsyncHttpResponseHandler responseHandler) {
        this.client = client;
        this.context = context;
        this.request = request;
        this.responseHandler = responseHandler;
        client.getParams().setIntParameter(
                HttpConnectionParams.SO_TIMEOUT, 8000); // 超时设置
        client.getParams().setIntParameter(
                HttpConnectionParams.CONNECTION_TIMEOUT, 5000);// 连接超时
    }

    @Override
	public void run() {
        try {
            if(responseHandler != null){
                responseHandler.sendStartMessage();
            }

            makeRequestWithRetries();

            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
            }
        } catch (IOException e) {
            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
                responseHandler.sendFailureMessage(e, null);
            }
        }
    }
    
    private void makeRequest() throws IOException {
    	if(!Thread.currentThread().isInterrupted()) {
    		HttpResponse response = client.execute(request, context);
    		if(!Thread.currentThread().isInterrupted()) {
    			if(responseHandler != null) {
    				responseHandler.sendResponseMessage(response);
    			}
    		} else{
    			//TODO: should raise InterruptedException? this block is reached whenever the request is cancelled before its response is received
    		}
    	}
    }

    private void makeRequestWithRetries() throws ConnectException {
        // This is an additional layer of retry logic lifted from droid-fu
        // See: https://github.com/kaeppler/droid-fu/blob/master/src/main/java/com/github/droidfu/http/BetterHttpRequestBase.java
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        // 重试次数3
        int retryCount = 0;
        while (retry && retryCount < 3) {
            try {
                makeRequest();
                return;
            } catch (IOException e) {
            	retryCount++;
            	e.printStackTrace();
                cause = e;
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } catch (NullPointerException e) {
            	retryCount++;
                // there's a bug in HttpClient 4.0.x that on some occasions causes
                // DefaultRequestExecutor to throw an NPE, see
                // http://code.google.com/p/android/issues/detail?id=5255
                cause = new IOException("NPE in HttpClient" + e.getMessage());
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } 
        }

        // no retries left, crap out with exception
        ConnectException ex = new ConnectException();
        ex.initCause(cause);
        throw ex;
    }
}
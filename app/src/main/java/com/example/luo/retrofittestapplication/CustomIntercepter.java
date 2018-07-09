package com.example.luo.retrofittestapplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * <p>Description.</p>
 *
 * <b>Maintenance History</b>:
 * <table>
 * 		<tr>
 * 			<th>Date</th>
 * 			<th>Developer</th>
 * 			<th>Target</th>
 * 			<th>Content</th>
 * 		</tr>
 * 		<tr>
 * 			<td>2018-05-30 16:06</td>
 * 			<td>Rui chaoqun</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public class CustomIntercepter implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        request = handlerRequest(request);
        Response response = chain.proceed(request);
        return response;
    }

    private Request handlerRequest(Request request) {
        Map<String,String> params = parseParams(request);
        if(params == null){
            params = new HashMap<>();
        }
        params.put("common","value");
        params.put("timeToken",String.valueOf("12321421312"));
        String method = request.method();
        if("GET".equals(method)){
            StringBuilder builder = new StringBuilder("http://baseUrl.com");
            builder.append("?").append(map2QueryStr(params));
        }else{
            if(request.body() instanceof  FormBody){
                FormBody.Builder builder = new FormBody.Builder();
                Iterator<Map.Entry<String,String>> iterator = params.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String,String> entry = iterator.next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    builder.add(key,value);
                }
                return request.newBuilder()
                        .header("cookie","asdasfasfda")
                        .header("sadasd","asdqweqfvdf")
                        .addHeader("cookie","oloolol")
                        .header("cookie","ppppppp")
                        .method(method,builder.build()).build();
            }
        }
        return request.newBuilder().header("cookie","12312").method(request.method(),request.body()).build();
    }

    private String map2QueryStr(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        Set<String> names = params.keySet();
        for (String name:names) {
            builder.append(name).append("=").append(params.get(name)).append("&");
        }
        return builder.length() > 0 ?builder.substring(0,builder.length() - 1):"";
    }

    private Map<String, String> parseParams(Request request) {
        String method = request.method();
        Map<String,String> params = null;
        if("GET".equals(method)){
            params = doGet(request);
        }else if("POST".equals(method)||"PUT".equals(method)||"DELETE".equals(method)||"PATCH".equals(method)){
            RequestBody requestBody = request.body();
            if(requestBody != null && requestBody instanceof FormBody){
                params = doForm(request);
            }
        }
        return params;
    }

    private Map<String, String> doGet(Request request) {
        Map<String,String> params = null;
        HttpUrl url = request.url();
        Set<String> strings = url.queryParameterNames();
        if(strings != null){
            Iterator<String> iterator = strings.iterator();
            params = new HashMap<>();
            int i = 0;
            while (iterator.hasNext()){
                String name = iterator.next();
                String value = url.queryParameterValue(i);
                params.put(name,value);
                i++;
            }
        }
        return params;
    }

    private Map<String, String> doForm(Request request) {
        Map<String,String> params = null;
        FormBody body = null;
        try {
            body = (FormBody) request.body();
        }catch (ClassCastException e){}
        if(body != null){
            int size = body.size();
            if(size > 0){
                params = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    params.put(body.encodedName(i),body.encodedValue(i));
                }
            }
        }
        return params;
    }
}

package net.sonmoosans.u3.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.sonmoosans.u3.api.Memory;
import net.sonmoosans.u3.api.model.Result;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class APICaller {
    public static final String webURL = "http://localhost:8080/";
    public static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final HttpClient httpclient = new DefaultHttpClient();

    private static <T extends HttpEntityEnclosingRequestBase> T getEntity(T http, File file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setContentType(ContentType.MULTIPART_FORM_DATA);
        builder.addBinaryBody("file", file);

        http.setEntity(builder.build());

        http.setHeader("token", getToken());

        return http;
    }

    private static <T extends HttpEntityEnclosingRequestBase> T getEntity(T http, Iterable<File> files) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setContentType(ContentType.MULTIPART_FORM_DATA);

        for (File file : files) {
            builder.addBinaryBody(file.getName(), file);
        }

        HttpEntity entity = builder.build();

        http.setEntity(entity);

        http.setHeader("token", getToken());

        return http;
    }

    /**
     * @throws IOException If failed to do http request
     * **/
    private static HttpURLConnection connect(String method, String path, Parameter... parameters) throws IOException {
        path = getFullPath(path, parameters);

        URL url = new URL(webURL + path);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.addRequestProperty("token", getToken());

        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP error code : " + conn.getResponseCode());
        }

        return conn;
    }

    private static String getToken() {
        String token = Memory.getSelfToken();
        return token == null? "" : token;
    }

    private static String GET(String path, Parameter... parameters) throws IOException {
        HttpURLConnection conn = connect("GET", path, parameters);
        String response = new String(conn.getInputStream().readAllBytes());
        conn.disconnect();
        return response;
    }

    private static String POST(String path, Parameter... parameters) throws IOException {
        HttpURLConnection conn = connect("POST", path, parameters);
        String response = new String(conn.getInputStream().readAllBytes());
        conn.disconnect();
        return response;
    }

    public static <T> Result<T> callGET(Class<T> type, String path, Parameter... parameters) {
        Result<T> result;
        try {
            String json = GET(path, parameters);

            result = getResult(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result<>(false, null);
        }
        return result;
    }

    public static boolean callPOST(String path, Parameter... parameters) {
        try {
            connect("POST", path, parameters).disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> Result<T> callPOST(Class<T> type, String path, Parameter... parameters) {
        Result<T> result;
        try {
            String json = POST(path, parameters);

            result = getResult(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result<>(false, null);
        }
        return result;
    }

    public static boolean callPOSTFile(File file, String path, Parameter... parameters) {
        boolean success = false;
        try {
            success = execute(
                    getEntity(getPOST(getFullPath(path, parameters)), file)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public static <T> Result<T> callPOSTFile(Class<T> type, File file, String path, Parameter... parameters) {
        Result<T> result;
        try {
            HttpPost post = getEntity(getPOST(getFullPath(path, parameters)), file);
            HttpResponse response = httpclient.execute(post);
            String json = getString(response.getEntity().getContent());
            post.releaseConnection();

            result = getResult(json, type, response.getStatusLine().getStatusCode() == 200);
        } catch (Exception e) {
            e.printStackTrace();
            result = new Result<>(false, null);
        }
        return result;
    }

    public static boolean callPOSTFiles(Iterable<File> files, String path, Parameter... parameters) {
        boolean success = false;
        try {
            success = execute(
                    getEntity(getPOST(getFullPath(path, parameters)), files)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    /**@return Success to do http request**/
    private static boolean execute(HttpEntityEnclosingRequestBase request) throws IOException {
        HttpResponse response = httpclient.execute(request);
        boolean success = response.getStatusLine().getStatusCode() == 200;
        request.releaseConnection();
        return success;
    }

    private static String getFullPath(String path, Parameter... parameters) {
        if (parameters.length > 0) {
            StringBuilder sb = new StringBuilder(path + "?");
            for (int i = 0;i < parameters.length;i++) {
                if (i != 0) sb.append("&");
                sb.append(parameters[i].toString());
            }

            path = sb.toString();
        }
        return path;
    }

    public static boolean callPUT(String path, Parameter... parameters) {
        try {
            connect("PUT", path, parameters).disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean callPUTFile(File file, String path, Parameter... parameters) {
        try {
            return execute(getEntity(getPUT(getFullPath(path, parameters)), file));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean callDELETE(String path, Parameter... parameters) {
        try {
            connect("DELETE", path, parameters).disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static HttpPost getPOST(String path) {
        return new HttpPost(webURL + path);
    }

    private static HttpPut getPUT(String path) {
        return new HttpPut(webURL + path);
    }

    private static String getString(InputStream in) throws IOException {
        return new String(in.readAllBytes());
    }

    private static <T> Result<T> getResult(String json, Class<T> type) throws JsonProcessingException {
        return getResult(json, type, true);
    }

    private static <T> Result<T> getResult(String json, Class<T> type, boolean success) throws JsonProcessingException {
        T response = type == String.class? (T) json : mapper.readValue(json, type);

        return new Result<>(success, response);
    }
}

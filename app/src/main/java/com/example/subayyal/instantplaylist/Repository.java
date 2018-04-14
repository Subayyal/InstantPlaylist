package com.example.subayyal.instantplaylist;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

import static com.google.api.client.util.ByteStreams.copy;

/**
 * Created by subayyal on 4/11/2018.
 */

public class Repository {

    private YouTube youTube;
    private Context context;

    public Repository(Context c) {
        this.context = c;
        youTube = new YouTube.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                String packageName = context.getPackageName();
                String SHA1 = getSHA1(packageName);

                request.getHeaders().set("X-Android-Package", packageName);
                request.getHeaders().set("X-Android-Cert",SHA1);
            }
        }).setApplicationName("Instant Playlist").build();
    }

    public Single<List<SearchResult>> search(){
        return Single.create(new SingleOnSubscribe<List<SearchResult>>() {
            @Override
            public void subscribe(SingleEmitter<List<SearchResult>> emitter) throws Exception {
                List<SearchResult> results = null;
                try {
                    YouTube.Search.List query;
                    query = youTube.search().list("id, snippet");
                    query.setKey(Constants.apiKey);
                    query.setType("video");
                    query.setQ("lionel messi");
                    query.setMaxResults(Long.valueOf(10));
                    SearchListResponse response = query.execute();
                    results = response.getItems();
                    for(SearchResult result: results){
                        String json = new Gson().toJson(result);
                        Log.d("Test", json);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                emitter.onSuccess(results);
            }
        });
    }

    private String getSHA1(String packageName){
        try {
            Signature[] signatures = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
            for (Signature signature: signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA-1");
                md.update(signature.toByteArray());
                return BaseEncoding.base16().encode(md.digest());
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Observable<Bitmap> loadBitmap(final String url) {
        return Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                Bitmap bitmap = null;
                InputStream in = null;
                BufferedOutputStream out = null;

                try {
                    in = new BufferedInputStream(new URL(url).openStream(), 4 * 1024);

                    final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                    out = new BufferedOutputStream(dataStream, 4 * 1024);
                    copy(in, out);
                    out.flush();

                    final byte[] data = dataStream.toByteArray();
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
                } catch (IOException e) {
                    Log.e("Test", "Could not load Bitmap from: " + url);
                } finally {
                    if (in != null && out != null) {
                        try {
                            in.close();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                emitter.onNext(bitmap);
            }
        });
    }
}

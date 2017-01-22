package es.schooleando.xkcdcomichandlers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static es.schooleando.xkcdcomichandlers.ComicManager.DOWNLOAD;
import static es.schooleando.xkcdcomichandlers.ComicManager.ERROR;
import static es.schooleando.xkcdcomichandlers.ComicManager.LOAD_IMAGE;
import static es.schooleando.xkcdcomichandlers.ComicManager.PROGRESS;

public class DownloadHandler extends Handler {

    private WeakReference<Context> contexto;
    private WeakReference<ComicManager.ImageHandler> imageHandler;

    public DownloadHandler(Looper looper,Context context,ComicManager.ImageHandler imageHandler) {
        super(looper);
        contexto = new WeakReference<>(context);
        this.imageHandler = new WeakReference<>(imageHandler);
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case(DOWNLOAD):
        //               nos descargará una imagen y una vez descargada enviaremos un mensaje LOAD_IMAGE al UI Thread indicando
        //               la URI del archivo descargado.
        //               También enviaremos mensajes PROGRESS al UI Thread indicando el porcentaje de progreso, si hay.
        //               Enviaremos mensajes ERROR, en caso de que haya un error en la conexión, descarga, etc...
                Message ms = imageHandler.get().obtainMessage();
                ms.what=ERROR;

                String sUrl = (String)msg.obj;

                ConnectivityManager cm = (ConnectivityManager) contexto.get().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnected()) {
                    HttpURLConnection con = null;
                    URL url;
                    try {
                        StringBuilder result = new StringBuilder();
                        url = new URL(sUrl);
                        con = (HttpURLConnection) url.openConnection();
                        String redirect = con.getHeaderField("Location");
                        if (redirect != null){
                            con = (HttpURLConnection)new URL(redirect).openConnection();
                        }
                        con.connect();

                        if (con.getResponseCode()==200 || con.getResponseCode()==201) {

                            InputStream in = new BufferedInputStream(con.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                result.append(line);
                            }
                            con.disconnect();

                            JSONObject json = new JSONObject(result.toString());
                            //json.getInt("num");
                            String imageUrl = json.getString("img");

                            url = new URL(imageUrl);
                            con = (HttpURLConnection) url.openConnection();
                            con.setConnectTimeout(5000);
                            con.setReadTimeout(5000);
                            con.setRequestMethod("HEAD");
                            con.connect();

                            int size = con.getContentLength();
                            in = url.openStream();

                            ByteArrayOutputStream out = new ByteArrayOutputStream();

                            byte[] by = new byte[1024];

                            for (int i; (i = in.read(by)) != -1; ) {

                                out.write(by, 0, i);
                                Message m = imageHandler.get().obtainMessage();
                                m.what = PROGRESS;
                                if (size > 0) {
                                    m.obj = out.size() * 100 / size;
                                } else {
                                    m.obj = i * -1;
                                }
                                imageHandler.get().sendMessage(m);
                            }

                            File outputDir = contexto.get().getExternalCacheDir();
                            String[] data = imageUrl.split("/");
                            String[] f = data[data.length - 1].split("\\.");
                            File outputFile = File.createTempFile(f[0], "." + f[1], outputDir);
                            outputFile.deleteOnExit();

                            FileOutputStream fos = new FileOutputStream(outputFile);
                            fos.write(out.toByteArray());

                            ms.obj = outputFile.getPath() + "|" + json.getInt("num");
                            ms.what = LOAD_IMAGE;

                            out.close();
                            in.close();
                        }else{
                            ms.obj="Code "+con.getResponseCode()+ ", "+con.getResponseMessage();
                        }

                    } catch (MalformedURLException e) {
                        ms.obj="url invalida: "+e.getMessage();
                    } catch (SocketTimeoutException e) {
                        ms.obj="Tiempo excesivo: "+e.getMessage();
                    } catch (IOException e) {
                        ms.obj="Error de lectura: "+e.getMessage();
                    } catch (JSONException e) {
                        ms.obj="Json incorrecto: "+e.getMessage();
                    } catch (Exception e) {
                        ms.obj="Excepción: "+e.getMessage();
                    } finally {
                        if (con != null) con.disconnect();
                    }
                } else {
                    ms.obj="No hay conexión";
                }
                imageHandler.get().sendMessage(ms);
                break;

        }


        // No es necesario procesar Runnables luego no llamamos a super
        //super.handleMessage(msg);
    }
}

package es.schooleando.xkcdcomichandlers;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by ruben on 5/01/17.
 */

public class ComicManager {

    private HandlerThread downloadHandlerThread;
    private static DownloadHandler downloadHandler; // Funcionará asociado al Worker Thread (HandlerThread)
    private ImageHandler imageHandler;              // Funcionará asociado al UI Thread
    private static boolean timerActive;             // Controlamos si el timer está activo o no
    private static int seconds;                     // Segundos del timer

    private static int maxComic=-1;

    private static WeakReference<Activity> activity;
    private static ImageView iv;
    private static ProgressBar pb;
    private Button btnTimer;
    private Button btnSalir;

    public static final int LOAD_IMAGE = 0;
    public static final int DOWNLOAD = 1;
    public static final int PROGRESS = 2;
    public static final int ERROR = 3;

    public ComicManager(final Activity activity, int secondsTimer){
        this.activity = new WeakReference<>(activity);
        
        // Aquí inicializamos el HandlerThread y el DownloadHandler usando el Looper de HandlerThread
        downloadHandlerThread = new HandlerThread("ComicManagerHandlerThread");
        downloadHandlerThread.start();
        // Inicializamos la imageHandler a partir de la static inner class definida posteriormente, asociandola al UI Looper
        imageHandler = new ImageHandler();// new Handler(Looper.getMainLooper());
        downloadHandler = new DownloadHandler(downloadHandlerThread.getLooper(),activity,imageHandler);

        // Inicializamos la temporalización
        startTimer(secondsTimer);

        //recoger variables del layout
        pb = (ProgressBar)this.activity.get().findViewById(R.id.progressBar);

        iv=(ImageView)this.activity.get().findViewById(R.id.imageView);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadComic();
            }
        });

        btnTimer=(Button)this.activity.get().findViewById(R.id.btnTimer);
        btnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnTimer.getText().toString().contains("Parar")){
                    btnTimer.setText("Iniciar");
                    stop();
                }else{
                    btnTimer.setText("Parar");
                    start();
                }
            }
        });

        btnSalir=(Button)this.activity.get().findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                activity.finish();
                System.exit(0);
            }
        });

        start();
    }

    public void start() {
        // Arrancamos el HandlerThread.
        if (!downloadHandlerThread.isAlive()) downloadHandlerThread.start();
        // llamamos a downloadComic una vez
        downloadComic();
    }

    public void stop() {
        pb.setVisibility(View.INVISIBLE);
        // Enviamos un Toast de que se está parando la aplicación
       Toast.makeText(activity.get(),"Parando las descargas",Toast.LENGTH_LONG).show();
        // Desactivamos el timer para que evite enviar mensajes a un HandlerThread que ya no existirá.
        // Paramos el HandlerThread, limpiando su cola de mensajes y esperando a que acabe su trabajo activo si lo tiene
        stopTimer();
    }

    public static void downloadComic() {
        pb.setVisibility(View.VISIBLE);
        // enviamos un mensaje para descargar un Comic (cuando pulsemos sobre el imageView)
        Message msg = downloadHandler.obtainMessage();
        msg.what = DOWNLOAD;
        if (maxComic<0) {
            msg.obj = "http://xkcd.com/info.0.json";
        }else{
            int rndNum = ThreadLocalRandom.current().nextInt(1, maxComic + 1);
            msg.obj = "http://xkcd.com/"+ rndNum + "/info.0.json";
        }
        downloadHandler.sendMessageDelayed(msg,seconds*1000);
    }

    public void startTimer(int segundos) {
        // activamos el timer y configuramos el timer
        timerActive=true;
        seconds=segundos;

    }

    public void stopTimer() {
        // desactivamos el timer
        // limpiamos mensajes de Timer en el HandlerThread
        timerActive=false;
        downloadHandler.removeMessages(DOWNLOAD);

        //downloadHandlerThread.quitSafely();
    }

    // Interfaz privada

    // Aquí declararemos una static inner class Handler
    public static class ImageHandler extends Handler {

        public void handleMessage(Message msg) {
                switch(msg.what) {
                     case(LOAD_IMAGE):
                         //    Obtenemos la URI del archivo temporal y cargamos el imageView
                         String[] datos = ((String)msg.obj).split("\\|");
                         if (maxComic<0) {
                             maxComic=Integer.parseInt(datos[1]);
                         }//si el comic es el último lo asignamos para obtener números aleatorios
                         String ruta=datos[0];
                         pb.setVisibility(View.INVISIBLE);
                         File f =new File(ruta);
                         if (f.exists()) {
                             iv.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
                         }else {
                             Toast.makeText(activity.get(),"No existe el fichero descargado en " + ruta,Toast.LENGTH_SHORT).show();
                         }
                         //     si está activo el timer posteriormente enviaremos un mensaje retardado de DOWNLOAD_COMIC al HandlerThread, solo si está activo el Timer.
                         if (timerActive) {
                             downloadComic();
                         }
                         break;

                     case(PROGRESS):
                         //     actualizaremos el progressBar
                         int progreso = (int)msg.obj;
                         pb.setIndeterminate(progreso < 0);
                         pb.setProgress(progreso);
                         break;

                     case(ERROR):
                         //     mostraremos un Toast del error. Cancelamos el Timer para evitar errores posteriores
                         pb.setVisibility(View.INVISIBLE);
                         Toast.makeText(activity.get(),(String)msg.obj,Toast.LENGTH_LONG).show();
                         if (timerActive) {
                             downloadComic();
                         }
                         break;

                     default:
                         //     Importante procesar el resto de mensajes:
                         super.handleMessage(msg);
                }
         }
    }
}

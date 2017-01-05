package es.schooleando.xkcdcomichandlers;

import android.os.Handler;
import android.os.HandlerThread;
import android.widget.ImageView;

/**
 * Created by ruben on 5/01/17.
 */

public class ComicManager {
    private HandlerThread downloadHandlerThread;
    private DownloadHandler downloadHandler; // Funcionará asociado al Worker Thread (HandlerThread)
    private Handler imageHandler;            // Funcionará asociado al UI Thread
    private boolean timerActive;             // Controlamos si el timer está activo o no
    private int seconds;                     // Segundos del timer

    public ComicManager(ImageView imageView, int secondsTimer) {
        // Aquí inicializamos el HandlerThread y el DownloadHandler usando el Looper de HandlerThread

        // Inicializamos la imageHandler a partir de la static inner class definida posteriormente, asociandola al UI Looper

        // Inicializamos la temporalización
    }

    public void start() {
        // Arrancamos el HandlerThread.

        // llamamos a downloadComic una vez

    }

    public void stop() {
        // Enviamos un Toast de que se está parando la aplicación
        // Desactivamos el timer para que evite enviar mensajes a un HandlerThread que ya no existirá.
        // Paramos el HandlerThread, limpiando su cola de mensajes y esperando a que acabe su trabajo activo si lo tiene

    }

    public void downloadComic() {
        // enviamos un mensaje para descargar un Comic (cuando pulsemos sobre el imageView)
    }

    public void startTimer(int segundos) {
        // activamos el timer y configuramos el timer

    }

    public void stopTimer() {
        // desactivamos el timer
        // limpiamos mensajes de Timer en el HandlerThread

    }

    // Interfaz privada



    // Aquí declararemos una static inner class Handler
    // ..... class ImageHandler extends Handler {
    //
    //     public void handleMessage(Message msg) {
    //        switch(msg.what) {
              // case(LOAD_IMAGE):
              //    Obtenemos la URI del archivo temporal y cargamos el imageView
              //     si está activo el timer posteriormente enviaremos un mensaje retardado de DOWNLOAD_COMIC al HandlerThread, solo si está activo el Timer.
              // case(PROGRESS):
              //     actualizaremos el progressBar
              // case(ERROR):
              //     mostraremos un Toast del error. Cancelamos el Timer para evitar errores posteriores
              // default:
              //     Importante procesar el resto de mensajes:
              //     super.handleMessage(msg);
    //     }
}

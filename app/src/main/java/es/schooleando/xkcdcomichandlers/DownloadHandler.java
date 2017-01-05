package es.schooleando.xkcdcomichandlers;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class DownloadHandler extends Handler {
    public DownloadHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        //switch(msg.what) {
        //    case(DOWNLOAD):
        //               nos descargará una imagen y una vez descargada enviaremos un mensaje LOAD_IMAGE al UI Thread indicando
        //               la URI del archivo descargado.
        //               También enviaremos mensajes PROGRESS al UI Thread indicando el porcentaje de progreso, si hay.
        //               Enviaremos mensajes ERROR, en caso de que haya un error en la conexión, descarga, etc...
        //        break;
        //}


        // No es necesario procesar Runnables luego no llamamos a super
        //super.handleMessage(msg);
    }
}

package labs.dstvdm.com.hotvee;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by paul on 2015/10/16.
 */
public class Program implements Serializable {

    private Bitmap image;
    private String synopsis;
    private String title;

    public Program(Bitmap image, String synopsis, String title) {
        this.image = image;
        this.synopsis = synopsis;
        this.title = title;
    }
}

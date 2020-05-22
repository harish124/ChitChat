package print;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.shashank.sony.fancytoastlib.FancyToast;

public class Print {
    private Context ctx;
    public Print(Context ctx)
    {
        this.ctx=ctx;
    }
    public void sprintf(Object o)
    {
        FancyToast.makeText(ctx,""+o, FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();
    }
    public void fprintf(Object o)
    {
        FancyToast.makeText(ctx,""+o, FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
        Log.d("Error:",""+o);
    }
}

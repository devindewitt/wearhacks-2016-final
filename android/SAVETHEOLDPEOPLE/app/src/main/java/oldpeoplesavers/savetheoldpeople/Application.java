package oldpeoplesavers.savetheoldpeople;

import com.firebase.client.Firebase;

/**
 * Created by Tony on 16-03-26.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Firebase.setAndroidContext(this);

    }
}

package app.openconnect.remote.data;

import android.content.Context;

import androidx.annotation.NonNull;

// I need some object ;-)
public class Global extends GlobalHelper {
    public Global(Context context) {
        setMainApplication(context);
    }

    public Context getMainApplication() {
        return this.mainApplication;
    }

    public void setMainApplication(@NonNull Context mainApplication) {
        this.mainApplication = mainApplication;
    }

}
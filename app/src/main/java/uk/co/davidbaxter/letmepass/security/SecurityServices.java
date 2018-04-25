package uk.co.davidbaxter.letmepass.security;

import android.content.Context;
import android.support.annotation.Nullable;

public class SecurityServices {

    private static SecurityServices instance;
    private final PasswordGeneratorService passwordGeneratorService;

    public SecurityServices(Context context) {
        this.passwordGeneratorService = new PasswordGeneratorService(context);
    }

    public PasswordGeneratorService getPasswordGeneratorService() {
        return passwordGeneratorService;
    }

    public static @Nullable SecurityServices getInstance() {
        return instance;
    }

    public static SecurityServices getInstance(Context context) {
        if (instance == null)
            instance = new SecurityServices(context);
        return instance;
    }

}

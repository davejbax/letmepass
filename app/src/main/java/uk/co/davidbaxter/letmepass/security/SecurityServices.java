package uk.co.davidbaxter.letmepass.security;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * A singleton collection of security services, backed by the application context
 */
public class SecurityServices {

    private static SecurityServices instance;
    private final PasswordGeneratorService passwordGeneratorService;
    private final PasswordFlagsService passwordFlagsService;
    private final PasswordBreachService passwordBreachService;

    public SecurityServices(Context context) {
        this.passwordGeneratorService = new PasswordGeneratorService(context);
        this.passwordFlagsService = new PasswordFlagsService(context);
        this.passwordBreachService = new PasswordBreachService(context);
    }

    /**
     * Gets the {@link PasswordGeneratorService} instance
     * @return Password generator service
     */
    public PasswordGeneratorService getPasswordGeneratorService() {
        return passwordGeneratorService;
    }

    /**
     * Gets the {@link PasswordFlagsService} instance
     * @return Password flags service
     */
    public PasswordFlagsService getPasswordFlagsService() {
        return passwordFlagsService;
    }

    /**
     * Gets the {@link PasswordBreachService} instance
     * @return Password breach service
     */
    public PasswordBreachService getPasswordBreachService() {
        return passwordBreachService;
    }

    /**
     * Gets the current instance. Note that this may return null if the services have not been
     * initialized with getInstance
     * @return
     */
    public static @Nullable SecurityServices getInstance() {
        return instance;
    }

    public static SecurityServices initialize(Context appContext) {
        if (instance == null)
            instance = new SecurityServices(appContext);
        return instance;
    }

}

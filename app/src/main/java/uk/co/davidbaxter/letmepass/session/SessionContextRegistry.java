package uk.co.davidbaxter.letmepass.session;

public class SessionContextRegistry {

    private static SessionContext currentSessionContext;

    public static SessionContext getSessionContext() {
        return currentSessionContext;
    }

    public static void setSessionContext(SessionContext context) {
        currentSessionContext = context;
    }

    public static void discardSessionContext() {
        currentSessionContext = null;
    }

}

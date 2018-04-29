package uk.co.davidbaxter.letmepass.presentation;

import android.util.Pair;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.security.PasswordBreachService;

/**
 * An action that a view should take relating to the breach check functionality
 */
public enum BreachAction {
    /**
     * View should request internet permissions
     */
    REQUEST_PERMS,

    /**
     * View should show a dialog explaining what the breach check is
     */
    EXPLAIN,

    /**
     * View should display the number of breaches; parameter to this is the number of
     * breaches (integer)
     */
    SHOW_BREACHES,

    /**
     * View should display an error; parameter is a string resource ID for the error (integer)
     */
    SHOW_ERROR;

    /**
     * Forms a pair of breach action and any parameters that the action requires from the result
     * of a breach check carried out by the {@link PasswordBreachService}. This pair may be used
     * in LiveData, for example, where it can be consumed by the view.
     *
     * @param result Result of {@link PasswordBreachService}
     * @return Pair
     */
    public static Pair<BreachAction, Object> getPairFromBreachCheckResult(int result) {
        switch (result) {
            case PasswordBreachService.ERROR_SERVER_ERROR:
                // Connection failed -- server-side issue
                return Pair.create(BreachAction.SHOW_ERROR,
                        (Object) R.string.breach_server_error);
            case PasswordBreachService.ERROR_IO_EXCEPTION:
                // IO exception -- internet not connected?
                return Pair.create(BreachAction.SHOW_ERROR,
                        (Object) R.string.breach_io_exception);
            case PasswordBreachService.ERROR_UNKNOWN_ERROR:
                // Unknown error
                return Pair.create(BreachAction.SHOW_ERROR,
                        (Object) R.string.breach_unknown_error);
            case PasswordBreachService.ERROR_NO_PERMS:
                // Activity has to request the permissions for internet
                return Pair.create(BreachAction.REQUEST_PERMS, null);
            default:
                // We have a valid result! Relay to activity
                return Pair.create(BreachAction.SHOW_BREACHES, (Object)result);
        }
    }
}

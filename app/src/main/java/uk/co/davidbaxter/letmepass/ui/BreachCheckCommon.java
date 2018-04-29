package uk.co.davidbaxter.letmepass.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Pair;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.presentation.BreachAction;

/**
 * A collection of functions to produce the common response to a breach check carried out by a
 * viewmodel and/or the {@link uk.co.davidbaxter.letmepass.security.PasswordBreachService}.
 */
public class BreachCheckCommon {

    /**
     * Handles a breach check result in the standard way. This should run on the main thread.
     * <p>
     * This function will do one of the following things, depending on the passed value:
     * <ul>
     *     <li>
     *         <b>Launch a permissions request intent</b>, the result of which should be processed
     *         by the activity/fragment, if the breach action is {@link BreachAction#REQUEST_PERMS}.
     *     </li>
     *     <li>
     *         <b>Open a dialog explaining what a breach check is</b>, if the BreachAction is
     *         {@link BreachAction#EXPLAIN}
     *     </li>
     *     <li>
     *         <b>Open a dialog with an appropriate error message</b>, if the BreachAction is
     *         {@link BreachAction#SHOW_ERROR}
     *     </li>
     *     <li>
     *         <b>Open a dialog with the breach check result and appropriate message</b>, if the
     *         BreachAction is {@link BreachAction#SHOW_BREACHES}
     *     </li>
     * </ul>
     *
     * @param activity Activity to bind to
     * @param value Pair result of a breach check, of the action to carry out, and any parameters as
     *              a single object.
     * @param requestPermsId The request code to use for launching an intent to request permissions.
     *                       This should be handled in the activity/fragment's code to process
     *                       activity results.
     */
    public static void handleBreachCheck(Activity activity,
                                         Pair<BreachAction, Object> value,
                                         int requestPermsId) {
        if (value == null)
            return;

        switch (value.first) {
            case REQUEST_PERMS:
                // Request permissions to use the internet; this will launch an intent that returns
                // a result in the activity
                ActivityCompat.requestPermissions(
                        activity,
                        new String[] { Manifest.permission.INTERNET },
                        requestPermsId);
                break;
            case EXPLAIN:
                // Open a dialog explaining what 'breach check' is
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.breach_help_title)
                        .setMessage(R.string.breach_help_message)
                        .setNegativeButton(R.string.breach_help_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create()
                        .show();
                break;
            case SHOW_ERROR:
            case SHOW_BREACHES:
                int messageId;

                // If we're showing an error, message resource ID is the error string resource ID
                if (value.first == BreachAction.SHOW_ERROR)
                    messageId = (Integer) value.second;
                // If we're showing a result, show a breached/warning message for > 0 breaches
                else if ((Integer) value.second > 0)
                    messageId = R.string.breach_result_breached_message;
                // Otherwise, we have no breaches, show a message that the password is safe
                else
                    messageId = R.string.breach_result_safe_message;

                // If we're showing breaches, the format parameters for the message is the number
                // of breaches that the password appeared in (i.e. the pair's second value)
                Object[] messageFormat = value.first == BreachAction.SHOW_ERROR ?
                        new Object[] {} : new Object[] { value.second };

                // Build and show the dialog
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.breach_result_title)
                        .setMessage(activity.getString(messageId, messageFormat))
                        .setNegativeButton(R.string.breach_result_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create()
                        .show();

                break;
        }
    }

}

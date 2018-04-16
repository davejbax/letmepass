package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import uk.co.davidbaxter.letmepass.model.PasswordFlags;

public class CreationViewModel extends ViewModel {

    // Bound fields: these are set by the view, and can be read/set by us
    public MutableLiveData<String> masterPassword = new MutableLiveData<>();
    public MutableLiveData<String> masterPasswordAgain = new MutableLiveData<>(); // Re-entry of mp
    public MutableLiveData<String> dbName = new MutableLiveData<>();
    public MutableLiveData<Boolean> dbIsDefault = new MutableLiveData<>();

    // Observable fields: the view observes these fields and updates accordingly. For instance,
    // to disable a portion of the UI when a different option is selected.
    public MutableLiveData<Boolean> cloudChecked = new MutableLiveData<>();
    public MutableLiveData<String> cloudLocation = new MutableLiveData<>();
    public MutableLiveData<String> deviceLocation = new MutableLiveData<>();
    public MutableLiveData<String> keyfileLocation = new MutableLiveData<>();
    public MutableLiveData<PasswordFlags> passwordFlags = new MutableLiveData<>();

    public CreationViewModel() {
        this.cloudChecked.setValue(true);
    }

    public void setCloudChecked(boolean cloudChecked) {
        this.cloudChecked.postValue(cloudChecked);
    }

    public void onOpenCloud() {
        // TODO
    }

    public void onOpenDevice() {
        // TODO
    }

    public void onBreachCheck() {
        // TODO
    }

    public void onBreachCheckHelp() {
        // TODO
    }

    public void onGenKeyfile() {
        // TODO
    }

    public void onMpChanged(CharSequence s, int start, int before, int count) {
        // TODO: temporary code here to give a prototype; actual code will call a Service
        PasswordFlags pwdFlags = passwordFlags.getValue();
        if (pwdFlags == null)
            pwdFlags = new PasswordFlags();

        generateFlags(pwdFlags, s.toString());

        // Re-set passwordFlags LiveData so UI updates
        passwordFlags.setValue(pwdFlags);
    }

    public static void generateFlags(PasswordFlags flags, String password) {
        // TODO: remove this temp function; will be implemented by a service in practice
        // Working booleans from which to set the password flags
        boolean numbers = false;
        boolean upper = false;
        boolean lower = false;
        boolean symbols = false;

        // Iterate over characters to check whether containing certain chars
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if ((c >= 32 && c <= 47)
                    || (c >= 58 && c <= 64)
                    || (c >= 91 && c <= 96)
                    || (c >= 123 && c <= 126))
                symbols = true;
            if (c >= 97 && c <= 122)
                lower = true;
            if (c >= 65 && c <= 90)
                upper = true;
            if (c >= 48 && c <= 57)
                numbers = true;
        }

        // Set good length
        flags.goodLength = password.length() >= 12;

        // Set mixed chars
        flags.hasMixedChars = upper && lower && numbers;

        // Set symbols
        flags.hasSymbols = symbols;

        // Set blacklisted
        flags.notBlacklisted = true;
    }

}

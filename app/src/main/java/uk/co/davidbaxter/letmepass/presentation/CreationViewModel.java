package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

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
    public MutableLiveData<DummyPasswordFlagsClass> passwordFlags = new MutableLiveData<>();

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
        DummyPasswordFlagsClass pwdFlags = passwordFlags.getValue();
        if (pwdFlags == null)
            pwdFlags = new DummyPasswordFlagsClass();

        // Working booleans from which to set the password flags
        boolean numbers = false;
        boolean upper = false;
        boolean lower = false;
        boolean symbols = false;

        // Iterate over characters to check whether containing certain chars
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
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
        pwdFlags.goodLength = s.length() >= 12;

        // Set mixed chars
        pwdFlags.hasMixedChars = upper && lower && numbers;

        // Set symbols
        pwdFlags.hasSymbols = symbols;

        // Re-set passwordFlags LiveData so UI updates
        passwordFlags.setValue(pwdFlags);
    }

    public class DummyPasswordFlagsClass {
        public boolean goodLength,
                hasSymbols,
                hasMixedChars, // Contains uppercase, lowercase, and numbers
                notBlacklisted;

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DummyPasswordFlagsClass))
                return false;

            // We are only equal if we are the same type; compare all fields to ensure equality
            DummyPasswordFlagsClass otherFlags = (DummyPasswordFlagsClass) other;
            return otherFlags.goodLength == goodLength
                    && otherFlags.hasSymbols == hasSymbols
                    && otherFlags.hasMixedChars == hasMixedChars
                    && otherFlags.notBlacklisted == notBlacklisted;
        }
    }
}

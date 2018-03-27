package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class DecryptionViewModel extends ViewModel {

    public MutableLiveData<String> masterPassword = new MutableLiveData<>();
    public MutableLiveData<Boolean> useKeyfile = new MutableLiveData<>();

    public void onDecrypt() {
        this.masterPassword.postValue("Hello!");
    }

}

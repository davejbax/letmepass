package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class IntroViewModel extends ViewModel {

    private MutableLiveData<String> toastText = new MutableLiveData<>();

    public LiveData<String> getToastText() {
        return toastText;
    }

    public void onNewDatabase() {
        toastText.postValue("New database clicked!");
    }

    public void onLoadCloud() {
        toastText.postValue("Load cloud clicked!");
    }

    public void onLoadDevice() {
        toastText.postValue("Load device clicked!");
    }

}

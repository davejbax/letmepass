package uk.co.davidbaxter.letmepass.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;

public class IntroViewModel extends ViewModel {

    private SingleLiveEvent<Action> action = new SingleLiveEvent<>();

    public LiveData<Action> getAction() {
        return action;
    }

    public void onNewDatabase() {
        action.postValue(Action.NEW_DATABASE);
    }

    public void onLoadCloud() {
        action.postValue(Action.LOAD_CLOUD);
    }

    public void onLoadDevice() {
        action.postValue(Action.LOAD_DEVICE);
    }

    public enum Action {
        NEW_DATABASE,
        LOAD_CLOUD,
        LOAD_DEVICE
    }

}

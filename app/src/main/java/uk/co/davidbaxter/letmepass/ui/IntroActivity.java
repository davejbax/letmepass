package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ActivityIntroBinding;
import uk.co.davidbaxter.letmepass.presentation.IntroViewModel;
import uk.co.davidbaxter.letmepass.storage.DataStore;
import uk.co.davidbaxter.letmepass.storage.impl.DriveStorageService;
import uk.co.davidbaxter.letmepass.storage.impl.FileStorageService;

public class IntroActivity extends AppCompatActivity {

    private static final int REQUEST_OPEN_FILE = 1;
    private static final int REQUEST_OPEN_CLOUD = 2;
    private static final int REQUEST_CLOUD_SIGN_IN_AND_OPEN_CLOUD = 3;

    private IntroViewModel viewModel;
    private FileStorageService fileStorageService;
    private DriveStorageService driveStorageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the layout using the data binding utility library: this allows for binding the
        // viewmodel, so that buttons etc. in the layout can invoke viewmodel methods, and fields
        // (such as text fields) can be bound to the viewmodel
        ActivityIntroBinding binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_intro);
        binding.setLifecycleOwner(this);

        // Setup viewmodel
        this.viewModel = ViewModelProviders.of(this).get(IntroViewModel.class);
        binding.setViewModel(this.viewModel);

        // Create our storage services
        this.fileStorageService = new FileStorageService(this);
        this.driveStorageService = new DriveStorageService(this);
        this.driveStorageService.onCreate();

        // Setup viewmodel events
        this.setupEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_FILE:
                if (resultCode == RESULT_OK) {
                    // Process result and relay to viewmodel
                    DataStore store = fileStorageService.onFilePickerResult(data);
                    viewModel.onDatabaseOpened(store);
                } /* TODO: not OK handling? */
                break;
            case REQUEST_CLOUD_SIGN_IN_AND_OPEN_CLOUD:
                if (resultCode == RESULT_OK) {
                    try {
                        driveStorageService.onSignInResult(data);
                        // Process result and then launch open dialog
                        openDriveFile();
                    } catch (Exception e) {
                        showSnackbar(R.string.drive_sign_in_failed);
                        Log.e(getClass().getSimpleName(), "Failed to sign into Drive", e);
                    }
                }
                break;
            case REQUEST_OPEN_CLOUD:
                if (resultCode == RESULT_OK) {
                    // Process result and relay to viewmodel
                    DataStore store = driveStorageService.onOpenFileResult(data);
                    viewModel.onDatabaseOpened(store);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSnackbar(int stringResId) {
        Snackbar.make(findViewById(android.R.id.content), stringResId, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void setupEvents() {
        // Observe for changes to toast text
        this.viewModel.getAction().observe(this, new Observer<IntroViewModel.Action>() {
            @Override
            public void onChanged(@Nullable IntroViewModel.Action action) {
                Class<?> activityClass = null;

                // Sanity check
                if (action == null)
                    return;

                Intent intent;

                switch (action) {
                    case NEW_DATABASE:
                        intent = new Intent(IntroActivity.this, CreationActivity.class);
                        startActivity(intent);
                        break;
                    case LOAD_CLOUD:
                        if (!driveStorageService.isSignedIn()) {
                            intent = driveStorageService.getSignInIntent();
                            startActivityForResult(intent, REQUEST_CLOUD_SIGN_IN_AND_OPEN_CLOUD);
                        } else {
                            openDriveFile();
                        }
                        break;
                    case LOAD_DEVICE:
                        intent = fileStorageService.getFilePickerIntent();
                        startActivityForResult(intent, REQUEST_OPEN_FILE);
                        break;
                    case LAUNCH_DECRYPTION:
                        intent = new Intent(IntroActivity.this, DecryptionActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private void openDriveFile() {
        driveStorageService.getOpenFileIntent(
                new DriveStorageService.Callback<IntentSender>() {
            @Override
            public void onSuccess(IntentSender result) {
                try {
                    startIntentSenderForResult(
                            result,
                            REQUEST_OPEN_CLOUD,
                            null,
                            0, 0, 0
                    );
                } catch (IntentSender.SendIntentException e) {
                    Log.e(IntroActivity.class.getSimpleName(), "Send open cloud intent failed", e);
                    showSnackbar(R.string.intro_drive_error);
                }
            }

            @Override
            public void onFailure(Exception e) {
                showSnackbar(R.string.intro_drive_error);
            }
        });
    }

}

package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.io.IOException;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.presentation.CreationViewModel;
import uk.co.davidbaxter.letmepass.storage.impl.DriveStorageService;
import uk.co.davidbaxter.letmepass.storage.impl.FileDataStore;
import uk.co.davidbaxter.letmepass.storage.impl.FileStorageService;

public class CreationActivity extends AppCompatActivity implements StepperLayout.StepperListener {

    private static final String TAG = "CreationActivity";
    private static final int REQUEST_SIGN_IN_AND_CREATE = 1;
    private static final int REQUEST_CREATE = 2;

    private CreationViewModel viewModel;
    private StepperLayout stepper;
    private DriveStorageService driveStorageService;
    private FileStorageService fileStorageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation);

        this.viewModel = ViewModelProviders.of(this).get(CreationViewModel.class);

        // Setup stepper layout
        this.setupStepper();

        // Setup Drive
        this.setupDriveService();

        // Setup device storage
        this.setupFileService();

        // Setup events
        this.setupEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SIGN_IN_AND_CREATE:
                // If result was not successful, show error
                if (resultCode != RESULT_OK) {
                    showError(resultCode == RESULT_CANCELED ? R.string.drive_sign_in_cancelled
                            : R.string.drive_sign_in_failed);
                    return;
                // No errors, continue with creating drive file
                } else {
                    driveStorageService.onSignInResult(data);
                    this.createDriveFile();
                }

                // Hide progress if we were waiting on this intent
                this.stepper.hideProgress();
                break;
            case REQUEST_CREATE:
                // If result was not successful, show error
                if (resultCode != RESULT_OK) {
                    showError(resultCode == RESULT_CANCELED ? R.string.drive_create_cancelled
                            : R.string.drive_create_failed);
                // No errors, relay result of creation to viewmodel
                } else {
                    viewModel.onDriveCreated(driveStorageService.onCreateFileResult(data));
                }

                // Hide progress if we were waiting on this intent
                this.stepper.hideProgress();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCompleted(View completeButton) {
        // TODO: disable back navigation here too
        this.stepper.setTabNavigationEnabled(false);
        this.stepper.showProgress(getString(R.string.creation_creating_db));
        this.viewModel.onComplete();
    }

    @Override
    public void onBackPressed() {
        // Don't do anything if we're in progress (creating DB)
        if (stepper.isInProgress())
            return;

        if (this.stepper.getCurrentStepPosition() > 0)
            this.stepper.onBackClicked();
        else {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.creation_close_dialog_title)
                    .setMessage(R.string.creation_close_dialog_message)
                    .setPositiveButton(R.string.creation_close_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    viewModel.onCancel();
                                    CreationActivity.super.onBackPressed();
                                }
                            })
                    .setNegativeButton(R.string.creation_close_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                    .create();
            dialog.show();
        }
    }

    @Override
    public void onError(VerificationError verificationError) {}

    @Override
    public void onStepSelected(int newStepPosition) {}

    @Override
    public void onReturn() {}

    private void showError(int stringResId) {
        Snackbar.make(this.stepper, stringResId, Snackbar.LENGTH_LONG).show();
    }

    private void setupStepper() {
        // Create new step adapter to handle creating step fragments, and set this in our view
        this.stepper = (StepperLayout) findViewById(R.id.creationStepper);
        this.stepper.setAdapter(new CreationStepAdapter(
                getSupportFragmentManager(),
                this));
        this.stepper.setListener(this);
    }

    private void setupDriveService() {
        this.driveStorageService = new DriveStorageService(getApplicationContext());
        this.driveStorageService.onCreate();
    }

    private void setupFileService() {
        this.fileStorageService = new FileStorageService(getApplicationContext());
    }

    private void setupEvents() {
        // Listen for signals by viewmodel to launch 'choose file' dialogs/activities for both
        // cloud and device storage
        this.viewModel.getChooseStorageEvent().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isDrive) {
                // If we're choosing a drive file, call the drive storage service
                if (isDrive != null && isDrive) {
                    // Show a progress spinner while we delegate control to Google Drive
                    stepper.showProgress("");

                    // Sign in if necessary, then create the file
                    if (!driveStorageService.isSignedIn()) {
                        startActivityForResult(
                                driveStorageService.getSignInIntent(),
                                REQUEST_SIGN_IN_AND_CREATE);
                    // Create the drive file directly if we're signed in
                    } else {
                        createDriveFile();
                    }
                // User wants to choose a device file
                } else {
                    createDeviceFile();
                }
            }
        });

        this.viewModel.getCreateResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean successful) {
                if (successful != null && successful) {
                    Intent intent = new Intent(CreationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    stepper.setTabNavigationEnabled(true);
                    stepper.hideProgress();
                    Toast.makeText(CreationActivity.this,
                            R.string.creation_error_creation_failed,
                            Toast.LENGTH_LONG)
                        .show();
                }
            }
        });
    }

    /** Opens a dialog to create a device file and handles its submission */
    private void createDeviceFile() {
        // Inflate our layout to show in the dialog
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_file_create_dialog, null, false);
        final TextInputEditText edit = view.findViewById(R.id.editFileName);

        // Create an AlertDialog allowing a user to enter their filename
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.creation_dialog_create_file)
                .setPositiveButton(R.string.creation_dialog_ok, null) // See below: this is a hack
                .setNegativeButton(R.string.creation_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Just close the dialog
                        dialog.cancel();
                    }
                })
                .setView(view);

        // Show the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // NOW, we set the positive button callback: we do this because we don't want the dialog to
        // dismiss when it shouldn't! By default, it will dismiss when clicking the positive button.
        // We want to stop this for purposes of validation: if the filename is empty, do not dismiss
        // when clicking the positive button.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String fileName = edit.getText().toString();

                // Validate the filename: it should not be empty! Do not dismiss dialog in this case
                if (fileName.isEmpty()) {
                    edit.setError(getString(R.string.creation_error_no_filename));
                    return;
                } else if (fileStorageService.fileExists(fileName)) {
                    edit.setError(getString(R.string.creation_error_file_exists));
                    return;
                } else if (!fileStorageService.isFileNameValid(fileName)) {
                    edit.setError(getString(R.string.creation_error_filename_invalid));
                    return;
                }

                // Filename is not empty; try creating the store from it.
                try {
                    // Get the store from the service
                    FileDataStore store =
                            fileStorageService.createStoreFromFilename(fileName);

                    // Update our viewmodel with the changes
                    viewModel.onFileCreated(store);

                    // Close this dialog if all went well
                    dialog.dismiss();
                } catch (IOException e) {
                    // We got an IOException while creating the store! Not much we can
                    // do other than display this error to the user (dialog not dismissed)
                    edit.setError(getString(R.string.creation_error_io_error));
                }
            }

        });
    }

    /** Opens a dialog to choose & create a file with Drive by launching an activity for result */
    private void createDriveFile() {
        // Create and, if successfully created, launch a create file intent to allow a user to
        // choose a drive file to create (plus touching the file).
        driveStorageService.getCreateFileIntent(
            getString(R.string.creation_choose_cloud_dialog_title),
            getString(R.string.database_filename_default),
            new DriveStorageService.Callback<IntentSender>() {
                @Override
                public void onSuccess(IntentSender sender) {
                    try {
                        // Start the create file intent
                        startIntentSenderForResult(
                            sender,         // Intent sender
                            REQUEST_CREATE, // Request code
                            null,           // IntentSender does not need intent param
                            0, 0, 0         // Flags
                        );
                    } catch (Exception e) {
                        Log.wtf(TAG, "Exception while starting intent sender for Drive", e);
                        showError(R.string.drive_internal_error);
                        stepper.hideProgress();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // We failed to create the intent(?!); show internal error
                    showError(R.string.drive_internal_error);
                    stepper.hideProgress();
                }
            }
        );
    }

}

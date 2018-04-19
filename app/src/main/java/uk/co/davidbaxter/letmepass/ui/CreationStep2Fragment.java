package uk.co.davidbaxter.letmepass.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Pair;

import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentCreationStep2Binding;
import uk.co.davidbaxter.letmepass.presentation.CreationViewModel;

public class CreationStep2Fragment extends CreationStepFragmentBase<FragmentCreationStep2Binding>
        implements BlockingStep {

    @Override
    protected final int getLayoutId() {
        return R.layout.fragment_creation_step2;
    }

    @Override
    protected void initDataBinding(FragmentCreationStep2Binding binding) {
        binding.setViewModel(viewModel);
    }

    @Override
    protected int getStepId() {
        return CreationViewModel.ID_STEP_2;
    }

    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        // Ask the viewmodel to generate a blocking dialog: a dialog asking the user whether they
        // really want to continue, essentially. If we don't have one, continue.
        Pair<Integer, Integer> dialogText = viewModel.generateBlockingDialog(getStepId());
        if (dialogText != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(dialogText.first)
                    .setMessage(dialogText.second)
                    .setPositiveButton(R.string.creation_dialog_continue,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            callback.goToNextStep();
                        }
                    })
                    .setNegativeButton(R.string.creation_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            // Show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        // Continue instantly if no need to show a blocking dialog
        } else {
            callback.goToNextStep();
        }
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        callback.complete();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }
}

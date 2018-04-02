package uk.co.davidbaxter.letmepass.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import uk.co.davidbaxter.letmepass.R;

public class CreationStepAdapter extends AbstractFragmentStepAdapter {

    public CreationStepAdapter(@NonNull FragmentManager fm, @NonNull Context context) {
        super(fm, context);
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0: return new CreationStep1Fragment();
            case 1: return new CreationStep2Fragment();
            case 2: return new CreationStep3Fragment();
            case 3: return new CreationStep4Fragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(int position) {
        // Set title to title string, formatted with position + 1 (positions start at 0)
        return new StepViewModel.Builder(this.context)
                .setTitle(this.context.getString(R.string.creation_step_title, position + 1))
                .create();
    }
}

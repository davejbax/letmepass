package uk.co.davidbaxter.letmepass.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.stepstone.stepper.StepperLayout;

import uk.co.davidbaxter.letmepass.R;

public class CreationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation);

        // Setup stepper layout
        this.setupStepper();
    }

    private void setupStepper() {
        // Create new step adapter to handle creating step fragments, and set this in our view
        StepperLayout stepper = (StepperLayout) findViewById(R.id.creationStepper);
        stepper.setAdapter(new CreationStepAdapter(
                getSupportFragmentManager(),
                this));
    }

}

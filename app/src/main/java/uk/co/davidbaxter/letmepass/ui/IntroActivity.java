package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ActivityIntroBinding;
import uk.co.davidbaxter.letmepass.presentation.IntroViewModel;

public class IntroActivity extends AppCompatActivity {

    private IntroViewModel viewModel;

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

        // Setup viewmodel events
        this.setupEvents();
    }

    private void setupEvents() {
        // Observe for changes to toast text
        this.viewModel.getAction().observe(this, new Observer<IntroViewModel.Action>() {
            @Override
            public void onChanged(@Nullable IntroViewModel.Action action) {
                Class<?> activityClass = null;

                switch (action) {
                    case NEW_DATABASE:
                        activityClass = CreationActivity.class;
                        break;
                    case LOAD_CLOUD:
                        // TODO: change this; this is for demonstration purposes
                        activityClass = DecryptionActivity.class;
                        break;
                    case LOAD_DEVICE:
                        // TODO: change this; this is for demonstration purposes
                        activityClass = MainActivity.class;
                        break;
                }

                Intent intent = new Intent(IntroActivity.this, activityClass);
                IntroActivity.this.startActivity(intent);
            }
        });
    }

}

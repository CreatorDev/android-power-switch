package com.imgtec.creator.petunia.presentation;

import android.os.Handler;

import com.imgtec.creator.petunia.app.ApplicationComponent;
import com.imgtec.creator.petunia.presentation.fragments.ChooseDeviceFragment;
import com.imgtec.creator.petunia.presentation.fragments.LoginFragment;
import com.imgtec.creator.petunia.presentation.fragments.RelayToggleFragment;
import com.imgtec.creator.petunia.presentation.fragments.SplashFragment;
import com.imgtec.creator.petunia.presentation.utils.ToolbarHelper;
import com.imgtec.di.HasComponent;
import com.imgtec.di.PerActivity;

import javax.inject.Named;

import dagger.Component;

@PerActivity
@Component(
    dependencies = ApplicationComponent.class,
    modules = {
        ActivityModule.class
    }
)
public interface ActivityComponent {

    class Initializer {

    private Initializer() {}

    static ActivityComponent init(MainActivity activity) {
      return DaggerActivityComponent
          .builder()
          .applicationComponent(((HasComponent<ApplicationComponent>) activity.getApplicationContext()).getComponent())
          .activityModule(new ActivityModule(activity))
          .build();
    }
  }

  void inject(MainActivity activity);
  void inject(SplashFragment fragment);
  void inject(LoginFragment fragment);
  void inject(ChooseDeviceFragment fragment);
  void inject(RelayToggleFragment fragment);

  ToolbarHelper getToolbarHelper();
}

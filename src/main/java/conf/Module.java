package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import controllers.DogparkController;

@Singleton
public class Module extends AbstractModule {

	@Override
	protected void configure() {
		bind(DogparkController.class);
	}

}

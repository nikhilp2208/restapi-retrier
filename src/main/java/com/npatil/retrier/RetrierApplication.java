package com.npatil.retrier;

import com.google.inject.Stage;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by nikhil.p on 31/03/16.
 */
public class RetrierApplication extends Application<RetrierConfiguration> {

    public static void main(String[] args) throws Exception {
        new RetrierApplication().run(args);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void initialize(Bootstrap<RetrierConfiguration> bootstrap) {

        bootstrap.addBundle(GuiceBundle.<RetrierConfiguration>newBuilder()
                .enableAutoConfig(getClass().getPackage().getName())
                .addModule(new RetrierModule())
                .setConfigClass(RetrierConfiguration.class)
                .build(Stage.DEVELOPMENT));
    }

    @Override
    public void run(RetrierConfiguration configuration, Environment environment) throws Exception {

    }
}

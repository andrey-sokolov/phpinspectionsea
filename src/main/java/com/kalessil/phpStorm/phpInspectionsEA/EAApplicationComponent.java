package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.kalessil.phpStorm.phpInspectionsEA.utils.analytics.AnalyticsUtil;
import com.wyday.turboactivate.TurboActivate;
import com.wyday.turboactivate.TurboActivateException;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.jetbrains.annotations.NotNull;

public class EAApplicationComponent implements ApplicationComponent {
    private boolean updated;
    private boolean updateNotificationShown;
    private TurboActivate limelm;

    @NotNull
    public static EAApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(EAApplicationComponent.class);
    }

    /* TODO: util */
    private void initLicensing() {
        final Application application = ApplicationManager.getApplication();
        final boolean needsLicense    = !application.isEAP() && !application.isHeadlessEnvironment();
        if (needsLicense) {
            try {
                limelm = new TurboActivate("2d65930359df9afb6f9a54.36732074");
            } catch (TurboActivateException failure) {
                // TODO: handle
            }
        }
    }

    @Override
    public void initComponent() {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.kalessil.phpStorm.phpInspectionsEA"));
        if (null == plugin) {
            return;
        }

        final EASettings settings = EASettings.getInstance();

        /* collect installation events (anonymous) */
        this.updated = !plugin.getVersion().equals(settings.getVersion());
        if (this.updated) {
            settings.setVersion(plugin.getVersion());
            AnalyticsUtil.registerPluginEvent(settings, "install", settings.getOldestVersion());
        }
        AnalyticsUtil.registerPluginEvent(settings, "run", settings.getOldestVersion());

        /* collect exceptions */
        final FileAppender appender = new FileAppender() {
            @Override
            public void append(@NotNull LoggingEvent event) {
                final ThrowableInformation exceptionDetails = event.getThrowableInformation();
                if (exceptionDetails != null) {
                    AnalyticsUtil.registerLoggedException(
                        settings.getVersion(),
                        settings.getUuid(),
                        exceptionDetails.getThrowable()
                    );
                }
            }
        };
        appender.setName("ea-exceptions-tracker");
        Logger.getRootLogger().addAppender(appender);

        this.initLicensing();
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "EAApplicationComponent";
    }

    boolean isUpdated() {
        return this.updated;
    }

    boolean isUpdateNotificationShown() {
        return this.updateNotificationShown;
    }

    void setUpdateNotificationShown(boolean shown) {
        this.updateNotificationShown = shown;
    }
}

/*
 * Copyright (c) 2017 Cosylab d.d.
 *
 * Contact Information:
 *   Cosylab d.d., Ljubljana, Slovenia
 *   http://www.cosylab.com
 */
package org.csstudio.fzj.cosy.css.product;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.csstudio.startup.application.Application;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 *
 * <code>ITERApplication</code> is an extension of the default CSS application that suppresses a specific exception
 * printout made by third party plugins. It also hides the splash screen during the workspace prompt.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
@SuppressWarnings("restriction")
public class FZJCOSYApplication extends Application {

    @Override
    public Object start(IApplicationContext context) throws Exception {
        Object o = super.start(context);
        // Bugfix/workaround for org.apache.felix.gogo.shell.Activator,
        // which prints InterruptedException if stopped before it was even started.
        // It is using a hardcoded 100 ms sleep, so we should be safe if we wait 150 ms for
        // that plugin to finish its magic.
        Thread.sleep(150);
        return o;
    }

    @Override
    protected Object promptForWorkspace(Display display, IApplicationContext context) throws Exception {
        Location loc = Platform.getInstanceLocation();
        if (loc.isSet()) {
            URL url = loc.getURL();
            File file = new File(url.getFile());
            file = new File(file, ".metadata");
            file = new File(file, ".plugins");
            file = new File(file, "org.eclipse.e4.workbench");
            file = new File(file, "workbench.xmi");
            if (file.exists()) {
                boolean delete = false;
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    while(br.ready()) {
                        String line = br.readLine();
                        if (line != null && line.contains("com.cosylab.fzj.cosy.oc.ui.perspective")) {
                            delete = true;
                            break;
                        }
                    }
                }
                if (delete) {
                    file.delete();
                }
            }
}

        Shell shell = WorkbenchPlugin.getSplashShell(display);
        if (shell != null) {
            shell.setVisible(false);
        }
        Object o = super.promptForWorkspace(display, context);
        if (shell != null) {
            shell.setVisible(true);
            shell.forceActive();
            shell.forceFocus();
        }
        return o;
    }
}

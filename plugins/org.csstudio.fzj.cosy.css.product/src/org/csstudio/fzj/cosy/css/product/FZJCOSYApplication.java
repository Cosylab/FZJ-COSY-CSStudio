package org.csstudio.fzj.cosy.css.product;

import org.csstudio.startup.application.Application;
import org.eclipse.equinox.app.IApplicationContext;
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

package org.csstudio.fzj.cosy.css.product;

import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * 
 * <code>FZJCOSYWorkbenchWindowAdvisor</code> takes care that unwanted views are removed from the Open View menu.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
@SuppressWarnings("restriction")
public class FZJCOSYWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    /**
     * Construct a new workbench window advisor.
     * 
     * @param configurer
     */
    public FZJCOSYWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor#postWindowOpen()
     */
    @Override
    public void postWindowOpen() {
        super.postWindowOpen();
        WorkbenchWindow window = (WorkbenchWindow) getWindowConfigurer().getWindow();
        MApplication application = (MApplication) window.getService(MApplication.class);
        WorkbenchUtil.removeUnwantedViews(application);

    }

}

package org.csstudio.fzj.cosy.css.product;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.utility.product.ApplicationWorkbenchAdvisor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * 
 * <code>FZJCOSYWorkbenchAdvisor</code> is an extension of default advisor that disables certain perspectives, logs and
 * bindings.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class FZJCOSYWorkbenchAdvisor extends ApplicationWorkbenchAdvisor {

    public FZJCOSYWorkbenchAdvisor(OpenDocumentEventProcessor openDocProcessor) {
        super(openDocProcessor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.csstudio.utility.product.ApplicationWorkbenchAdvisor#initialize(org.
     * eclipse.ui.application.IWorkbenchConfigurer)
     */
    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        WorkbenchUtil.removeUnWantedPerspectives();
        WorkbenchUtil.unbindDuplicateBindings();
    }

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new FZJCOSYWorkbenchWindowAdvisor(configurer);
    }
}

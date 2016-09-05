package org.csstudio.fzj.cosy.css.product;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

/**
 * 
 * <code>WorkbenchUtil</code> is a utility class for shaping the workbench by removing unneeded stuff.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class WorkbenchUtil {

    private static final String[] HIDE_MESSAGE_STARTS_WITH = new String[] {
            "Keybinding conflicts occurred.  They may interfere with normal accelerator operation.",
            "Invalid preference page path: XML Syntax", "Job found still running after platform shutdown." };

    private static final String[] IGNORE_PERSPECTIVES = new String[] {
            "org.eclipse.team.ui.TeamSynchronizingPerspective" };

    private static final String[] IGNORE_VIEWS = new String[] { "org.eclipse.ui.views.BookmarkView",
            "org.eclipse.ui.views.TaskList", "org.eclipse.ui.views.ProblemView", "org.eclipse.ui.views.AllMarkersView",
            "org.eclipse.search.SearchResultView", "org.eclipse.ui.navigator.ProjectExplorer",
            //"org.eclipse.team.sync.views.SynchronizeView", "org.eclipse.team.ui.GenericHistoryView"
            };

    private static final String[] VERBOSE_PACKAGES = new String[] { "com.sun.jersey.core.spi.component",
            "com.sun.jersey.core.spi.component.ProviderServices", "com.sun.jersey.spi.service.ServiceFinder" };

    private static final List<Logger> strongRefLoggers = new ArrayList<>();

    private static class HideUnWantedLogFilter implements Filter {

        private Filter previousFilter;

        public HideUnWantedLogFilter(Filter previousFilter) {
            this.previousFilter = previousFilter;
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            if (record.getMessage() != null) {
                for (String hideMsgStartsWith : HIDE_MESSAGE_STARTS_WITH) {
                    if (record.getMessage().startsWith(hideMsgStartsWith)) {
                        return false;
                    }
                }
            }
            if (previousFilter == null) {
                return true;
            }
            return previousFilter.isLoggable(record);
        }
    };

    public static void removeUnWantedLog() {
        // Hide unwanted message from log
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setFilter(new HideUnWantedLogFilter(rootLogger.getFilter()));
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setFilter(new HideUnWantedLogFilter(handler.getFilter()));
        }

        // Set upper log level on too verbose packages
        Level verboseLogLevel = Level.SEVERE;

        for (String verbosePackage : VERBOSE_PACKAGES) {
            Logger logger = Logger.getLogger(verbosePackage);
            logger.setLevel(verboseLogLevel);
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(verboseLogLevel);
            }
            // keep strong references to all loggers. Otherwise the LogMaager
            // will flush them out
            strongRefLoggers.add(logger);
        }
    }

    /**
     * Removes the unwanted perspectives from your RCP application
     */
    public static void removeUnWantedPerspectives() {
        IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
        IPerspectiveDescriptor[] perspectiveDescriptors = perspectiveRegistry.getPerspectives();
        List<String> ignoredPerspectives = Arrays.asList(IGNORE_PERSPECTIVES);
        List<IPerspectiveDescriptor> removePerspectiveDesc = new ArrayList<IPerspectiveDescriptor>();

        for (IPerspectiveDescriptor perspectiveDescriptor : perspectiveDescriptors) {
            if (ignoredPerspectives.contains(perspectiveDescriptor.getId())) {
                removePerspectiveDesc.add(perspectiveDescriptor);
                if ("org.eclipse.ui.internal.registry.PerspectiveDescriptor"
                        .equals(perspectiveDescriptor.getClass().getName())) {
                    try {
                        Field f = perspectiveDescriptor.getClass().getDeclaredField("configElement");
                        f.setAccessible(true);
                        f.set(perspectiveDescriptor, null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // ignore: we don't care this will happen only if the Eclipse internals change
                    }
                }
                perspectiveRegistry.deletePerspective(perspectiveDescriptor);
            }
        }

        if (perspectiveRegistry instanceof IExtensionChangeHandler && !removePerspectiveDesc.isEmpty()) {
            IExtensionChangeHandler extChgHandler = (IExtensionChangeHandler) perspectiveRegistry;
            extChgHandler.removeExtension(null, removePerspectiveDesc.toArray());
        }
    }
    
    /**
     * Remove unwanted views from CSS.
     *
     * @param application the application object from which views will be removed
     */
    public static void removeUnwantedViews(MApplication application) {
        if (application == null) {
            return;
        }
        //Because of some unknown reasons the activities do not remove the views from the Show View dialog. Therefore,
        //we try to brute force remove the view from the application
        List<MPartDescriptor> descriptors = application.getDescriptors();
        List<String> views = Arrays.asList(IGNORE_VIEWS);
        List<MPartDescriptor> toRemove = new ArrayList<>();
        for (MPartDescriptor d : descriptors) {
            if (views.contains(d.getElementId())) {
                toRemove.add(d);
            }
        }
        for (MPartDescriptor p : toRemove) {
            descriptors.remove(p);
        }
    }

    /**
     * Unbind F11 KeyBinding of org.eclipse.debug.ui to avoid conflict with org.csstudio.opibuilder plugin
     */
    public static void unbindDuplicateBindings() {
        IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        BindingManager localChangeManager = new BindingManager(new ContextManager(), new CommandManager());

        final Scheme[] definedSchemes = bindingService.getDefinedSchemes();
        try {
            for (int i = 0; i < definedSchemes.length; i++) {
                final Scheme scheme = definedSchemes[i];
                final Scheme copy = localChangeManager.getScheme(scheme.getId());
                copy.define(scheme.getName(), scheme.getDescription(), scheme.getParentId());
            }
            localChangeManager.setActiveScheme(bindingService.getActiveScheme());
        } catch (final NotDefinedException e) {
            e.printStackTrace();
        }
        localChangeManager.setLocale(bindingService.getLocale());
        localChangeManager.setPlatform(bindingService.getPlatform());
        localChangeManager.setBindings(bindingService.getBindings());

        KeyBinding opiFullScreenBinding = null;
        int nbBinding = 0;

        Binding[] bArray = bindingService.getBindings();
        if (bArray != null) {
            for (Binding binding : bArray) {
                if (binding instanceof KeyBinding) {
                    KeyBinding kBind = (KeyBinding) binding;
                    if (kBind.getParameterizedCommand() != null
                            && kBind.getParameterizedCommand().getCommand() != null) {
                        String id = kBind.getParameterizedCommand().getCommand().getId();
                        if ("org.eclipse.debug.ui.commands.DebugLast".equals(id)
                                || "org.eclipse.jdt.ui.edit.text.java.search.declarations.in.workspace".equals(id)) {
                            KeySequence triggerSequence = kBind.getKeySequence();
                            String contextId = kBind.getContextId();
                            String schemeId = kBind.getSchemeId();
                            KeyBinding deleteBinding = new KeyBinding(triggerSequence, null, schemeId, contextId, null,
                                    null, null, Binding.USER);
                            localChangeManager.addBinding(deleteBinding);
                            try {
                                bindingService.savePreferences(localChangeManager.getActiveScheme(),
                                        localChangeManager.getBindings());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if ("org.csstudio.opibuilder.actions.fullscreen"
                                .equals(kBind.getParameterizedCommand().getCommand().getId())) {
                            if (opiFullScreenBinding == null)
                                opiFullScreenBinding = kBind;
                            nbBinding++;
                        }
                    }
                }
            }
        }

        // Rebind OPI runner full screen command if it exists only one time
        if (nbBinding == 1 && opiFullScreenBinding != null) {
            KeySequence triggerSequence = opiFullScreenBinding.getKeySequence();
            String contextId = opiFullScreenBinding.getContextId();
            String schemeId = opiFullScreenBinding.getSchemeId();

            KeyBinding updateBinding = new KeyBinding(triggerSequence, opiFullScreenBinding.getParameterizedCommand(),
                    schemeId, contextId, null, null, null, Binding.USER);
            localChangeManager.addBinding(updateBinding);
            try {
                bindingService.savePreferences(localChangeManager.getActiveScheme(), localChangeManager.getBindings());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

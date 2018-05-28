/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.workbench.client;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import org.dashbuilder.client.cms.screen.explorer.NavigationExplorerScreen;
import org.dashbuilder.client.navigation.NavigationManager;
import org.dashbuilder.client.navigation.event.NavTreeChangedEvent;
import org.dashbuilder.client.navigation.widget.editor.NavTreeEditor;
import org.dashbuilder.navigation.NavTree;
import org.guvnor.common.services.shared.config.AppConfigService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jbpm.workbench.client.i18n.Constants;
import org.jbpm.workbench.client.navigation.NavTreeDefinitions;
import org.jbpm.workbench.client.perspectives.ProcessAdminSettingsPerspective;
import org.jbpm.workbench.client.perspectives.TaskAdminSettingsPerspective;
import org.kie.workbench.common.workbench.client.admin.DefaultAdminPageHelper;
import org.kie.workbench.common.workbench.client.entrypoint.DefaultWorkbenchEntryPoint;
import org.kie.workbench.common.workbench.client.menu.DefaultWorkbenchFeaturesMenusHelper;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.views.pfly.menu.MainBrand;
import org.uberfire.client.workbench.widgets.menu.megamenu.WorkbenchMegaMenuPresenter;
import org.uberfire.ext.security.management.client.widgets.management.events.SaveGroupEvent;
import org.uberfire.ext.security.management.client.widgets.management.events.SaveRoleEvent;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.kie.workbench.common.workbench.client.PerspectiveIds.*;

@EntryPoint
@Bundle("i18n/HomeConstants.properties")
public class ShowcaseEntryPoint extends DefaultWorkbenchEntryPoint {

    protected Constants constants = Constants.INSTANCE;

    protected org.jbpm.workbench.common.client.resources.i18n.Constants commonConstants = org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE;

    protected SyncBeanManager iocManager;

    protected User identity;

    protected DefaultWorkbenchFeaturesMenusHelper menusHelper;

    protected WorkbenchMegaMenuPresenter menuBar;

    protected DefaultAdminPageHelper adminPageHelper;

    protected NavTreeDefinitions navTreeDefinitions;
    protected NavigationManager navigationManager;
    protected NavigationExplorerScreen navigationExplorerScreen;

    @Inject
    public ShowcaseEntryPoint(final Caller<AppConfigService> appConfigService,
                              final ActivityBeansCache activityBeansCache,
                              final SyncBeanManager iocManager,
                              final User identity,
                              final DefaultAdminPageHelper adminPageHelper,
                              final DefaultWorkbenchFeaturesMenusHelper menusHelper,
                              final WorkbenchMegaMenuPresenter menuBar,
                              final NavTreeDefinitions navTreeDefinitions,
                              final NavigationManager navigationManager,
                              final NavigationExplorerScreen navigationExplorerScreen) {
        super(appConfigService,
              activityBeansCache);
        this.iocManager = iocManager;
        this.identity = identity;
        this.menusHelper = menusHelper;
        this.menuBar = menuBar;
        this.adminPageHelper = adminPageHelper;
        this.navTreeDefinitions = navTreeDefinitions;
        this.navigationManager = navigationManager;
        this.navigationExplorerScreen = navigationExplorerScreen;
    }

    @PostConstruct
    public void init() {
        initNavTreeEditor();
    }

    @Override
    protected void setupMenu() {
        navigationManager.init(() -> {

            // Set the default nav tree
            NavTree navTree = navTreeDefinitions.buildDefaultNavTree();
            navigationManager.setDefaultNavTree(navTree);

            // Initialize the  menu bar
            initMenuBar();
        });
    }

    protected void initMenuBar() {
        refreshMenuBar();
    }

    protected void refreshMenuBar() {

        // Turn the workbench nav tree into a Menus instance that is passed as input to the workbench's menu bar
        NavTree workbenchNavTree = navigationManager.getNavTree().getItemAsTree(NavTreeDefinitions.GROUP_WORKBENCH);
        MenuFactory.TopLevelMenusBuilder<MenuFactory.MenuBuilder> builder = menusHelper.buildMenusFromNavTree(workbenchNavTree);

        Menus menus = builder.build();

        // Refresh the menu bar
        menuBar.clear();
        menuBar.addMenus(menus);
        menusHelper.addUtilitiesMenuItems();
    }

    @Override
    protected void setupAdminPage() {
        adminPageHelper.setup();
    }

    protected List<? extends MenuItem> getAuthoringViews() {
        return Arrays.asList(
                MenuFactory.newSimpleItem(constants.Project_Authoring()).perspective(LIBRARY).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(constants.artifactRepository()).perspective(GUVNOR_M2REPO).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(constants.Administration()).perspective(ADMINISTRATION).endMenu().build().getItems().get(0)
        );
    }

    protected List<? extends MenuItem> getProcessManagementViews() {
        return Arrays.asList(
                MenuFactory.newSimpleItem(commonConstants.Process_Definitions()).perspective(PROCESS_DEFINITIONS).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(commonConstants.Process_Instances()).perspective(PROCESS_INSTANCES).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(constants.Process_Instances_Admin()).perspective(ProcessAdminSettingsPerspective.PERSPECTIVE_ID).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(commonConstants.Tasks()).perspective(TASKS_ADMIN).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(commonConstants.ExecutionErrors()).perspective(EXECUTION_ERRORS).endMenu().build().getItems().get(0)
        );
    }

    protected List<? extends MenuItem> getDeploymentViews() {
        return Arrays.asList(
                MenuFactory.newSimpleItem(constants.Execution_Servers()).perspective(SERVER_MANAGEMENT).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(commonConstants.Jobs()).perspective(JOBS).endMenu().build().getItems().get(0)
        );
    }

    protected List<? extends MenuItem> getWorkViews() {
        return Arrays.asList(
                MenuFactory.newSimpleItem(commonConstants.Task_Inbox()).perspective(TASKS).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(constants.Tasks_List_Admin()).perspective(TaskAdminSettingsPerspective.PERSPECTIVE_ID).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(constants.Data_Sets()).perspective(DATASET_AUTHORING).endMenu().build().getItems().get(0)
        );
    }

    protected List<? extends MenuItem> getDashboardsViews() {
        return Arrays.asList(
                MenuFactory.newSimpleItem(constants.Process_Reports()).perspective(PROCESS_DASHBOARD).endMenu().build().getItems().get(0),
                MenuFactory.newSimpleItem(constants.Task_Reports()).perspective(TASK_DASHBOARD).endMenu().build().getItems().get(0)
        );
    }

    @Produces
    @ApplicationScoped
    public MainBrand createBrandLogo() {
        return () -> new Image(AppResource.INSTANCE.images().logo());
    }

    public void onNavTreeChanged(@Observes final NavTreeChangedEvent event) {
        refreshMenuBar();
    }

    // Listen to authorization policy changes as it might impact the menu items shown

    public void onAuthzPolicyChanged(@Observes final SaveRoleEvent event) {
        refreshMenuBar();
    }

    public void onAuthzPolicyChanged(@Observes final SaveGroupEvent event) {
        refreshMenuBar();
    }

    private void initNavTreeEditor() {
        final NavTreeEditor navTreeEditor = navigationExplorerScreen.getNavTreeEditor();

        // Due to a limitation in the Menus API the number of levels in the workbench's menu bar
        // navigation tree node must be limited to 2 (see https://issues.jboss.org/browse/GUVNOR-2992)
        navTreeEditor.setMaxLevels(NavTreeDefinitions.GROUP_WORKBENCH, 2);

        // Mega Menu does not support dividers at any level
        navTreeEditor.setNewDividerEnabled(NavTreeDefinitions.GROUP_WORKBENCH, false).applyToAllChildren();

        // Mega Menu does not support single menu items at first level (menu items are only allowed inside menu groups).
        navTreeEditor.setNewPerspectiveEnabled(NavTreeDefinitions.GROUP_WORKBENCH, false);

        // Mega Menu is the only nav widget which allow links to core perspectives
        navTreeEditor.setOnlyRuntimePerspectives(NavTreeDefinitions.GROUP_WORKBENCH, false).applyToAllChildren();

        // Mega Menu's linked perspectives don't support passing a navigation context
        navTreeEditor.setPerspectiveContextEnabled(NavTreeDefinitions.GROUP_WORKBENCH, false).applyToAllChildren();
    }
}

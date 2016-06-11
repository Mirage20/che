/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.client;

import com.google.inject.Inject;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.testing.client.view.TestResultPresenter;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
//import org.eclipse.che.ide.ext.java.testing.client.view.TestRunnerPresenter;
//import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
//import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;

public class RunClassTestAction extends JavaEditorAction {

    private final NotificationManager notificationManager;
    private final EditorAgent editorAgent;
    private final TestResultPresenter presenter;
    private final TestServiceClient service;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public RunClassTestAction(TestResources resources, NotificationManager notificationManager, EditorAgent editorAgent,
                              FileTypeRegistry fileTypeRegistry, TestResultPresenter presenter,
                              TestServiceClient service,DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super("Run Class", "Run the current test cases", resources.testIcon(), editorAgent, fileTypeRegistry);
        this.notificationManager = notificationManager;
        this.editorAgent = editorAgent;
        this.presenter = presenter;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        EditorPartPresenter editorPart = editorAgent.getActiveEditor();
        final VirtualFile file = editorPart.getEditorInput().getFile();
        String fqn = JavaSourceFolderUtil.getFQNForFile(file);
        Unmarshallable<TestResult> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(TestResult.class);

        service.runTest(appContext.getWorkspaceId(),
                project,
                fqn,
                new RequestCallback<TestResult>(unmarshaller) {
                    @Override
                    protected void onSuccess(TestResult result) {
                        Log.info(TestResultPresenter.class, result);
                        notification.setStatus(SUCCESS);
                        if (result.isSuccess()) {
                            notification.setTitle("Test runner executed successfully");
                            notification.setContent("All tests are passed");
                        } else {
                            notification.setTitle("Test runner executed successfully with test failures.");
                            notification.setContent(result.getFailureCount() + " tests are failed.\n");
                        }
                        presenter.handleResponse(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        final String errorMessage = (exception.getMessage() != null)
                                ? exception.getMessage()
                                : "Failed to run test cases";
                        notification.setContent(errorMessage);
                        notification.setStatus(FAIL);
                    }
                }
        );
//        presenter.showDialog();
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setVisible(true);
    }
}
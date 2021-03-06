/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.client.command;

import org.eclipse.che.ide.CommandLine;

/**
 * Model of the Maven command line.
 *
 * @author Artem Zatsarynnyi
 */
class MavenCommandModel {

    private String workingDirectory;
    private String arguments;

    MavenCommandModel(String workingDirectory, String arguments) {
        this.workingDirectory = workingDirectory;
        this.arguments = arguments;
    }

    /** Crates {@link MavenCommandModel} instance from the given command line. */
    static MavenCommandModel fromCommandLine(String commandLine) {
        final CommandLine cmd = new CommandLine(commandLine);

        String workingDirectory = null;

        if (cmd.hasArgument("-f")) {
            workingDirectory = cmd.getArgument(cmd.indexOf("-f") + 1);

            cmd.removeArgument("-f");
            cmd.removeArgument(workingDirectory);
        }

        cmd.removeArgument("mvn");
        String arguments = cmd.toString();

        return new MavenCommandModel(workingDirectory, arguments);
    }

    String getWorkingDirectory() {
        return workingDirectory;
    }

    void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    String getArguments() {
        return arguments;
    }

    /** Set command arguments, e.g. {@code [options] [<goal(s)>] [<phase(s)>]}. */
    void setArguments(String arguments) {
        this.arguments = arguments;
    }

    String toCommandLine() {
        final StringBuilder cmd = new StringBuilder("mvn");

        if (!workingDirectory.trim().isEmpty()) {
            cmd.append(" -f ").append(workingDirectory.trim());
        }

        if (!arguments.trim().isEmpty()) {
            cmd.append(' ').append(arguments.trim());
        }

        return cmd.toString();
    }
}

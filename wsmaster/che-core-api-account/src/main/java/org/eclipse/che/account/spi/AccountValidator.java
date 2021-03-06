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
package org.eclipse.che.account.spi;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

/**
 * Utils for account validation.
 *
 * @author Sergii Leschenko
 */
// TODO extract normalization code from the validator as it is not related to the validation at all
@Singleton
public class AccountValidator {
    private static final Logger LOG = LoggerFactory.getLogger(AccountValidator.class);

    private static final Pattern ILLEGAL_ACCOUNT_NAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern VALID_ACCOUNT_NAME              = Pattern.compile("^[a-zA-Z0-9]*");

    private final AccountManager accountManager;

    @Inject
    public AccountValidator(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    /**
     * Validate name, if it doesn't contain illegal characters
     *
     * @param name
     *         account name
     * @return true if valid name, false otherwise
     */
    public boolean isValidName(String name) {
        return name != null && VALID_ACCOUNT_NAME.matcher(name).matches();
    }

    /**
     * Remove illegal characters from account name, to make it URL-friendly.
     * If all characters are illegal, return automatically generated account name with specified prefix.
     * Also ensures account name is unique, if not, adds digits to it's end.
     *
     * @param name
     *         account name
     * @param prefix
     *         prefix to add to generated name
     * @return account name without illegal characters
     */
    public String normalizeAccountName(String name, String prefix) throws ServerException {
        String normalized = ILLEGAL_ACCOUNT_NAME_CHARACTERS.matcher(name).replaceAll("");
        String candidate = normalized.isEmpty() ? NameGenerator.generate(prefix, 4) : normalized;

        int i = 1;
        try {
            while (accountExists(candidate)) {
                candidate = normalized.isEmpty() ? NameGenerator.generate(prefix, 4) : normalized + String.valueOf(i++);
            }
        } catch (ServerException e) {
            LOG.warn("Error occurred during account name normalization", e);
            throw e;
        }
        return candidate;
    }

    private boolean accountExists(String accountName) throws ServerException {
        try {
            accountManager.getByName(accountName);
        } catch (NotFoundException e) {
            return false;
        }
        return true;
    }
}

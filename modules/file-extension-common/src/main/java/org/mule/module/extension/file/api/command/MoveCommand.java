/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.file.api.command;

import org.mule.module.extension.file.api.FileSystem;

/**
 * Command design pattern for moving files
 *
 * @since 4.0
 */
public interface MoveCommand
{

    /**
     * Moves files under the considerations of {@link FileSystem#move(String, String, boolean, boolean)}
     *
     * @param sourcePath            the path to the file to be copied
     * @param targetPath            the target directory
     * @param overwrite             whether or not overwrite the file if the target destination already exists.
     * @param createParentDirectory whether or not to attempt creating the parent directory if it doesn't exists.
     * @throws IllegalArgumentException if an illegal combination of arguments is supplied
     */
    void move(String sourcePath, String targetPath, boolean overwrite, boolean createParentDirectory);
}
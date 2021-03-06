/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ftp.internal.ftp.command;

import static java.lang.String.format;
import org.apache.commons.net.ftp.FTPClient;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.command.WriteCommand;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.ftp.internal.ftp.connection.ClassicFtpFileSystem;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * A {@link ClassicFtpCommand} which implements the {@link WriteCommand} contract
 *
 * @since 4.0
 */
public final class FtpWriteCommand extends ClassicFtpCommand implements WriteCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(FtpWriteCommand.class);

  private final MuleContext muleContext;

  /**
   * {@inheritDoc}
   */
  public FtpWriteCommand(ClassicFtpFileSystem fileSystem, FTPClient client, MuleContext muleContext) {
    super(fileSystem, client);
    this.muleContext = muleContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(String filePath, InputStream content, FileWriteMode mode, boolean lock, boolean createParentDirectory,
                    String encoding) {
    Path path = resolvePath(filePath);
    FileAttributes file = getFile(filePath);

    if (file == null) {
      assureParentFolderExists(path, createParentDirectory);
    } else {
      if (mode == FileWriteMode.CREATE_NEW) {
        throw new FileAlreadyExistsException(format(
                                                    "Cannot write to path '%s' because it already exists and write mode '%s' was selected. "
                                                        + "Use a different write mode or point to a path which doesn't exists",
                                                    path, mode));
      } else if (mode == FileWriteMode.OVERWRITE) {
        fileSystem.delete(file.getPath());
      }
    }

    try (OutputStream outputStream = getOutputStream(path.toString(), mode)) {
      IOUtils.copy(content, outputStream);
      LOGGER.debug("Successfully wrote to path {}", path.toString());
    } catch (Exception e) {
      throw exception(format("Exception was found writing to file '%s'", path), e);
    } finally {
      fileSystem.awaitCommandCompletion();
    }
  }

  private OutputStream getOutputStream(String path, FileWriteMode mode) {
    try {
      return mode == FileWriteMode.APPEND ? client.appendFileStream(path) : client.storeFileStream(path);
    } catch (Exception e) {
      throw exception(format("Could not open stream to write to path '%s' using mode '%s'", path, mode), e);
    }
  }
}

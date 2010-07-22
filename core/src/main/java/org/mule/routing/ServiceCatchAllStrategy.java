/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing;

import org.mule.DefaultMuleEvent;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.routing.RoutingException;
import org.mule.api.service.Service;

/**
 * <code>ServiceCatchAllStrategy</code> is used to catch any events and forward the
 * events to the service as is.
 * @deprecated
 */
public class ServiceCatchAllStrategy extends AbstractCatchAllStrategy
{
    @Override
    public synchronized MuleEvent doCatchMessage(MuleEvent event)
        throws RoutingException
    {
        if (!(event.getFlowConstruct() instanceof Service))
        {
            throw new UnsupportedOperationException(
                "CollectionResponseWithCallbackCorrelator is only supported with Service");
        }

        logger.debug("Catch all strategy handling event: " + event);
        try
        {
            if (event.getEndpoint().getExchangePattern().hasResponse())
            {
                statistics.incrementRoutedMessage(event.getEndpoint());
                MuleMessage responseMessage = ((Service) event.getFlowConstruct()).sendEvent(event);
                if (responseMessage != null)
                {
                    return new DefaultMuleEvent(responseMessage, event);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                statistics.incrementRoutedMessage(event.getEndpoint());
                ((Service) event.getFlowConstruct()).dispatchEvent(event);
                return null;
            }
        }
        catch (MuleException e)
        {
            throw new RoutingException(event.getMessage(), event.getEndpoint(), e);
        }
    }
}

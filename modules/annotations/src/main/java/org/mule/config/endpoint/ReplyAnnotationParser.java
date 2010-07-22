/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.endpoint;

import org.mule.api.MuleException;
import org.mule.api.RouterAnnotationParser;
import org.mule.api.annotations.meta.Channel;
import org.mule.api.annotations.routing.Reply;
import org.mule.api.processor.MessageProcessor;
import org.mule.config.routing.reply.AbstractResponseCallbackAggregator;
import org.mule.config.routing.reply.SingleResponseWithCallbackRouter;
import org.mule.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

/**
 * TODO
 */
public class ReplyAnnotationParser extends AbstractEndpointAnnotationParser implements RouterAnnotationParser
{
    protected AnnotatedEndpointData createEndpointData(Annotation annotation) throws MuleException
    {
        Reply reply = (Reply) annotation;
        AnnotatedEndpointData epd = new AnnotatedEndpointData(MEP.InOnly, reply);
        epd.setConnectorName(reply.connector());
        epd.setAddress(reply.uri());
        epd.setFilter(reply.filter());
        return epd;
    }

    protected String getIdentifier()
    {
        return Reply.class.getAnnotation(Channel.class).identifer();
    }

    public MessageProcessor parseRouter(Annotation annotation) throws MuleException
    {
        AbstractResponseCallbackAggregator router;
        Reply reply = (Reply) annotation;
        router = new SingleResponseWithCallbackRouter();
        if (StringUtils.isNotBlank(reply.callback()))
        {
            router.setCallbackMethod(reply.callback());
        }
        router.setTimeout(reply.replyTimeout());
        router.setFailOnTimeout(reply.failOnTimeout());

        return null;//router;
    }

    @Override
    public boolean supports(Annotation annotation, Class clazz, Member member)
    {
        if (clazz.isInterface())
        {
            //You cannot use the @Reply annotation on an interface
            return false;
        }
        return super.supports(annotation, clazz, member);
    }
}
package org.mule.util.scan.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Meta
public @interface Marker
{
    String value();
}

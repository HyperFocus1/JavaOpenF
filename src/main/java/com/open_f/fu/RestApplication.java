package com.open_f.fu;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** Jakarta REST descubre los resources y providers; CDI resuelve sus dependencias. */
@ApplicationPath("/api")
public class RestApplication extends Application {
}

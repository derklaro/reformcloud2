package de.klaro.reformcloud2.executor.api.common.restapi.auth;

import de.klaro.reformcloud2.executor.api.common.configuration.Configurable;
import de.klaro.reformcloud2.executor.api.common.restapi.request.WebRequester;
import de.klaro.reformcloud2.executor.api.common.utility.function.Double;

public interface Auth {

    Double<Boolean, WebRequester> handleAuth(Configurable configurable);
}
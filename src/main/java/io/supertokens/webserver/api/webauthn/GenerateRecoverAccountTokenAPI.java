/*
 *    Copyright (c) 2025, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens.webserver.api.webauthn;

import com.google.gson.JsonObject;
import io.supertokens.Main;
import io.supertokens.pluginInterface.Storage;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.multitenancy.TenantIdentifier;
import io.supertokens.pluginInterface.multitenancy.exceptions.TenantOrAppNotFoundException;
import io.supertokens.pluginInterface.webauthn.exceptions.UserIdNotFoundException;
import io.supertokens.utils.Utils;
import io.supertokens.webauthn.WebAuthN;
import io.supertokens.webserver.InputParser;
import io.supertokens.webserver.WebserverAPI;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class GenerateRecoverAccountTokenAPI extends WebserverAPI {
    public GenerateRecoverAccountTokenAPI(Main main) {
        super(main, "webauthn");
    }

    @Override
    public String getPath() {
        return "/recipe/webauthn/user/recover/token";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // API is tenant specific
        JsonObject input = InputParser.parseJsonObjectOrThrowError(req);
        String email = InputParser.parseStringOrThrowError(input, "email", false);
        String userId = InputParser.parseStringOrThrowError(input, "userId", false);
        email = Utils.normaliseEmail(email);

        try {
            TenantIdentifier tenantIdentifier = getTenantIdentifier(req);
            Storage storage = getTenantStorage(req);

            String token = WebAuthN.generateRecoverAccountToken(main, storage, tenantIdentifier, email, userId);
            JsonObject response = new JsonObject();
            response.addProperty("status", "OK");
            response.addProperty("token", token);

            sendJsonResponse(200, response, resp);
        } catch (UserIdNotFoundException e) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "UNKNOWN_USER_ID_ERROR");
            sendJsonResponse(200, response, resp);
        } catch (TenantOrAppNotFoundException | StorageQueryException | NoSuchAlgorithmException |
                 InvalidKeySpecException e) {
            throw new ServletException(e);
        }
    }
}

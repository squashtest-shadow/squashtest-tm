/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.oauth;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
 
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
 
public class AuthProxy {
 
    private Proxy proxy;
    private RestTemplate template;
 
    public AuthProxy() {
        proxy = new Proxy(Type.HTTP, new InetSocketAddress(
                "proxy.abc.net", 3001));
 
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
 
        requestFactory.setProxy(proxy);
 
        template = new RestTemplate(requestFactory);
    }
 
    public boolean isValidUser(String user, String password) {
 
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("user", user);
        map.add("password", password);
 
        HttpEntity<String> response = template.postForEntity(
                "https://authentication.local/auth", map,
                String.class);
 
        HttpHeaders headers = response.getHeaders();
 
        List<String> cookies = headers.get("Set-Cookie");
 
        for (String cookie : cookies) {
            if (cookie.indexOf("Auth")!=-1)
                return true;
        }
 
        return false;
    }
 
}
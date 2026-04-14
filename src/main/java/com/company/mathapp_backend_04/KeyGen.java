package com.company.mathapp_backend_04;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Base64;

public class KeyGen {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println(base64Key); // ta sẽ đc SecretKey: C4yadc7mKYeowjvzjD88/4+DxfIpQPAskLPsEJt9tEM=
    }
}

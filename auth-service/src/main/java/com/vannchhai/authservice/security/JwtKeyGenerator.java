package com.vannchhai.authservice.security;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Base64;

public final class JwtKeyGenerator {

  private JwtKeyGenerator() {}

  public static void main(String[] args) {

    SecretKey key = Jwts.SIG.HS256.key().build();
    String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

    System.out.println("Generated HS256 secret (base64, 256-bit):");
    System.out.println(base64Key);
    System.out.println();
    System.out.println("Set this as an environment variable, e.g.:");
    System.out.println("export JWT_SECRET=" + base64Key);
  }
}

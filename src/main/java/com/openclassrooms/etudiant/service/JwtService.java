package com.openclassrooms.etudiant.service;


import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;


@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secret;

    public String generateToken(UserDetails userDetails) {
                
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .compact();
    }

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

}

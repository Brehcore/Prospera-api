package com.example.prospera.auth.jwt;

import com.example.prospera.auth.domain.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Getter
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; // Em milissegundos

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Método principal para gerar um token.
     * Ele cria o mapa de 'claims' e chama o método auxiliar.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        extraClaims.put("roles", roles);

        if (userDetails instanceof AuthUser authUser) {
            extraClaims.put("userId", authUser.getId());

            // Extrai os IDs de todas as organizações das quais o usuário é membro.
            List<UUID> orgIds = authUser.getMemberships().stream()
                    .map(membership -> membership.getOrganization().getId())
                    .toList(); // ou .collect(Collectors.toList()) para Java < 16

            // Adiciona a lista de IDs ao token.
            // O nome 'memberOfOrgs' é mais descritivo que 'organizationId'.
            extraClaims.put("memberOfOrgs", orgIds);
        }

        return generateToken(extraClaims, userDetails);
    }

    /**
     * Método auxiliar que constrói o token com base nas 'claims' recebidas.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // 3. Aqui, 'extraClaims' é um parâmetro recebido, por isso é encontrado.
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
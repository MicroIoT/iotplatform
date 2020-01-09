package top.microiot.security.authentication;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import top.microiot.domain.Token;
import top.microiot.exception.AuthenticationException;

@Component
public class TokenFactory {
    private static final String SCOPES = "scopes";
	private final TokenSettings settings;

    @Autowired
    public TokenFactory(TokenSettings settings) {
        this.settings = settings;
    }

    public Token createToken(String username, String domain) {
    	return new Token(token(username, domain), refreshToken(username, domain));
    }
    
    private String token(String username, String domain) {
        Claims claims = Jwts.claims().setSubject(username);
        if(domain != null)
        	claims.put(SCOPES, domain);

        Date currentTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTime);
        cal.add(Calendar.HOUR_OF_DAY, settings.getExpirationTime());
        
        String token = Jwts.builder()
          .setClaims(claims)
          .setIssuer(settings.getIssuer())
          .setIssuedAt(currentTime)
          .setExpiration(cal.getTime())
          .signWith(SignatureAlgorithm.HS512, settings.getSigningKey())
        .compact();

       return token;
    }

    private String refreshToken(String username, String domain) {
        Claims claims = Jwts.claims().setSubject(username);
        if(domain != null)
        	claims.put(SCOPES, domain);
        
        Date currentTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTime);
        cal.add(Calendar.HOUR_OF_DAY, settings.getRefreshExpTime());
        
        String token = Jwts.builder()
          .setClaims(claims)
          .setIssuer(settings.getIssuer())
          .setId(UUID.randomUUID().toString())
          .setIssuedAt(currentTime)
          .setExpiration(cal.getTime())
          .signWith(SignatureAlgorithm.HS512, settings.getSigningKey())
        .compact();

        return token;
    }
    
    public Jws<Claims> parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(settings.getSigningKey()).parseClaimsJws(token);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
           throw new BadCredentialsException("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException expiredEx) {
            throw new AuthenticationException(AuthenticationException.TOKEN_EXPIRED);
        }
    }
    
    public String getScope(Jws<Claims> claims) {
    	return claims.getBody().get(SCOPES, String.class);
    }
    
    public String getUsername(Jws<Claims> claims) {
    	return claims.getBody().getSubject();
    }
    
    public Date getExpire(String token) {
    	return parseClaims(token).getBody().getExpiration();
    }
    
    public String getJti(String token) {
		Jws<Claims> jwsClaims = parseClaims(token);
		return jwsClaims.getBody().getId();
	}
}

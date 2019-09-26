package top.microiot.security.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "microiot.authenticate.token")
public class TokenSettings {
    private Integer expirationTime;
    private String issuer;
    private String signingKey;
    private Integer refreshExpTime;
    
	public Integer getExpirationTime() {
		return expirationTime;
	}
	public void setExpirationTime(Integer expirationTime) {
		this.expirationTime = expirationTime;
	}
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public String getSigningKey() {
		return signingKey;
	}
	public void setSigningKey(String signingKey) {
		this.signingKey = signingKey;
	}
	public Integer getRefreshExpTime() {
		return refreshExpTime;
	}
	public void setRefreshExpTime(Integer refreshExpTime) {
		this.refreshExpTime = refreshExpTime;
	}
    
    
}

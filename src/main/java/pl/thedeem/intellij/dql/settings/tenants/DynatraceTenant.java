package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.openapi.util.text.StringUtil;

import java.util.Objects;

public class DynatraceTenant {
    private String name;
    private String url;
    private String credentialId;
    private AuthType authType = AuthType.API_TOKEN;

    public DynatraceTenant() {
    }

    public DynatraceTenant(String name, String url, String credentialId) {
        this.url = url;
        this.name = name != null ? name : url;
        this.credentialId = credentialId;
    }

    public DynatraceTenant(String name, String url, String credentialId, AuthType authType) {
        this.url = url;
        this.name = name != null ? name : url;
        this.credentialId = credentialId;
        this.authType = authType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getName() {
        return name;
    }

    public DynatraceTenant setName(String name) {
        this.name = name;
        return this;
    }

    public AuthType getAuthType() {
        return authType != null ? authType : AuthType.API_TOKEN;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    @Override
    public String toString() {
        return StringUtil.isNotEmpty(name) ? name : url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynatraceTenant that = (DynatraceTenant) o;
        return Objects.equals(url, that.url)
                && Objects.equals(name, that.name)
                && Objects.equals(credentialId, that.credentialId)
                && getAuthType() == that.getAuthType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, credentialId, name, getAuthType());
    }

    public enum AuthType {
        API_TOKEN,
        SSO_OAUTH
    }
}

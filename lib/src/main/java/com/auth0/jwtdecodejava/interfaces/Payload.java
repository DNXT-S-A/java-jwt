package com.auth0.jwtdecodejava.interfaces;

import java.util.Date;

/**
 * The Payload class represents the 2nd part of the JWT, where the Payload value is hold.
 */
public interface Payload {

    /**
     * Get the value of the "iss" claim, or null if it's not available.
     *
     * @return the Issuer value or null.
     */
    String getIssuer();

    /**
     * Get the value of the "sub" claim, or null if it's not available.
     *
     * @return the Subject value or null.
     */
    String getSubject();

    /**
     * Get the value of the "aud" claim, or null if it's not available.
     *
     * @return the Audience value or null.
     */
    String[] getAudience();

    /**
     * Get the value of the "exp" claim, or null if it's not available.
     *
     * @return the Expiration Time value or null.
     */
    Date getExpiresAt();

    /**
     * Get the value of the "nbf" claim, or null if it's not available.
     *
     * @return the Not Before value or null.
     */
    Date getNotBefore();

    /**
     * Get the value of the "iat" claim, or null if it's not available.
     *
     * @return the Issued At value or null.
     */
    Date getIssuedAt();

    /**
     * Get the value of the "jti" claim, or null if it's not available.
     *
     * @return the Payload ID value or null.
     */
    String getId();

    /**
     * Get a Private Claim given it's name. If the Claim wasn't specified in the Payload payload, null will be returned.
     *
     * @param name the name of the Claim to retrieve.
     * @return the Claim if found or null.
     */
    Claim getClaim(String name);
}
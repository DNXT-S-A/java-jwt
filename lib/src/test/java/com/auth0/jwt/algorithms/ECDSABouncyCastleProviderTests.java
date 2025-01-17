package com.auth0.jwt.algorithms;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.Arrays;
import java.util.Base64;

import static com.auth0.jwt.PemUtils.readPrivateKeyFromFile;
import static com.auth0.jwt.PemUtils.readPublicKeyFromFile;
import static com.auth0.jwt.algorithms.CryptoTestHelper.asJWT;
import static com.auth0.jwt.algorithms.CryptoTestHelper.assertSignaturePresent;
import static com.auth0.jwt.algorithms.ECDSAAlgorithmTest.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ECDSABouncyCastleProviderTests {

    private static final String PRIVATE_KEY_FILE_256 = "src/test/resources/ec256-key-private.pem";
    private static final String PUBLIC_KEY_FILE_256 = "src/test/resources/ec256-key-public.pem";
    private static final String INVALID_PUBLIC_KEY_FILE_256 = "src/test/resources/ec256-key-public-invalid.pem";

    private static final String PRIVATE_KEY_FILE_384 = "src/test/resources/ec384-key-private.pem";
    private static final String PUBLIC_KEY_FILE_384 = "src/test/resources/ec384-key-public.pem";
    private static final String INVALID_PUBLIC_KEY_FILE_384 = "src/test/resources/ec384-key-public-invalid.pem";

    private static final String PRIVATE_KEY_FILE_512 = "src/test/resources/ec512-key-private.pem";
    private static final String PUBLIC_KEY_FILE_512 = "src/test/resources/ec512-key-public.pem";
    private static final String INVALID_PUBLIC_KEY_FILE_512 = "src/test/resources/ec512-key-public-invalid.pem";
 
    private static final String ES256K_JWT = "eyJraWQiOiJteS1rZXktaWQiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.e30.2ggPsc4xwQhYcgJueo3uQ14MpaVJ3AbEE8UE-wA9fc8SMibeW54gjZbikL-JBHqhEwc22Cp8DNOtadXsM81RGQ";
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private static final Provider bcProvider = new BouncyCastleProvider();

    //JOSE Signatures obtained using Node 'jwa' lib: https://github.com/brianloveswords/node-jwa
    //DER Signatures obtained from source JOSE signature using 'ecdsa-sig-formatter' lib: https://github.com/Brightspace/node-ecdsa-sig-formatter


    //These tests add and use the BouncyCastle SecurityProvider to handle ECDSA algorithms

    @BeforeClass
    public static void setUp() {
        //Set BC as the preferred bcProvider
        Security.insertProviderAt(bcProvider, 1);
    }

    @AfterClass
    public static void tearDown() {
        Security.removeProvider(bcProvider.getName());
    }

    @Test
    public void shouldPreferBouncyCastleProvider() {
        assertThat(Security.getProviders()[0], is(equalTo(bcProvider)));
    }
    
    @Test
    public void shouldPassECDSA256VerificationWithJOSESignature() throws Exception {
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.4iVk3-Y0v4RT4_9IaQlp-8dZ_4fsTzIylgrPTDLrEvTHBTyVS3tgPbr2_IZfLETtiKRqCg0aQ5sh9eIsTTwB1g";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        Algorithm algorithm = Algorithm.ECDSA256(key);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnECDSA256VerificationWithDERSignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.MEYCIQDiJWTf5jShFPj0hpCWn7x1nhxPMjKWCs9MMusS9AIhAMcFPJVLe2A9uvb8hl8sRO2IpGoKDRpDmyH14ixNPAHW";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        Algorithm algorithm = Algorithm.ECDSA256(key);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA256VerificationWithJOSESignatureWithBothKeys() throws Exception {
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.4iVk3-Y0v4RT4_9IaQlp-8dZ_4fsTzIylgrPTDLrEvTHBTyVS3tgPbr2_IZfLETtiKRqCg0aQ5sh9eIsTTwB1g";
        Algorithm algorithm = Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnECDSA256VerificationWithDERSignatureWithBothKeys() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.MEYCIQDiJWTf5jShFPj0hpCWn7x1nhxPMjKWCs9MMusS9AIhAMcFPJVLe2A9uvb8hl8sRO2IpGoKDRpDmyH14ixNPAHW";
        Algorithm algorithm = Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA256VerificationWithProvidedPublicKey() throws Exception {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        PublicKey publicKey = readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        when(provider.getPublicKeyById("my-key-id")).thenReturn((ECPublicKey) publicKey);
        String jwt = "eyJhbGciOiJFUzI1NiIsImtpZCI6Im15LWtleS1pZCJ9.eyJpc3MiOiJhdXRoMCJ9.D_oU4CB0ZEsxHOjcWnmS3ZJvlTzm6WcGFx-HASxnvcB2Xu2WjI-axqXH9xKq45aPBDs330JpRhJmqBSc2K8MXQ";
        Algorithm algorithm = Algorithm.ECDSA256(provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA256VerificationWhenProvidedPublicKeyIsNull() {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Public Key is null.")));
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPublicKeyById("my-key-id")).thenReturn(null);
        String jwt = "eyJhbGciOiJFUzI1NiIsImtpZCI6Im15LWtleS1pZCJ9.eyJpc3MiOiJhdXRoMCJ9.D_oU4CB0ZEsxHOjcWnmS3ZJvlTzm6WcGFx-HASxnvcB2Xu2WjI-axqXH9xKq45aPBDs330JpRhJmqBSc2K8MXQ";
        Algorithm algorithm = Algorithm.ECDSA256(provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA256VerificationWithInvalidPublicKey() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.W9qfN1b80B9hnMo49WL8THrOsf1vEjOhapeFemPMGySzxTcgfyudS5esgeBTO908X5SLdAr5jMwPUPBs9b6nNg";
        Algorithm algorithm = Algorithm.ECDSA256((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA256VerificationWhenUsingPrivateKey() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Public Key is null.")));
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.W9qfN1b80B9hnMo49WL8THrOsf1vEjOhapeFemPMGySzxTcgfyudS5esgeBTO908X5SLdAr5jMwPUPBs9b6nNg";
        Algorithm algorithm = Algorithm.ECDSA256((ECKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA256VerificationOnInvalidJOSESignatureLength() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        byte[] bytes = new byte[63];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA256((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA256VerificationOnInvalidJOSESignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");

        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA256((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA256VerificationOnInvalidDERSignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");

        byte[] bytes = new byte[64];
        bytes[0] = 0x30;
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA256((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_256, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA384VerificationWithJOSESignature() throws Exception {
        String jwt = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9.50UU5VKNdF1wfykY8jQBKpvuHZoe6IZBJm5NvoB8bR-hnRg6ti-CHbmvoRtlLfnHfwITa_8cJMy6TenMC2g63GQHytc8rYoXqbwtS4R0Ko_AXbLFUmfxnGnMC6v4MS_z";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC");
        Algorithm algorithm = Algorithm.ECDSA384(key);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnECDSA384VerificationWithDERSignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        String jwt = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9.MGUCMQDnRRTlUo10XXBKRjyNAEqm4dmh7ohkEmbk2gHxtH6GdGDq2L4IduahG2UtccCMH8CE2vHCTMuk3pzAtoOtxkB8rXPK2KF6m8LUuEdCqPwF2yxVJn8ZxpzAurDEv8w";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC");
        Algorithm algorithm = Algorithm.ECDSA384(key);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA384VerificationWithJOSESignatureWithBothKeys() throws Exception {
        String jwt = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9.50UU5VKNdF1wfykY8jQBKpvuHZoe6IZBJm5NvoB8bR-hnRg6ti-CHbmvoRtlLfnHfwITa_8cJMy6TenMC2g63GQHytc8rYoXqbwtS4R0Ko_AXbLFUmfxnGnMC6v4MS_z";
        Algorithm algorithm = Algorithm.ECDSA384((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnECDSA384VerificationWithDERSignatureWithBothKeys() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        String jwt = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9.MGUCMQDnRRTlUo10XXBKRjyNAEqm4dmh7ohkEmbk2gHxtH6GdGDq2L4IduahG2UtccCMH8CE2vHCTMuk3pzAtoOtxkB8rXPK2KF6m8LUuEdCqPwF2yxVJn8ZxpzAurDEv8w";
        Algorithm algorithm = Algorithm.ECDSA384((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA384VerificationWithProvidedPublicKey() throws Exception {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        PublicKey publicKey = readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC");
        when(provider.getPublicKeyById("my-key-id")).thenReturn((ECPublicKey) publicKey);
        String jwt = "eyJhbGciOiJFUzM4NCIsImtpZCI6Im15LWtleS1pZCJ9.eyJpc3MiOiJhdXRoMCJ9.9kjGuFTPx3ylfpqL0eY9H7TGmPepjQOBKI8UPoEvby6N7dDLF5HxLohosNxxFymNT7LzpeSgOPAB0wJEwG2Nl2ukgdUOpZOf492wog_i5ZcZmAykd3g1QH7onrzd69GU";
        Algorithm algorithm = Algorithm.ECDSA384(provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA384VerificationWhenProvidedPublicKeyIsNull() {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Public Key is null.")));
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPublicKeyById("my-key-id")).thenReturn(null);
        String jwt = "eyJhbGciOiJFUzM4NCIsImtpZCI6Im15LWtleS1pZCJ9.eyJpc3MiOiJhdXRoMCJ9.9kjGuFTPx3ylfpqL0eY9H7TGmPepjQOBKI8UPoEvby6N7dDLF5HxLohosNxxFymNT7LzpeSgOPAB0wJEwG2Nl2ukgdUOpZOf492wog_i5ZcZmAykd3g1QH7onrzd69GU";
        Algorithm algorithm = Algorithm.ECDSA384(provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA384VerificationWithInvalidPublicKey() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");
        String jwt = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9._k5h1KyO-NE0R2_HAw0-XEc0bGT5atv29SxHhOGC9JDqUHeUdptfCK_ljQ01nLVt2OQWT2SwGs-TuyHDFmhPmPGFZ9wboxvq_ieopmYqhQilNAu-WF-frioiRz9733fU";
        Algorithm algorithm = Algorithm.ECDSA384((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA384VerificationWhenUsingPrivateKey() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Public Key is null.")));
        String jwt = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9._k5h1KyO-NE0R2_HAw0-XEc0bGT5atv29SxHhOGC9JDqUHeUdptfCK_ljQ01nLVt2OQWT2SwGs-TuyHDFmhPmPGFZ9wboxvq_ieopmYqhQilNAu-WF-frioiRz9733fU";
        Algorithm algorithm = Algorithm.ECDSA384((ECKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA384VerificationOnInvalidJOSESignatureLength() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        byte[] bytes = new byte[95];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA384((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA384VerificationOnInvalidJOSESignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");

        byte[] bytes = new byte[96];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA384((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA384VerificationOnInvalidDERSignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA384withECDSA");

        byte[] bytes = new byte[96];
        new SecureRandom().nextBytes(bytes);
        bytes[0] = 0x30;
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA384((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_384, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA512VerificationWithJOSESignature() throws Exception {
        String jwt = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.AeCJPDIsSHhwRSGZCY6rspi8zekOw0K9qYMNridP1Fu9uhrA1QrG-EUxXlE06yvmh2R7Rz0aE7kxBwrnq8L8aOBCAYAsqhzPeUvyp8fXjjgs0Eto5I0mndE2QHlgcMSFASyjHbU8wD2Rq7ZNzGQ5b2MZfpv030WGUajT-aZYWFUJHVg2";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC");
        Algorithm algorithm = Algorithm.ECDSA512(key);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnECDSA512VerificationWithDERSignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        String jwt = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.MIGIAkIB4Ik8MixIeHBFIZkJjquymLzN6Q7DQr2pgw2uJ0UW726GsDVCsb4RTFeUTTrKaHZHtHPRoTuTEHCuerwvxo4EICQgGALKocz3lL8qfH1444LNBLaOSNJp3RNkB5YHDEhQEsox21PMA9kau2TcxkOW9jGX6b9N9FhlGo0mmWFhVCR1YNg";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC");
        Algorithm algorithm = Algorithm.ECDSA512(key);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA512VerificationWithJOSESignatureWithBothKeys() throws Exception {
        String jwt = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.AeCJPDIsSHhwRSGZCY6rspi8zekOw0K9qYMNridP1Fu9uhrA1QrG-EUxXlE06yvmh2R7Rz0aE7kxBwrnq8L8aOBCAYAsqhzPeUvyp8fXjjgs0Eto5I0mndE2QHlgcMSFASyjHbU8wD2Rq7ZNzGQ5b2MZfpv030WGUajT-aZYWFUJHVg2";
        Algorithm algorithm = Algorithm.ECDSA512((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowECDSA512VerificationWithDERSignatureWithBothKeys() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        String jwt = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.MIGIAkIB4Ik8MixIeHBFIZkJjquymLzN6Q7DQr2pgw2uJ0UW726GsDVCsb4RTFeUTTrKaHZHtHPRoTuTEHCuerwvxo4EICQgGALKocz3lL8qfH1444LNBLaOSNJp3RNkB5YHDEhQEsox21PMA9kau2TcxkOW9jGX6b9N9FhlGo0mmWFhVCR1YNg";
        Algorithm algorithm = Algorithm.ECDSA512((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldPassECDSA512VerificationWithProvidedPublicKey() throws Exception {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        PublicKey publicKey = readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC");
        when(provider.getPublicKeyById("my-key-id")).thenReturn((ECPublicKey) publicKey);
        String jwt = "eyJhbGciOiJFUzUxMiIsImtpZCI6Im15LWtleS1pZCJ9.eyJpc3MiOiJhdXRoMCJ9.AGxEwbsYa2bQ7Y7DAcTQnVD8PmLSlhJ20jg2OfdyPnqdXI8SgBaG6lGciq3_pofFhs1HEoFoJ33Jcluha24oMHIvAfwu8qbv_Wq3L2eI9Q0L0p6ul8Pd_BS8adRa2PgLc36xXGcRc7ID5YH-CYaQfsTp5YIaF0Po3h0QyCoQ6ZiYQkqm";
        Algorithm algorithm = Algorithm.ECDSA512(provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA512VerificationWhenProvidedPublicKeyIsNull() {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Public Key is null.")));
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPublicKeyById("my-key-id")).thenReturn(null);
        String jwt = "eyJhbGciOiJFUzUxMiIsImtpZCI6Im15LWtleS1pZCJ9.eyJpc3MiOiJhdXRoMCJ9.AGxEwbsYa2bQ7Y7DAcTQnVD8PmLSlhJ20jg2OfdyPnqdXI8SgBaG6lGciq3_pofFhs1HEoFoJ33Jcluha24oMHIvAfwu8qbv_Wq3L2eI9Q0L0p6ul8Pd_BS8adRa2PgLc36xXGcRc7ID5YH-CYaQfsTp5YIaF0Po3h0QyCoQ6ZiYQkqm";
        Algorithm algorithm = Algorithm.ECDSA512(provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA512VerificationWithInvalidPublicKey() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");
        String jwt = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.AZgdopFFsN0amCSs2kOucXdpylD31DEm5ChK1PG0_gq5Mf47MrvVph8zHSVuvcrXzcE1U3VxeCg89mYW1H33Y-8iAF0QFkdfTUQIWKNObH543WNMYYssv3OtOj0znPv8atDbaF8DMYAtcT1qdmaSJRhx-egRE9HGZkinPh9CfLLLt58X";
        Algorithm algorithm = Algorithm.ECDSA512((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA512VerificationWhenUsingPrivateKey() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Public Key is null.")));
        String jwt = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.AZgdopFFsN0amCSs2kOucXdpylD31DEm5ChK1PG0_gq5Mf47MrvVph8zHSVuvcrXzcE1U3VxeCg89mYW1H33Y-8iAF0QFkdfTUQIWKNObH543WNMYYssv3OtOj0znPv8atDbaF8DMYAtcT1qdmaSJRhx-egRE9HGZkinPh9CfLLLt58X";
        Algorithm algorithm = Algorithm.ECDSA512((ECKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA512VerificationOnInvalidJOSESignatureLength() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        byte[] bytes = new byte[131];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA512((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA512VerificationOnInvalidJOSESignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");

        byte[] bytes = new byte[132];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA512((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailECDSA512VerificationOnInvalidDERSignature() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA512withECDSA");

        byte[] bytes = new byte[132];
        new SecureRandom().nextBytes(bytes);
        bytes[0] = 0x30;
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;
        Algorithm algorithm = Algorithm.ECDSA512((ECKey) readPublicKeyFromFile(INVALID_PUBLIC_KEY_FILE_512, "EC"));
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailJOSEToDERConversionOnInvalidJOSESignatureLength() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(SignatureException.class));
        exception.expectCause(hasMessage(is("Invalid JOSE signature format.")));

        byte[] bytes = new byte[256];
        new SecureRandom().nextBytes(bytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9." + signature;

        ECPublicKey publicKey = (ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm("ES256", "SHA256withECDSA", 128, provider);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnVerifyWhenSignatureAlgorithmDoesNotExists() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: some-alg");
        exception.expectCause(isA(NoSuchAlgorithmException.class));

        CryptoHelper crypto = mock(CryptoHelper.class);
        when(crypto.verifySignatureFor(anyString(), any(PublicKey.class), any(String.class), any(String.class), any(byte[].class), (Provider) ArgumentMatchers.isNull()))
                .thenThrow(NoSuchAlgorithmException.class);

        ECPublicKey publicKey = mock(ECPublicKey.class);
        when(publicKey.getParams()).thenReturn(mock(ECParameterSpec.class));
        byte[] a = new byte[64];
        Arrays.fill(a, Byte.MAX_VALUE);
        when(publicKey.getParams().getOrder()).thenReturn(new BigInteger(a));
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm(crypto, "some-alg", "some-algorithm", 32, provider);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.4iVk3-Y0v4RT4_9IaQlp-8dZ_4fsTzIylgrPTDLrEvTHBTyVS3tgPbr2_IZfLETtiKRqCg0aQ5sh9eIsTTwB1g";
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnVerifyWhenThePublicKeyIsInvalid() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: some-alg");
        exception.expectCause(isA(InvalidKeyException.class));

        CryptoHelper crypto = mock(CryptoHelper.class);
        when(crypto.verifySignatureFor(anyString(), any(PublicKey.class), any(String.class), any(String.class), any(byte[].class), (Provider) ArgumentMatchers.isNull()))
                .thenThrow(InvalidKeyException.class);

        ECPublicKey publicKey = mock(ECPublicKey.class);
        when(publicKey.getParams()).thenReturn(mock(ECParameterSpec.class));
        byte[] a = new byte[64];
        Arrays.fill(a, Byte.MAX_VALUE);
        when(publicKey.getParams().getOrder()).thenReturn(new BigInteger(a));
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm(crypto, "some-alg", "some-algorithm", 32, provider);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.4iVk3-Y0v4RT4_9IaQlp-8dZ_4fsTzIylgrPTDLrEvTHBTyVS3tgPbr2_IZfLETtiKRqCg0aQ5sh9eIsTTwB1g";
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldThrowOnVerifyWhenTheSignatureIsNotPrepared() throws Exception {
        exception.expect(SignatureVerificationException.class);
        exception.expectMessage("The Token's Signature resulted invalid when verified using the Algorithm: some-alg");
        exception.expectCause(isA(SignatureException.class));

        CryptoHelper crypto = mock(CryptoHelper.class);
        when(crypto.verifySignatureFor(anyString(), any(PublicKey.class), any(String.class), any(String.class), any(byte[].class), (Provider) ArgumentMatchers.isNull()))
                .thenThrow(SignatureException.class);

        ECPublicKey publicKey = mock(ECPublicKey.class);
        when(publicKey.getParams()).thenReturn(mock(ECParameterSpec.class));
        byte[] a = new byte[64];
        Arrays.fill(a, Byte.MAX_VALUE);
        when(publicKey.getParams().getOrder()).thenReturn(new BigInteger(a));
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm(crypto, "some-alg", "some-algorithm", 32, provider);
        String jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.4iVk3-Y0v4RT4_9IaQlp-8dZ_4fsTzIylgrPTDLrEvTHBTyVS3tgPbr2_IZfLETtiKRqCg0aQ5sh9eIsTTwB1g";
        algorithm.verify(JWT.decode(jwt));
    }

    //Sign
    private static final String ES256Header = "eyJhbGciOiJFUzI1NiJ9";
    private static final String ES384Header = "eyJhbGciOiJFUzM4NCJ9";
    private static final String ES512Header = "eyJhbGciOiJFUzUxMiJ9";
    private static final String auth0IssPayload = "eyJpc3MiOiJhdXRoMCJ9";

    @Test
    public void shouldDoECDSA256Signing() throws Exception {
        Algorithm algorithmSign = Algorithm.ECDSA256((ECKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        Algorithm algorithmVerify = Algorithm.ECDSA256((ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"));
        String jwt = asJWT(algorithmSign, ES256Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithmVerify.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldDoECDSA256SigningWithBothKeys() throws Exception {
        Algorithm algorithm = Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        String jwt = asJWT(algorithm, ES256Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldDoECDSA256SigningWithProvidedPrivateKey() throws Exception {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        PrivateKey privateKey = readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC");
        PublicKey publicKey = readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        when(provider.getPrivateKey()).thenReturn((ECPrivateKey) privateKey);
        when(provider.getPublicKeyById(null)).thenReturn((ECPublicKey) publicKey);
        Algorithm algorithm = Algorithm.ECDSA256(provider);
        
        String jwt = asJWT(algorithm, ES256Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailOnECDSA256SigningWhenProvidedPrivateKeyIsNull() {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Private Key is null.")));

        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPrivateKey()).thenReturn(null);
        Algorithm algorithm = Algorithm.ECDSA256(provider);
        algorithm.sign(new byte[0], new byte[0]);
    }

    @Test
    public void shouldFailOnECDSA256SigningWhenUsingPublicKey() throws Exception {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: SHA256withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Private Key is null.")));

        Algorithm algorithm = Algorithm.ECDSA256((ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"));
        algorithm.sign(new byte[0], new byte[0]);
    }

    @Test
    public void shouldDoECDSA384Signing() throws Exception {
        Algorithm algorithmSign = Algorithm.ECDSA384((ECKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));
        Algorithm algorithmVerify = Algorithm.ECDSA384((ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"));
        String jwt = asJWT(algorithmSign, ES384Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithmVerify.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldDoECDSA384SigningWithBothKeys() throws Exception {
        Algorithm algorithm = Algorithm.ECDSA384((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));
        String jwt = asJWT(algorithm, ES384Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldDoECDSA384SigningWithProvidedPrivateKey() throws Exception {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        PrivateKey privateKey = readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC");
        PublicKey publicKey = readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC");
        when(provider.getPrivateKey()).thenReturn((ECPrivateKey) privateKey);
        when(provider.getPublicKeyById(null)).thenReturn((ECPublicKey) publicKey);
        Algorithm algorithm = Algorithm.ECDSA384(provider);
        
        String jwt = asJWT(algorithm, ES384Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailOnECDSA384SigningWhenProvidedPrivateKeyIsNull() {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Private Key is null.")));

        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPrivateKey()).thenReturn(null);
        Algorithm algorithm = Algorithm.ECDSA384(provider);
        algorithm.sign(new byte[0], new byte[0]);
    }

    @Test
    public void shouldFailOnECDSA384SigningWhenUsingPublicKey() throws Exception {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: SHA384withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Private Key is null.")));

        Algorithm algorithm = Algorithm.ECDSA384((ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"));
        algorithm.sign(new byte[0], new byte[0]);
    }

    @Test
    public void shouldDoECDSA512Signing() throws Exception {
        Algorithm algorithmSign = Algorithm.ECDSA512((ECKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));
        Algorithm algorithmVerify = Algorithm.ECDSA512((ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"));
        
        String jwt = asJWT(algorithmSign, ES512Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithmVerify.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldDoECDSA512SigningWithBothKeys() throws Exception {
        Algorithm algorithm = Algorithm.ECDSA512((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));
        String jwt = asJWT(algorithm, ES512Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithm.verify(JWT.decode(jwt));
    }


    @Test
    public void shouldDoECDSA512SigningWithProvidedPrivateKey() throws Exception {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        PrivateKey privateKey = readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC");
        PublicKey publicKey = readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC");
        when(provider.getPrivateKey()).thenReturn((ECPrivateKey) privateKey);
        when(provider.getPublicKeyById(null)).thenReturn((ECPublicKey) publicKey);
        Algorithm algorithm = Algorithm.ECDSA512(provider);
        String jwt = asJWT(algorithm, ES512Header, auth0IssPayload);

        assertSignaturePresent(jwt);
        algorithm.verify(JWT.decode(jwt));
    }

    @Test
    public void shouldFailOnECDSA512SigningWhenProvidedPrivateKeyIsNull() {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Private Key is null.")));

        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPrivateKey()).thenReturn(null);
        Algorithm algorithm = Algorithm.ECDSA512(provider);
        algorithm.sign(new byte[0], new byte[0]);
    }

    @Test
    public void shouldFailOnECDSA512SigningWhenUsingPublicKey() throws Exception {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: SHA512withECDSA");
        exception.expectCause(isA(IllegalStateException.class));
        exception.expectCause(hasMessage(is("The given Private Key is null.")));

        Algorithm algorithm = Algorithm.ECDSA512((ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"));
        algorithm.sign(new byte[0], new byte[0]);
    }

    @Test
    public void shouldThrowOnSignWhenSignatureAlgorithmDoesNotExists() throws Exception {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: some-algorithm");
        exception.expectCause(isA(NoSuchAlgorithmException.class));

        CryptoHelper crypto = mock(CryptoHelper.class);
        when(crypto.createSignatureFor(anyString(), any(PrivateKey.class), any(byte[].class), (Provider) ArgumentMatchers.isNull()))
                .thenThrow(NoSuchAlgorithmException.class);

        ECPublicKey publicKey = mock(ECPublicKey.class);
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm(crypto, "some-alg", "some-algorithm", 32, provider);
        algorithm.sign(ES256Header.getBytes(StandardCharsets.UTF_8), new byte[0]);
    }

    @Test
    public void shouldThrowOnSignWhenThePrivateKeyIsInvalid() throws Exception {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: some-algorithm");
        exception.expectCause(isA(InvalidKeyException.class));

        CryptoHelper crypto = mock(CryptoHelper.class);
        when(crypto.createSignatureFor(anyString(), any(PrivateKey.class), any(byte[].class), (Provider) ArgumentMatchers.isNull()))
                .thenThrow(InvalidKeyException.class);

        ECPublicKey publicKey = mock(ECPublicKey.class);
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm(crypto, "some-alg", "some-algorithm", 32, provider);
        algorithm.sign(ES256Header.getBytes(StandardCharsets.UTF_8), new byte[0]);
    }

    @Test
    public void shouldThrowOnSignWhenTheSignatureIsNotPrepared() throws Exception {
        exception.expect(SignatureGenerationException.class);
        exception.expectMessage("The Token's Signature couldn't be generated when signing using the Algorithm: some-algorithm");
        exception.expectCause(isA(SignatureException.class));

        CryptoHelper crypto = mock(CryptoHelper.class);
        when(crypto.createSignatureFor(anyString(), any(PrivateKey.class), any(byte[].class), (Provider) ArgumentMatchers.isNull()))
                .thenThrow(SignatureException.class);

        ECPublicKey publicKey = mock(ECPublicKey.class);
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm(crypto, "some-alg", "some-algorithm", 32, provider);
        algorithm.sign(ES256Header.getBytes(StandardCharsets.UTF_8), new byte[0]);
    }

    @Test
    public void shouldReturnNullSigningKeyIdIfCreatedWithDefaultProvider() {
        ECPublicKey publicKey = mock(ECPublicKey.class);
        ECPrivateKey privateKey = mock(ECPrivateKey.class);
        ECDSAKeyProvider provider = ECDSAAlgorithm.providerForKeys(publicKey, privateKey);
        Algorithm algorithm = new ECDSAAlgorithm("some-alg", "some-algorithm", 32, provider);

        assertThat(algorithm.getSigningKeyId(), is(nullValue()));
    }

    @Test
    public void shouldReturnSigningKeyIdFromProvider() {
        ECDSAKeyProvider provider = mock(ECDSAKeyProvider.class);
        when(provider.getPrivateKeyId()).thenReturn("keyId");
        Algorithm algorithm = new ECDSAAlgorithm("some-alg", "some-algorithm", 32, provider);

        assertThat(algorithm.getSigningKeyId(), is("keyId"));
    }

    @Test
    public void shouldThrowOnDERSignatureConversionIfDoesNotStartWithCorrectSequenceByte() throws Exception {
        exception.expect(SignatureException.class);
        exception.expectMessage("Invalid DER signature format.");

        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        String content256 = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9";

        byte[] signature = algorithm256.sign(content256.getBytes(), new byte[0]);
        signature[0] = (byte) 0x02;
        algorithm256.DERToJOSE(signature);
    }

    @Test
    public void shouldThrowOnDERSignatureConversionIfDoesNotHaveExpectedLength() throws Exception {
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        byte[] derSignature = createDERSignature(32, false, false);
        int received = (int) derSignature[1];
        received--;
        derSignature[1] = (byte) received;
        exception.expect(SignatureException.class);
        exception.expectMessage("Invalid DER signature format.");

        algorithm256.DERToJOSE(derSignature);
    }

    @Test
    public void shouldThrowOnDERSignatureConversionIfRNumberDoesNotHaveExpectedLength() throws Exception {
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        byte[] derSignature = createDERSignature(32, false, false);
        derSignature[3] = (byte) 34;
        exception.expect(SignatureException.class);
        exception.expectMessage("Invalid DER signature format.");

        algorithm256.DERToJOSE(derSignature);
    }

    @Test
    public void shouldThrowOnDERSignatureConversionIfSNumberDoesNotHaveExpectedLength() throws Exception {
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        byte[] derSignature = createDERSignature(32, false, false);
        derSignature[4 + 32 + 1] = (byte) 34;
        exception.expect(SignatureException.class);
        exception.expectMessage("Invalid DER signature format.");

        algorithm256.DERToJOSE(derSignature);
    }

    @Test
    public void shouldThrowOnJOSESignatureConversionIfDoesNotHaveExpectedLength() throws Exception {
        ECPublicKey publicKey = (ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256(publicKey, (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        byte[] joseSignature = new byte[32 * 2 - 1];
        exception.expect(SignatureException.class);
        exception.expectMessage("Invalid JOSE signature format.");

        algorithm256.validateSignatureStructure(joseSignature, publicKey);
    }

    @Test
    public void shouldSignAndVerifyWithECDSA256() throws Exception {
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));
        String header256 = "eyJhbGciOiJFUzI1NiJ9";
        String body = "eyJpc3MiOiJhdXRoMCJ9";

        for (int i = 0; i < 10; i++) {
            String jwt = asJWT(algorithm256, header256, body);
            algorithm256.verify(JWT.decode(jwt));
        }
    }

    @Test
    public void shouldSignAndVerifyWithECDSA384() throws Exception {
        ECDSAAlgorithm algorithm384 = (ECDSAAlgorithm) Algorithm.ECDSA384((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));
        String header384 = "eyJhbGciOiJFUzM4NCJ9";
        String body = "eyJpc3MiOiJhdXRoMCJ9";

        for (int i = 0; i < 10; i++) {
            String jwt = asJWT(algorithm384, header384, body);
            algorithm384.verify(JWT.decode(jwt));
        }
    }

    @Test
    public void shouldSignAndVerifyWithECDSA512() throws Exception {
        ECDSAAlgorithm algorithm512 = (ECDSAAlgorithm) Algorithm.ECDSA512((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));
        String header512 = "eyJhbGciOiJFUzUxMiJ9";
        String body = "eyJpc3MiOiJhdXRoMCJ9";

        for (int i = 0; i < 10; i++) {
            String jwt = asJWT(algorithm512, header512, body);
            algorithm512.verify(JWT.decode(jwt));
        }
    }

    @Test
    public void shouldDecodeECDSA256JOSE() throws Exception {
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));

        //Without padding
        byte[] joseSignature = createJOSESignature(32, false, false);
        byte[] derSignature = algorithm256.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 32, false, false);

        //With R padding
        joseSignature = createJOSESignature(32, true, false);
        derSignature = algorithm256.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 32, true, false);

        //With S padding
        joseSignature = createJOSESignature(32, false, true);
        derSignature = algorithm256.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 32, false, true);

        //With both paddings
        joseSignature = createJOSESignature(32, true, true);
        derSignature = algorithm256.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 32, true, true);
    }

    @Test
    public void shouldDecodeECDSA256DER() throws Exception {
        ECDSAAlgorithm algorithm256 = (ECDSAAlgorithm) Algorithm.ECDSA256((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_256, "EC"));

        //Without padding
        byte[] derSignature = createDERSignature(32, false, false);
        byte[] joseSignature = algorithm256.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 32, false, false);

        //With R padding
        derSignature = createDERSignature(32, true, false);
        joseSignature = algorithm256.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 32, true, false);

        //With S padding
        derSignature = createDERSignature(32, false, true);
        joseSignature = algorithm256.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 32, false, true);

        //With both paddings
        derSignature = createDERSignature(32, true, true);
        joseSignature = algorithm256.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 32, true, true);
    }

    @Test
    public void shouldDecodeECDSA384JOSE() throws Exception {
        ECDSAAlgorithm algorithm384 = (ECDSAAlgorithm) Algorithm.ECDSA384((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));

        //Without padding
        byte[] joseSignature = createJOSESignature(48, false, false);
        byte[] derSignature = algorithm384.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 48, false, false);

        //With R padding
        joseSignature = createJOSESignature(48, true, false);
        derSignature = algorithm384.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 48, true, false);

        //With S padding
        joseSignature = createJOSESignature(48, false, true);
        derSignature = algorithm384.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 48, false, true);

        //With both paddings
        joseSignature = createJOSESignature(48, true, true);
        derSignature = algorithm384.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 48, true, true);
    }

    @Test
    public void shouldDecodeECDSA384DER() throws Exception {
        ECDSAAlgorithm algorithm384 = (ECDSAAlgorithm) Algorithm.ECDSA384((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_384, "EC"));

        //Without padding
        byte[] derSignature = createDERSignature(48, false, false);
        byte[] joseSignature = algorithm384.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 48, false, false);

        //With R padding
        derSignature = createDERSignature(48, true, false);
        joseSignature = algorithm384.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 48, true, false);

        //With S padding
        derSignature = createDERSignature(48, false, true);
        joseSignature = algorithm384.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 48, false, true);

        //With both paddings
        derSignature = createDERSignature(48, true, true);
        joseSignature = algorithm384.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 48, true, true);
    }

    @Test
    public void shouldDecodeECDSA512JOSE() throws Exception {
        ECDSAAlgorithm algorithm512 = (ECDSAAlgorithm) Algorithm.ECDSA512((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));

        //Without padding
        byte[] joseSignature = createJOSESignature(66, false, false);
        byte[] derSignature = algorithm512.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 66, false, false);

        //With R padding
        joseSignature = createJOSESignature(66, true, false);
        derSignature = algorithm512.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 66, true, false);

        //With S padding
        joseSignature = createJOSESignature(66, false, true);
        derSignature = algorithm512.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 66, false, true);

        //With both paddings
        joseSignature = createJOSESignature(66, true, true);
        derSignature = algorithm512.JOSEToDER(joseSignature);
        assertValidDERSignature(derSignature, 66, true, true);
    }

    @Test
    public void shouldDecodeECDSA512DER() throws Exception {
        ECDSAAlgorithm algorithm512 = (ECDSAAlgorithm) Algorithm.ECDSA512((ECPublicKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC"), (ECPrivateKey) readPrivateKeyFromFile(PRIVATE_KEY_FILE_512, "EC"));

        //Without padding
        byte[] derSignature = createDERSignature(66, false, false);
        byte[] joseSignature = algorithm512.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 66, false, false);

        //With R padding
        derSignature = createDERSignature(66, true, false);
        joseSignature = algorithm512.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 66, true, false);

        //With S padding
        derSignature = createDERSignature(66, false, true);
        joseSignature = algorithm512.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 66, false, true);

        //With both paddings
        derSignature = createDERSignature(66, true, true);
        joseSignature = algorithm512.DERToJOSE(derSignature);
        assertValidJOSESignature(joseSignature, 66, true, true);
    }

    
}

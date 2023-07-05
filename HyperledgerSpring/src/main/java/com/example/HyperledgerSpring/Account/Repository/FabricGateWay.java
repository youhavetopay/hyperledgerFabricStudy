package com.example.HyperledgerSpring.Account.Repository;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import jakarta.annotation.PreDestroy;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

@Component
public class FabricGateWay {

    private static ManagedChannel channel;
    private static Gateway.Builder builder;

    private static String MSP_ID;
    private static Path CRYPTO_PATH;
    private static Path CERT_PATH;
    private static Path KEY_DIR_PATH;
    private static Path TLS_CERT_PATH;

    private static String PEER_END_POINT;
    private static String OVERRIDE_AUTH;

    public FabricGateWay(){

        MSP_ID = "Org1MSP";

        CRYPTO_PATH = Paths.get("/Users/creativehill/Desktop/hyperledger-sample/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com");
        CERT_PATH = CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/signcerts/cert.pem"));
        KEY_DIR_PATH = CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/keystore"));
        TLS_CERT_PATH = CRYPTO_PATH.resolve(Paths.get("peers/peer0.org1.example.com/tls/ca.crt"));

        PEER_END_POINT = "localhost:7051";
        OVERRIDE_AUTH = "peer0.org1.example.com";

        this.channel = newGrpcConnection();
        this.builder = initBuilder();
    }


    public Gateway connection() {
        return this.builder.connect();
    }

    @PreDestroy
    public void channelDown() throws InterruptedException {
        this.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    private static Gateway.Builder initBuilder() {

        try {
            return Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
                    // Default timeouts for different gRPC calls
                    .evaluateOptions(options -> options.withDeadlineAfter(10, TimeUnit.SECONDS))
                    .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                    .submitOptions(options -> options.withDeadlineAfter(10, TimeUnit.SECONDS))
                    .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        } catch (CertificateException certificateException){
            certificateException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    private static Identity newIdentity() throws IOException, CertificateException {
        var certReader = Files.newBufferedReader(CERT_PATH);
        var certificate = Identities.readX509Certificate(certReader);

        return new X509Identity(MSP_ID, certificate);
    }

    private static Signer newSigner() throws IOException, InvalidKeyException {
        var keyReader = Files.newBufferedReader(getPrivateKeyPath());
        var privateKey = Identities.readPrivateKey(keyReader);

        return Signers.newPrivateKeySigner(privateKey);
    }

    private static Path getPrivateKeyPath() throws IOException {
        try (var keyFiles = Files.list(KEY_DIR_PATH)){
            return keyFiles.findFirst().orElseThrow();
        }
    }


    private static ManagedChannel newGrpcConnection() {
        try {
            var credentials = TlsChannelCredentials.newBuilder()
                    .trustManager(TLS_CERT_PATH.toFile())
                    .build();

            return Grpc.newChannelBuilder(PEER_END_POINT, credentials)
                    .overrideAuthority(OVERRIDE_AUTH)
                    .build();
        } catch (IOException ioException){
            ioException.printStackTrace();
        }

        return null;
    }

}

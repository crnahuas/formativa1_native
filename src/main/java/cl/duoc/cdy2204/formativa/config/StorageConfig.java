package cl.duoc.cdy2204.formativa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfig {

    @Bean
    public S3Client s3Client(
            @Value("${aws.region}") String region,
            AwsCredentialsProvider awsCredentialsProvider
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey,
            @Value("${aws.session-token:}") String sessionToken
    ) {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            return DefaultCredentialsProvider.create();
        }

        if (sessionToken == null || sessionToken.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }

        return StaticCredentialsProvider.create(AwsSessionCredentials.create(accessKey, secretKey, sessionToken));
    }
}

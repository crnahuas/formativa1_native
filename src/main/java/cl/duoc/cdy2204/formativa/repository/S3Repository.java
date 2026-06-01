package cl.duoc.cdy2204.formativa.repository;

public interface S3Repository {

    void upload(String key, byte[] content, String contentType);

    boolean exists(String key);

    byte[] download(String key);

    void delete(String key);
}

package cl.duoc.cdy2204.formativa.dto;

public class S3Response {

    private String mensaje;
    private String bucket;
    private String carpeta;
    private String archivo;
    private String clave;

    public S3Response(String mensaje, String bucket, String carpeta, String archivo, String clave) {
        this.mensaje = mensaje;
        this.bucket = bucket;
        this.carpeta = carpeta;
        this.archivo = archivo;
        this.clave = clave;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getBucket() {
        return bucket;
    }

    public String getCarpeta() {
        return carpeta;
    }

    public String getArchivo() {
        return archivo;
    }

    public String getClave() {
        return clave;
    }
}

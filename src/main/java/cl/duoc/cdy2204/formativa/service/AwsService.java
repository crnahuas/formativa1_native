package cl.duoc.cdy2204.formativa.service;

import cl.duoc.cdy2204.formativa.dto.S3Response;
import org.springframework.web.multipart.MultipartFile;

public interface AwsService {

    S3Response uploadResumen(Long numeroResumen, MultipartFile file);

    S3Response updateResumen(Long numeroResumen, MultipartFile file);

    byte[] downloadResumen(Long numeroResumen);

    S3Response deleteResumen(Long numeroResumen);
}

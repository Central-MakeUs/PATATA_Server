package PATATA.domain.spot.service;

import PATATA.global.error.exception.S3ImageHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static PATATA.global.error.code.status.ErrorStatus.IMAGE_EMPTY;
import static PATATA.global.error.code.status.ErrorStatus.S3_UPLOAD_FAIL;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile image, String folder) {
        //입력받은 이미지 파일이 빈 파일인지 검증
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            log.info("s3 upload image is empty");
            throw new S3ImageHandler(IMAGE_EMPTY);
        }
        return uploadImage(image, folder);
    }

    private String uploadImage(MultipartFile image, String folder) {
        try {
            return uploadImageToS3(image, folder);
        } catch (IOException e) {
            log.error("S3 이미지 업로드 실패: {}", e.getMessage(), e);  // 스택 트레이스를 포함한 상세 로그
            throw new S3ImageHandler(S3_UPLOAD_FAIL);
        }
    }

    //S3에 파일 업로드
    private String uploadImageToS3(MultipartFile image, String folder) throws IOException {
        String s3FileName = UUID.randomUUID().toString().concat(".jpg");

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        //metadata 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType("image/jpeg");

        //S3에 요청할 때 사용할 byteInputStream 생성
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        //S3로 putObject 할 때 사용할 요청 객체
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, folder + s3FileName, byteArrayInputStream, metadata);;

        //put image to S3
        amazonS3.putObject(putObjectRequest);

        byteArrayInputStream.close();
        is.close();

        return amazonS3.getUrl(bucket, folder + s3FileName).toString();
    }

}

package PATATA.domain.spot.service;

import PATATA.domain.spot.dto.S3ImageUrlDto;
import PATATA.global.error.exception.S3ImageHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    public String uploadOriginal(MultipartFile image, String folder) {
            //입력받은 이미지 파일이 빈 파일인지 검증
            if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
                log.info("s3 upload image is empty");
                throw new S3ImageHandler(IMAGE_EMPTY);
            }
            log.info("프로필 사진 업데이트 중 ... ");
            return uploadOriginalImage(image, folder);
    }

    private String uploadOriginalImage(MultipartFile image, String folder) {
        try {
            return uploadImageToS3(image, folder);
        } catch (IOException e) {
            log.error("S3 이미지 업로드 실패: {}", e.getMessage(), e);  // 스택 트레이스를 포함한 상세 로그
            throw new S3ImageHandler(S3_UPLOAD_FAIL);
        }
    }

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
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, folder + s3FileName, byteArrayInputStream, metadata);
        ;
        //put image to S3
        amazonS3.putObject(putObjectRequest);

        byteArrayInputStream.close();
        is.close();

        return amazonS3.getUrl(bucket, folder + s3FileName).toString();
    }

    public S3ImageUrlDto upload(MultipartFile image, String folder) {
        //입력받은 이미지 파일이 빈 파일인지 검증
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            log.info("s3 upload image is empty");
            throw new S3ImageHandler(IMAGE_EMPTY);
        }
        log.info("이미지 리사이징 중...");
        return uploadImage(image, folder);
    }

    private S3ImageUrlDto uploadImage(MultipartFile image, String folder) {
        try {
            return uploadOriginalAndResizedImage(image, folder);
        } catch (IOException e) {
            log.error("S3 이미지 업로드 실패: {}", e.getMessage(), e);  // 스택 트레이스를 포함한 상세 로그
            throw new S3ImageHandler(S3_UPLOAD_FAIL);
        }
    }

    private S3ImageUrlDto uploadOriginalAndResizedImage(MultipartFile image, String folder) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originalFileName = folder + uuid + "_o.jpg";
        String resizedFileName = folder + uuid + "_r.jpg";

        // S3 업로드를 위한 input stream 재사용 불가 -> 각각 따로 열기
        try (InputStream originalInputStream = image.getInputStream()) {
            String originalUrl = uploadImageToS3(originalInputStream, originalFileName, image.getSize());

            // 리사이징용 input stream 다시 생성
            try (InputStream resizeInputStream = image.getInputStream()) {
                ByteArrayOutputStream resizedOutputStream = new ByteArrayOutputStream();

                Thumbnails.of(resizeInputStream)
                        .size(400, 400)
                        .outputFormat("jpg")
                        .outputQuality(1.0)
                        .toOutputStream(resizedOutputStream);

                byte[] resizedBytes = resizedOutputStream.toByteArray();
                String resizedUrl = uploadImageToS3(new ByteArrayInputStream(resizedBytes), resizedFileName, resizedBytes.length);

                return new S3ImageUrlDto(originalUrl, resizedUrl);
            }
        }

//        // original image upload
//        byte[] originalBytes;
//        try (InputStream is = image.getInputStream()) {
//            originalBytes = IOUtils.toByteArray(is);
//        }
//        String originalUrl = uploadImageToS3(originalBytes, originalFileName);
//
//        // resized image upload
//        byte[] resizedBytes = resizeImage(originalBytes, 400, 400);
//        String resizedUrl = uploadImageToS3(resizedBytes, resizedFileName);
//
//        return new S3ImageUrlDto(originalUrl, resizedUrl);
    }

    private byte[] resizeImage(byte[] originalBytes, int width, int height) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(originalBytes))
                    .size(width, height)
                    .outputFormat("jpg")
                    .outputQuality(1.0)
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        }
    }

    //S3에 파일 업로드
    private String uploadImageToS3(InputStream inputStream, String key, long contentLength) throws IOException {
        //metadata 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType("image/jpeg");

//        //S3에 요청할 때 사용할 byteInputStream 생성
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        //S3로 putObject 할 때 사용할 요청 객체
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, metadata);;

        //put image to S3
        amazonS3.putObject(putObjectRequest);
        return amazonS3.getUrl(bucket, key).toString();
    }

}

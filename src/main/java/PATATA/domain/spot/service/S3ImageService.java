package PATATA.domain.spot.service;

import PATATA.domain.spot.dto.S3ImageUrlDto;
import PATATA.global.error.exception.S3ImageHandler;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
            return uploadAndResize(image, folder);
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

//    private S3ImageUrlDto uploadOriginalAndResizedImage(MultipartFile image, String folder) throws IOException {
//        String uuid = UUID.randomUUID().toString();
//        String baseFileName = folder + uuid;
//
//        // 원본 업로드
//        String originalFileName = baseFileName + "_o.jpg";
//        String originalUrl;
//        try (InputStream originalInputStream = image.getInputStream()) {
//            originalUrl = uploadImageToS3(originalInputStream, originalFileName, image.getSize());
//        }
//
//        // 리사이징 이미지 생성 및 업로드
//        Map<Integer, String> resizedUrls = new HashMap<>();
//        int[] sizes = {400, 800, 1200};
//
//        for (int size : sizes) {
//            String resizedFileName = baseFileName + "_r" + size + ".jpg";
//
//            try (
//                    InputStream resizeInputStream = image.getInputStream();
//                    ByteArrayOutputStream resizedOutputStream = new ByteArrayOutputStream()
//            ) {
//                Thumbnails.of(resizeInputStream)
//                        .size(size, size)
//                        .outputFormat("jpg")
//                        .outputQuality(0.8)
//                        .toOutputStream(resizedOutputStream);
//
//                byte[] resizedBytes = resizedOutputStream.toByteArray();
//                String resizedUrl = uploadImageToS3(new ByteArrayInputStream(resizedBytes), resizedFileName, resizedBytes.length);
//                resizedUrls.put(size, resizedUrl);
//            }
//        }
//
//        return new S3ImageUrlDto(
//                originalUrl,
//                resizedUrls.get(400),
//                resizedUrls.get(800),
//                resizedUrls.get(1200)
//        );

    public String uploadAndResize(MultipartFile image, String folder) throws IOException {
        // 1. 원본 업로드
        String originalUrl = uploadImageToS3(image, folder);

//        // 2. S3에서 원본 이미지 다운로드
//        log.info("original url: {}", originalUrl);
//        S3Object s3Object = amazonS3.getObject(bucket, extractKeyFromUrl(originalUrl));
//        log.info("원본 이미지 다운로드 중...");
//        InputStream originalInputStream = s3Object.getObjectContent();
//        log.info("contentType: {}", s3Object.getObjectMetadata().getContentType());

        try {
            log.info("original url: {}", originalUrl);
            String key = extractKeyFromUrl(originalUrl);
            log.info("추출된 S3 키: {}", key);
            S3Object s3Object = amazonS3.getObject(bucket, key);
            log.info("원본 이미지 다운로드 중...");
            InputStream originalInputStream = s3Object.getObjectContent();
            log.info("contentType: {}", s3Object.getObjectMetadata().getContentType());

            // 3. 이미지 리사이징
            ByteArrayOutputStream resizedOutputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalInputStream)
                    .size(400, 400)
                    .outputFormat("jpg")
                    .outputQuality(1.0)
                    .toOutputStream(resizedOutputStream);

            byte[] resizedBytes = resizedOutputStream.toByteArray();

            // 4. 리사이징된 이미지 업로드
            String thumbnailFileName = UUID.randomUUID().toString().concat("_thumbnail.jpg");
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(resizedBytes.length);
            metadata.setContentType("image/jpeg");

            ByteArrayInputStream resizedInputStream = new ByteArrayInputStream(resizedBytes);
            amazonS3.putObject(new PutObjectRequest(bucket, folder + thumbnailFileName, resizedInputStream, metadata));

            // 스트림 정리
            originalInputStream.close();
            resizedInputStream.close();
            resizedOutputStream.close();

            return amazonS3.getUrl(bucket, folder + thumbnailFileName).toString();
        } catch (AmazonServiceException e) {
            log.error("AmazonServiceException: {}", e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("SdkClientException: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
        }
        return originalUrl;
    }


    private String extractKeyFromUrl(String s3Url) {
        try {
            // URL에서 path만 추출
            URL u = new URL(s3Url);
            // 도메인과 앞의 슬래시 제거 후 파일 경로만 반환
            return u.getPath().substring(1); // 앞의 '/' 제거
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid S3 URL", e);
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
                        .size(1200, 1200)
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

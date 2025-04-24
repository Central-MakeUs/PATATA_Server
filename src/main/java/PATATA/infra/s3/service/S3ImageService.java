package PATATA.infra.s3.service;

import PATATA.infra.s3.dto.S3ImageUrlDto;
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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static PATATA.global.error.code.status.ErrorStatus.IMAGE_EMPTY;
import static PATATA.global.error.code.status.ErrorStatus.S3_UPLOAD_FAIL;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3ImageUrlDto uploadOriginal(MultipartFile image, String folder) {
        //입력받은 이미지 파일이 빈 파일인지 검증
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            log.info("s3 upload image is empty");
            throw new S3ImageHandler(IMAGE_EMPTY);
        }
        log.info("프로필 사진 업데이트 중 ... ");
        return uploadOriginalImage(image, folder);
    }

    private S3ImageUrlDto uploadOriginalImage(MultipartFile image, String folder) {
        try {
            return uploadImageToS3(image, folder);
        } catch (IOException e) {
            log.error("S3 이미지 업로드 실패: {}", e.getMessage(), e);  // 스택 트레이스를 포함한 상세 로그
            throw new S3ImageHandler(S3_UPLOAD_FAIL);
        }
    }

    private S3ImageUrlDto uploadImageToS3(MultipartFile image, String folder) throws IOException {
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, folder + "/" + s3FileName, byteArrayInputStream, metadata);

        //put image to S3
        amazonS3.putObject(putObjectRequest);

        byteArrayInputStream.close();
        is.close();

        String originalUrl = amazonS3.getUrl(bucket, folder + "/" + s3FileName).toString();
        String resizedUrl400 = amazonS3.getUrl(bucket, folder + "_resized/resized_400_" + s3FileName).toString();

        return S3ImageUrlDto.of(originalUrl, resizedUrl400, null, null);
    }

    private String uploadThumbnailImage(MultipartFile image, String folder) throws IOException {
        log.info("JVM Heap - Max: {} MB, Total: {} MB, Free: {} MB",
                Runtime.getRuntime().maxMemory() / (1024 * 1024),
                Runtime.getRuntime().totalMemory() / (1024 * 1024),
                Runtime.getRuntime().freeMemory() / (1024 * 1024));

        ImageInputStream iis = ImageIO.createImageInputStream(image.getInputStream());
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        if (!readers.hasNext()) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
        }

        ImageReader reader = readers.next();
        reader.setInput(iis, true);
        BufferedImage bufferedImage = reader.read(0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(bufferedImage)
                .size(400, 400)
                .outputFormat("jpg")
                .outputQuality(0.8f)
                .toOutputStream(os);

        byte[] thumbnailBytes = os.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(thumbnailBytes);

        String thumbnailFileName = UUID.randomUUID().toString().concat("_thumbnail.jpg");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(thumbnailBytes.length);
        metadata.setContentType("image/jpeg");

        amazonS3.putObject(new PutObjectRequest(bucket, folder + thumbnailFileName, inputStream, metadata));

        iis.close();
        os.close();
        inputStream.close();

        return amazonS3.getUrl(bucket, folder + thumbnailFileName).toString();
    }

    // ----------------------------------------

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
        String baseFileName = folder + uuid;

        // 원본 업로드
        String originalFileName = baseFileName + "_o.jpg";
        String originalUrl;
        try (InputStream originalInputStream = image.getInputStream()) {
            originalUrl = uploadImageToS3(originalInputStream, originalFileName, image.getSize());
        }

        // 리사이징 이미지 생성 및 업로드
        Map<Integer, String> resizedUrls = new HashMap<>();
        int[] sizes = {400, 800, 1200};

        for (int size : sizes) {
            String resizedFileName = baseFileName + "_r" + size + ".jpg";

            try (
                    InputStream resizeInputStream = image.getInputStream();
                    ByteArrayOutputStream resizedOutputStream = new ByteArrayOutputStream()
            ) {
                Thumbnails.of(resizeInputStream)
                        .size(size, size)
                        .outputFormat("jpg")
                        .outputQuality(0.8)
                        .toOutputStream(resizedOutputStream);

                byte[] resizedBytes = resizedOutputStream.toByteArray();
                String resizedUrl = uploadImageToS3(new ByteArrayInputStream(resizedBytes), resizedFileName, resizedBytes.length);
                resizedUrls.put(size, resizedUrl);
            }
        }

        return new S3ImageUrlDto(
                originalUrl,
                resizedUrls.get(400),
                resizedUrls.get(800),
                resizedUrls.get(1200)
        );
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

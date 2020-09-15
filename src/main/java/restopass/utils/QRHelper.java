package restopass.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restopass.dto.response.QRData;
import sun.misc.BASE64Encoder;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class QRHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(QRHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String createQRBase64(String reservationId, String userId) {
        ByteArrayOutputStream createQrCodeImg = null;
        try {
            String content = generateUrl(reservationId, userId);
            createQrCodeImg = getQrCodeImageWithLogo(content);
        } catch (WriterException | IOException e) {
            LOGGER.error("Error generating QR code for: {}, {}", reservationId, userId);
        }

        BASE64Encoder encoder = new BASE64Encoder();
        String base64Img = encoder.encode(createQrCodeImg.toByteArray());

        return String.format("data:image/jpeg;base64,%s", base64Img.replaceAll("\r|\n", ""));
    }

    public static ByteArrayOutputStream getQrCodeImageWithLogo(String text) throws WriterException, IOException {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 250, 250, hints);

        MatrixToImageConfig config = new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

        BufferedImage logoImage = ImageIO.read(new File("src/main/resources/img/restopassIcon.png"));

        int deltaHeight = qrImage.getHeight() - logoImage.getHeight();
        int deltaWidth = qrImage.getWidth() - logoImage.getWidth();

        BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) combined.getGraphics();

        g.drawImage(qrImage, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        g.drawImage(logoImage, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

        ImageIO.write(combined, "png", output);
        return output;
    }

    public static byte[] decodeBase64ToByteArray(String qr) {
        return Base64.getEncoder().encode(qr.getBytes());
    }

    public static String generateUrl(String reservationId, String userId) throws IOException {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        return mapper.writeValueAsString(new QRData(reservationId,userId));
    }

}

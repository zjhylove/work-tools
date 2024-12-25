package com.zjhy.love.worktools.service;

import com.zjhy.love.worktools.model.AuthEntry;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

/**
 * 身份验证服务
 * 处理TOTP验证码的生成和验证
 */
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    /**
     * 生成TOTP验证码
     * @param secret Base32编码的密钥
     * @param digits 验证码位数
     * @param period 更新间隔（秒）
     * @return 当前验证码
     */
    public String generateTOTP(String secret, int digits, int period) {
        try {
            Base32 base32 = new Base32();
            byte[] bytes = base32.decode(secret.toUpperCase());
            
            long currentTime = System.currentTimeMillis() / 1000L;
            long counter = currentTime / period;
            
            return generateTOTPCode(bytes, counter, digits);
        } catch (Exception e) {
            LOGGER.error("生成TOTP验证码失败", e);
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    /**
     * 生成密钥
     * @return Base32编码的密钥
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }

    /**
     * 生成二维码
     * @param entry 验证条目
     * @return 二维码图片
     */
    public Image generateQRCode(AuthEntry entry) {
        try {
            String otpAuth = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
                entry.getIssuer(),
                entry.getName(),
                entry.getSecret(),
                entry.getIssuer(),
                entry.getAlgorithm(),
                entry.getDigits(),
                entry.getPeriod()
            );
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuth, BarcodeFormat.QR_CODE, 200, 200);
            
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(qrImage, null);
        } catch (Exception e) {
            LOGGER.error("生成二维码失败", e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    private String generateTOTPCode(byte[] key, long counter, int digits) {
        try {
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            
            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);
            
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (Exception e) {
            LOGGER.error("生成TOTP代码失败", e);
            throw new RuntimeException("生成验证码失败", e);
        }
    }
} 
package com.zjhy.love.worktools.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 身份验证条目
 * 存储单个身份验证器的配置信息
 */
public class AuthEntry {
    /**
     * 账户名称
     * 用于标识不同的验证账户
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * 密钥
     * Base32编码的密钥
     */
    private final StringProperty secret = new SimpleStringProperty();

    /**
     * 发行方
     * 通常是服务提供商的名称
     */
    private final StringProperty issuer = new SimpleStringProperty();

    /**
     * 算法
     * 默认为 SHA1
     */
    private final StringProperty algorithm = new SimpleStringProperty("SHA1");

    /**
     * 验证码位数
     * 默认为6位
     */
    private final IntegerProperty digits = new SimpleIntegerProperty(6);

    /**
     * 更新间隔（秒）
     * 默认为30秒
     */
    private final IntegerProperty period = new SimpleIntegerProperty(30);

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getSecret() {
        return secret.get();
    }

    public void setSecret(String secret) {
        this.secret.set(secret);
    }

    public StringProperty secretProperty() {
        return secret;
    }

    public String getIssuer() {
        return issuer.get();
    }

    public void setIssuer(String issuer) {
        this.issuer.set(issuer);
    }

    public StringProperty issuerProperty() {
        return issuer;
    }

    public String getAlgorithm() {
        return algorithm.get();
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm.set(algorithm);
    }

    public StringProperty algorithmProperty() {
        return algorithm;
    }

    public int getDigits() {
        return digits.get();
    }

    public void setDigits(int digits) {
        this.digits.set(digits);
    }

    public IntegerProperty digitsProperty() {
        return digits;
    }

    public int getPeriod() {
        return period.get();
    }

    public void setPeriod(int period) {
        this.period.set(period);
    }

    public IntegerProperty periodProperty() {
        return period;
    }


}
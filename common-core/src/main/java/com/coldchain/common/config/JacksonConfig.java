package com.coldchain.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 閰嶇疆绫?
 * 澶勭悊 LocalDateTime/LocalDate/LocalTime 鐨勫簭鍒楀寲涓庡弽搴忓垪鍖?
 *
 * @author Alnnt
 */
@Configuration
public class JacksonConfig {

    /**
     * 鏃ユ湡鏃堕棿鏍煎紡
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 鏃ユ湡鏍煎紡
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 鏃堕棿鏍煎紡
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 鍒涘缓 JavaTimeModule 鐢ㄤ簬澶勭悊 Java 8 鏃ユ湡鏃堕棿绫诲瀷
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime 搴忓垪鍖栧櫒鍜屽弽搴忓垪鍖栧櫒
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        // LocalDate 搴忓垪鍖栧櫒鍜屽弽搴忓垪鍖栧櫒
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        // LocalTime 搴忓垪鍖栧櫒鍜屽弽搴忓垪鍖栧櫒
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

        // 娉ㄥ唽妯″潡
        objectMapper.registerModule(javaTimeModule);

        // 绂佺敤灏嗘棩鏈熷啓涓烘椂闂存埑鐨勫姛鑳?
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 绂佺敤閬囧埌鏈煡灞炴€ф椂鎶涘嚭寮傚父
        objectMapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 绂佺敤绌哄璞″簭鍒楀寲鏃舵姏鍑哄紓甯?
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;
    }
}

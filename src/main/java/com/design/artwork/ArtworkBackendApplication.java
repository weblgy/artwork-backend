package com.design.artwork;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.design.artwork.mapper")
// 👇 注意这里：类名必须和你的文件名 ArtworkBackendApplication 一模一样
public class ArtworkBackendApplication {

    public static void main(String[] args) {
        // 👇 这里也要改成 ArtworkBackendApplication.class
        SpringApplication.run(ArtworkBackendApplication.class, args);
    }

}
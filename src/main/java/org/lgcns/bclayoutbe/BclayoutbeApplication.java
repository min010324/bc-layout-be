package org.lgcns.bclayoutbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BclayoutbeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BclayoutbeApplication.class, args);
    }

}

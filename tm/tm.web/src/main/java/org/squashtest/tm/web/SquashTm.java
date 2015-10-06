package org.squashtest.tm.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableAutoConfiguration
public class SquashTm {
    public static final void main(String[] args) {
        new SpringApplication(SquashTm.class).run(args);
    }

}

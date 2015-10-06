package org.squashtest.tm.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class SquashTm {
    public static final void main(String[] args) {
        new SpringApplication(SquashTm.class).run(args);
    }

    @Value("${squash.test}")
    private String test;

    @PostConstruct
    public void onPostconstruct() {
        System.err.println("TEST VAUT " + test);
    }



}

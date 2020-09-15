/**
 * 
 */
package com.plugin.qanda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Sankha
 *
 */

@SpringBootApplication
@EnableDiscoveryClient
public class QandaApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(QandaApplication.class, args);

	}
}

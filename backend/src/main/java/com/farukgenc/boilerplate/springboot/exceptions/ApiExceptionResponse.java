package com.farukgenc.boilerplate.springboot.exceptions;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExceptionResponse {

	private String message;

	private Integer status;

	private String error;

	private LocalDateTime timestamp;

}

package com.spring.auth.aws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinResponse {
	private boolean success; // true if operation succeeded
	private String message; // readable message for frontend
}

package com.example.userservice.error.contract;

import java.time.LocalDateTime;
import java.util.Map;

public interface ErrorResponse {
	
	LocalDateTime getTimestamp();
	int getStatus();
	String getError();
	String getMessage();
	String getPath();
	Map<String, Object>getDetails();
	
}

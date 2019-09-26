package top.microiot.controller;

import org.springframework.validation.BindingResult;

import top.microiot.exception.ValueException;

public abstract class IoTController {
	protected void throwError(BindingResult result) {
		if(result.hasErrors())
			throw new ValueException(result.getFieldError().getDefaultMessage());
	}
}

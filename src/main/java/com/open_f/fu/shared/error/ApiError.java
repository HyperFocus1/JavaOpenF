package com.open_f.fu.shared.error;

public record ApiError(int status, String error, String message) {
}

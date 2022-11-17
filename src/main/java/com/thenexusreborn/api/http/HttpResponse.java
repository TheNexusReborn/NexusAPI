package com.thenexusreborn.api.http;

public record HttpResponse(String url, String response, int responseCode) {
}

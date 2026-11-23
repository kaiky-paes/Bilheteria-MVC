package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8081), 0);
        servidor.createContext("/", Main::abrirPagina);
        servidor.start();
        System.out.println("Bilheteria disponível em http://localhost:8081");

    }

    private static void abrirPagina(HttpExchange troca) throws IOException {
        if (!troca.getRequestURI().getPath().equals("/")) {
            responderPagina
                    (troca, 404, "Página não encontrada.".getBytes(StandardCharsets.UTF_8), "text/plain");
            return;
        }

        if (!troca.getRequestMethod().equals("GET")) {
            troca.getResponseHeaders().set("Allow", "GET");
            responderPagina(troca, 405, "Utilize GET.".getBytes(StandardCharsets.UTF_8),
                    "text/plain");
            return;
        }
        try (InputStream arquivo = Main.class.getResourceAsStream("/public/index.html")) {
            if (arquivo == null) {
                responderPagina(troca, 500, "Arquivo HTML não encontrado.".getBytes(StandardCharsets.UTF_8), "text/plan");
                return;
            }
            responderPagina(troca, 200, arquivo.readAllBytes(), "text/html");
        }
    }

    private static void responderPagina(HttpExchange troca, int status, byte[] bytes, String tipo) throws IOException {
        troca.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        troca.sendResponseHeaders(status, bytes.length);
        troca.getResponseBody().write(bytes);
        troca.close();
    }
}
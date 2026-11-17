package org.example;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class IngressoController {
    public static void calcular(HttpExchange troca) throws IOException {
        if (!troca.getRequestURI().getPath().equals("/api/ingressos")) {
            responder(troca, 404, "{\"erro\":\"Rota não encontrado.\"}");
            return;
        }
        if (!troca.getRequestMethod().equals("POST")) {
            troca.getResponseHeaders().set("Allow", "POST");
            responder(troca, 405, "{\"erro\":\"Utilize POST para calcular.\"}");
            return;
        }
        try {
            Map<String, String> dados = lerFormulario(troca);
            int quantidade = Integer.parseInt(dados.getOrDefault("quantidade", ""));

            Ingresso ingresso = new Ingresso(
                    dados.getOrDefault("filme", ""),
                    quantidade,
                    dados.getOrDefault("tipo", "")
            );
            String json = "{\"filme\":\"" + ingresso.getTitulo()
                    + "\",\"tipo\":\"" + ingresso.getTipo()
                    + "\",\"quantidade\":" + ingresso.getQuantidade()
                    + ",\"unitarioCentavos\":" + ingresso.getPrecoUnitarioCentavos()
                    + ",\"totalCentavos\":" + ingresso.calcularTotalCentavos() + "}";
            responder(troca, 200, json);
        } catch (NumberFormatException erro) {
            responder(troca, 400, "{\"erro\":\"Informe uma quantidade inteira.\"}");
        } catch (IllegalArgumentException erro) {
            responder(troca, 400, "{\"erro\":\"Confira o filme, o tipo e a quantidade de 1 a 10.\"}");
        }
    }

    private static Map<String, String> lerFormulario(HttpExchange troca) throws IOException {
        String corpo = new String(
                troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8
        );
        Map<String, String> dados = new HashMap<>();
        for (String campo : corpo.split("&")) {
            String[] partes = campo.split("=", 2);
            String nome = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length == 2
                    ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            dados.put(nome, valor);
        }
        return dados;
    }

    private static void responder(HttpExchange troca, int status, String json)
            throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        troca.sendResponseHeaders(status, bytes.length);
        troca.getResponseBody().write(bytes);
        troca.close();
    }
}

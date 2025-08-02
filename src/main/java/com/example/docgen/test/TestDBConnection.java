package com.example.docgen.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnection {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/prosperadbteste?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Garante que o driver seja carregado

            System.out.println("Tentando conectar ao banco de dados...");
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                System.out.println("Conexão bem-sucedida!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Erro: Driver JDBC não encontrado. Verifique suas dependências.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erro de conexão com o banco de dados:");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
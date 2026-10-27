# Backend-AVIA
Backend do nosso trabalho de Ánalise e Desenvolvimento de Software (APS): AVIA - Sistema de Gestão de Tarefas

## Ambientes

| Ambiente | Aplicação | API |
|---|---|---|
| Homologação (`develop`) | https://avia-homolog.gesaduece.com.br | https://avia-api-homolog.gesaduece.com.br |
| Produção (`main`) | https://avia.gesaduece.com.br | https://avia-api.gesaduece.com.br |

O deploy é automático pelo Dokploy: cada push na `develop` publica em homologação e cada push na `main` em produção.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok
- Swagger

Ao clonar o projeto, configure o seu proprio application.properties.

Com o Maven instalado, execute:
mvn clean install

Para rodar o projeto:
mvn spring-boot:run

Link do Swagger:
http://localhost:8080/swagger-ui/index.html?continue#

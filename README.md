# Class Market 🛒 — Spring Boot + REST API

Marketplace estudantil para venda de comidas e bebidas na FUCAPI.

---

## Estrutura do projeto

```
classmarket/
├── pom.xml                          ← Maven (Spring Boot 3.3)
├── sql/
│   ├── classmarket.sql              ← DDL + seed completo
│   └── migration_springboot.sql     ← Migração se já tiver o banco legado
│
└── src/main/
    ├── java/com/classmarket/
    │   ├── ClassMarketApplication.java
    │   ├── config/
    │   │   └── SecurityConfig.java  ← Spring Security + CORS
    │   ├── security/
    │   │   ├── JwtUtil.java         ← Geração e validação de tokens JWT
    │   │   └── JwtFilter.java       ← Filtro HTTP que injeta autenticação
    │   ├── model/                   ← Entidades JPA
    │   ├── repository/              ← Spring Data JPA repositories
    │   ├── service/                 ← Lógica de negócio
    │   ├── dto/Dto.java             ← Todos os DTOs (request/response)
    │   └── controller/              ← REST controllers
    │
    └── resources/
        ├── application.properties
        └── static/                  ← Frontend SPA servido pelo Spring Boot
            ├── index.html
            ├── css/main.css
            └── js/main.js           ← Adaptado para consumir a REST API
```

---

## 1. Pré-requisitos

- Java 17+
- Maven 3.8+
- MySQL 8.0+

---

## 2. Banco de dados

### Banco novo
```bash
mysql -u root -p < sql/classmarket.sql
```

### Migrar banco legado (já existe o classmarket.sql)
```bash
mysql -u root -p classmarket < sql/migration_springboot.sql
```

A migração:
- Adiciona a coluna `status` em `produtos`
- Atualiza a senha do ADM para BCrypt (`adm123`)
- Cria índices úteis

---

## 3. Configuração

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/classmarket?...
spring.datasource.username=root
spring.datasource.password=SUA_SENHA_AQUI

# Gere uma chave segura para produção (Base64, 256+ bits)
classmarket.jwt.secret=Y2xhc3NtYXJrZXQtc2VjcmV0LWtleS1kZXZlLXNlci10cm9jYWRhLWVtLXByb2R1Y2Fv
```

---

## 4. Compilar e executar

```bash
mvn clean package -DskipTests
java -jar target/classmarket-1.0.0.jar
```

Ou em modo dev:
```bash
mvn spring-boot:run
```

Acesse: **http://localhost:8080**

---

## 5. REST API

Todas as rotas estão sob `/api`. A autenticação usa JWT no header:
```
Authorization: Bearer <token>
```

### Auth (pública)
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Login → `{ token, usuario }` |
| POST | `/api/auth/cadastro` | Cadastro de usuário |
| POST | `/api/auth/esqueci-senha` | Gerar token de reset |
| GET  | `/api/auth/token-valido?token=` | Verificar token |
| POST | `/api/auth/redefinir-senha` | Redefinir senha |

### Produtos (GET público, demais requerem login)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/produtos?categoria=doces&busca=brow` | Listar aprovados |
| GET | `/api/produtos/{id}` | Produto por ID |
| GET | `/api/produtos/meus` | Produtos do usuário logado |
| POST | `/api/produtos` | Cadastrar produto |
| PUT | `/api/produtos/{id}` | Atualizar (dono ou ADM) |
| DELETE | `/api/produtos/{id}` | Excluir (dono ou ADM) |

### Avaliações (GET público, POST requer login)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/avaliacoes` | Listar todas |
| GET | `/api/avaliacoes/produto/{id}` | Listar por produto |
| POST | `/api/avaliacoes` | Enviar avaliação |

### ADM (requer role ADM)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/adm/produtos` | Todos os produtos |
| GET | `/api/adm/produtos/pendentes` | Pendentes de aprovação |
| PATCH | `/api/adm/produtos/{id}/status` | Aprovar / negar |
| DELETE | `/api/adm/produtos/{id}` | Excluir produto |
| GET | `/api/adm/usuarios` | Listar usuários |
| DELETE | `/api/adm/usuarios/{id}` | Desativar usuário |

---

## 6. Credenciais padrão

| Conta | E-mail | Senha |
|-------|--------|-------|
| Administrador | classmarket@proton.me | adm123 |

> A senha é armazenada como BCrypt. O hash é aplicado automaticamente pela migration SQL.

---

## 7. Deploy (Railway / Render)

Configure as variáveis de ambiente:
```
SPRING_DATASOURCE_URL=jdbc:mysql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
CLASSMARKET_JWT_SECRET=<chave-segura-base64>
PORT=8080
```

O `server.port` lê `${PORT:8080}` automaticamente no Railway.

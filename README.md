# FlowBank

Aplicação bancária simplificada com cadastro, login, consulta de saldo, transferência entre contas (Pix) e extrato. Projeto da faculdade (Ciência da Computação, USJT), com backend em Java/Spring Boot e frontend em HTML, CSS e JavaScript puro.

> **Status:** protótipo acadêmico em evolução. O fluxo principal funciona, mas o projeto tem limitações conhecidas (veja [Limitações conhecidas](#limitações-conhecidas)). Elas estão listadas de propósito e viram o roteiro de melhorias.

## O que está implementado

- Cadastro e login de usuários, com senha protegida por BCrypt.
- Autenticação stateless com JWT (expiração de 10 horas).
- Consulta de saldo do usuário autenticado.
- Transferência entre usuários, usando o e-mail do destinatário como chave Pix. Valida destinatário inexistente, transferência para si mesmo e saldo insuficiente.
- Extrato com entradas e saídas, em ordem do mais recente para o mais antigo.
- Frontend com telas de boas-vindas, cadastro, login, home, envio de Pix, transferência, extrato e cotação de moedas (consome a API pública Frankfurter).

## Stack

| Camada | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA |
| Autenticação | JWT (jjwt 0.12), BCrypt |
| Banco de dados | MySQL (H2 também está nas dependências) |
| Frontend | HTML, CSS, JavaScript (`fetch`), Chart.js na tela de cotação |
| Build | Maven (wrapper incluso) |

## Estrutura

```
FlowBankCompleto/
├── BackEnd/        API REST (Spring Boot)
│   └── src/main/java/com/flowbank/
│       ├── controller/   AuthController, BankController
│       ├── service/      AuthService, BankService
│       ├── repository/   UserRepository, TransactionRepository
│       ├── model/        User, Transaction
│       ├── dto/          AuthRequest, AuthResponse, TransferRequest, TransactionResponse
│       ├── config/       SecurityConfig, JwtAuthenticationFilter
│       └── util/         JwtUtil
├── Inicio/         Telas de boas-vindas, cadastro e login
├── Pagamentos/     Telas de home, Pix, transferência, extrato e cotação
└── Imagem/         Imagens usadas pelo frontend
```

## API

Base: `http://localhost:8080`. Rotas em `/bank/**` exigem o header `Authorization: Bearer <token>`.

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/auth/register` | Cria uma conta (`email`, `password`) e retorna um token |
| POST | `/auth/login` | Autentica e retorna um token |
| GET | `/bank/balance` | Saldo do usuário autenticado |
| POST | `/bank/transfer` | Transfere para outro usuário (`toEmail`, `amount`) |
| GET | `/bank/extract` | Extrato do usuário autenticado |

## Como rodar

### Pré-requisitos

- JDK 17
- MySQL em execução em `localhost:3306`
- Uma extensão ou servidor estático para abrir o frontend, como o Live Server do VS Code

### Backend

1. Confira as credenciais do banco em `BackEnd/src/main/resources/application.yaml`. O banco `a3faculdade` é criado automaticamente se não existir, e as tabelas são geradas pelo Hibernate (`ddl-auto: update`).
2. Suba a API:

```bash
cd BackEnd
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080`.

### Frontend

Abra a pasta do projeto com o Live Server na porta **5500** (a configuração de CORS do backend só aceita `localhost:5500` e `127.0.0.1:5500`) e comece por `Inicio/Boas Vindas.html`.

> Observação: um usuário novo começa com saldo zero. Para testar uma transferência é preciso ajustar o saldo diretamente no banco.

## Limitações conhecidas

Estas são as falhas que eu identifiquei na revisão do código e pretendo corrigir:

- **Concorrência na transferência:** `BankService.transfer` não é transacional nem usa lock. Transferências simultâneas podem gastar o mesmo saldo duas vezes, e uma falha no meio da operação pode deixar os saldos inconsistentes.
- **Segredos no repositório:** o segredo do JWT e as credenciais do banco estão no `application.yaml`. Devem ir para variáveis de ambiente, e o segredo atual deve ser tratado como comprometido.
- **Tratamento de erros:** validações duplicadas no controller, saldo insuficiente retornando 401, login inválido retornando 200 sem token, sem Bean Validation nos DTOs e mensagens internas de exceção devolvidas ao cliente.
- **Sem testes automatizados** e sem Docker.
- **Frontend:** a URL da API está repetida nos arquivos HTML, o token fica no `localStorage` e as telas de Pix copia e cola e TED são apenas layout, sem backend.

## Roadmap

- [ ] Mover segredos para variáveis de ambiente e criar um perfil de desenvolvimento com H2
- [ ] Tornar a transferência atômica (`@Transactional` com lock) e cobrir com teste de concorrência
- [ ] Tratamento global de erros (`@ControllerAdvice`) e códigos HTTP corretos
- [ ] Bean Validation nos DTOs
- [ ] Testes unitários do serviço e de integração (MockMvc)
- [ ] Docker Compose com API e banco
- [ ] Remover ou sinalizar telas sem backend

## Autoria

Projeto desenvolvido na faculdade por **Gustavo Guerato**. <!-- Se foi feito em grupo, liste aqui os integrantes e o que foi responsabilidade de cada um. -->

Meu foco atual está em TypeScript, Node.js, React e PostgreSQL. Veja também o [AccessForge](https://github.com/GustavoGuerato/workspace_auth).

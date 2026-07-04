# Contrato de API — Backend AVIA

> Branch: `feature/dev/projetos`  
> Base URL: `http://localhost:8080`  
> Gerado em: 2026-06-09

---

## Mudança aplicada (CORS)

CORS foi configurado em `SecurityConfig.java`. O backend agora aceita requisições de:

- **Origin permitida:** `http://localhost:5173`
- **Métodos:** GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Headers:** `Authorization`, `Content-Type`
- **Credentials:** permitido (`withCredentials: true` no fetch é válido)

**Nenhuma mudança necessária no frontend para CORS** — só garantir que `api.js` usa a URL base correta.

---

## Autenticação — JWT

O JWT é emitido com **HS256**. Claims presentes no payload:

```json
{
  "iss": "avia-api",
  "sub": "email@do.usuario",
  "id": 1,
  "nome": "Nome do Usuário",
  "iat": 1234567890,
  "exp": 1234567890
}
```

**Importante para o frontend:**
- Campo `sub` = email do usuário
- Campo `id` = ID numérico do usuário
- Campo `nome` = nome do usuário
- **Não existe claim `role`** — o backend não implementa papéis no token ainda. O `parseJwt` no frontend pode setar `role: 'MEMBRO'` como default sem problema funcional por enquanto.
- Expiração padrão: **86400 segundos (24h)**
- Header obrigatório em rotas protegidas: `Authorization: Bearer <token>`

---

## Endpoints

### Auth — rotas públicas (sem token)

#### `POST /auth/cadastro`
Cria novo usuário.

**Request:**
```json
{
  "nome": "Pedro Lavor",
  "email": "pedro@email.com",
  "senha": "minhasenha123"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "nome": "Pedro Lavor",
  "email": "pedro@email.com"
}
```

**Erros:**
| Status | Causa |
|--------|-------|
| 400 | Campo inválido ou vazio (validação Bean Validation) |
| 500 | E-mail já cadastrado (`"E-mail já cadastrado"`) |

> **Atenção:** e-mail duplicado retorna 500, não 409. O frontend deve tratar `status === 500` com mensagem genérica ou checar `error.message`.

---

#### `POST /auth/login`
Autentica e retorna JWT.

**Request:**
```json
{
  "email": "pedro@email.com",
  "senha": "minhasenha123"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGci..."
}
```

**Erros:**
| Status | Causa |
|--------|-------|
| 400 | Campo vazio/inválido |
| 401 | Credenciais incorretas (Spring Security) |

---

#### `POST /auth/recuperacao-senha`
Solicita token de recuperação de senha.

**Request:**
```json
{
  "email": "pedro@email.com"
}
```

**Response `204 No Content`** (sempre, mesmo se e-mail não existe — não vaza informação)

> Em desenvolvimento, o link de recuperação é impresso no console do servidor:  
> `Link de recuperação de senha: http://localhost:5173/redefinir-senha?token=<uuid>`  
> **Não existe envio de e-mail ainda.** O frontend deve informar ao usuário para verificar o console do backend em dev.

---

#### `POST /auth/redefinicao-senha`
Redefine senha usando token de recuperação.

**Request:**
```json
{
  "token": "uuid-do-token",
  "novaSenha": "novasenha456"
}
```

**Response `204 No Content`**

**Erros:**
| Status | Causa |
|--------|-------|
| 500 | Token inválido, já utilizado ou expirado (expira em 30 minutos) |

---

#### `POST /auth/logout`
Stub stateless — não faz nada no servidor.

**Response `204 No Content`**

> Frontend deve apenas limpar o token do localStorage/sessionStorage.

---

### Usuário — rotas protegidas (token obrigatório)

#### `GET /usuario/eu`
Retorna perfil do usuário autenticado.

**Response `200 OK`:**
```json
{
  "id": 1,
  "nome": "Pedro Lavor",
  "email": "pedro@email.com"
}
```

---

#### `PATCH /usuario/eu`
Atualiza nome e/ou senha do usuário autenticado.

**Request** (todos os campos opcionais):
```json
{
  "nome": "Novo Nome",
  "senhaAtual": "senhaantiga",
  "novaSenha": "senhanova"
}
```

**Response `200 OK`:** mesmo formato de `GET /usuario/eu`

---

### Projetos — rotas protegidas (token obrigatório)

#### `POST /projetos`
Cria projeto. Criador vira **gerente** automaticamente.

**Request:**
```json
{
  "nome": "Meu Projeto",
  "tipoEstimativa": "FIBONACCI"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "nome": "Meu Projeto",
  "tipoEstimativa": "FIBONACCI"
}
```

---

#### `GET /projetos`
Lista todos os projetos onde o usuário é membro (qualquer cargo).

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "nome": "Meu Projeto",
    "tipoEstimativa": "FIBONACCI"
  }
]
```

> **Limitação:** a response não inclui o cargo (`gerente` vs membro) do usuário no projeto. O frontend não consegue saber quem é gerente apenas com este endpoint.

---

#### `GET /projetos/{id}`
Busca projeto por ID. Só membros do projeto podem visualizar.

**Response `200 OK`:** mesmo formato acima

**Erros:**
| Status | Causa |
|--------|-------|
| 500 | Projeto não encontrado ou usuário não é membro |

---

#### `DELETE /projetos/{id}`
Deleta projeto. Só o **gerente** pode deletar.

**Response `204 No Content`**

**Erros:**
| Status | Causa |
|--------|-------|
| 500 | Projeto não encontrado ou usuário não é gerente |

---

#### `PATCH /projetos/{id}`
Edita nome do projeto. Só o **gerente** pode editar.

**Request:**
```json
{
  "nome": "Nome Atualizado"
}
```

**Response `200 OK`:** projeto atualizado no mesmo formato

**Erros:**
| Status | Causa |
|--------|-------|
| 500 | Projeto não encontrado ou usuário não é gerente |

---

## Formato de erro padrão

Sem `@RestControllerAdvice` implementado. Erros retornam o formato padrão do Spring Boot:

```json
{
  "timestamp": "2026-06-09T12:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/projetos/99"
}
```

> O campo `message` **não aparece por padrão** no Spring Boot 3+/4.x (segurança). Para validação (400), o campo `errors` lista os problemas por campo.

**Recomendação para `api.js`:** checar `response.status` ao invés de tentar ler `response.error.message`.

---

## Ajustes necessários no frontend

### 1. `src/services/api.js` — URL base e headers

```js
const BASE_URL = 'http://localhost:8080';

// Todas as requisições protegidas precisam do header:
headers: {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${getToken()}`
}
```

### 2. `AuthContext.jsx` — parseJwt

O JWT contém `sub` (email), `id`, `nome`. Não contém `role`. Mapear assim:

```js
function parseJwt(token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return {
    id: payload.id,
    nome: payload.nome,
    email: payload.sub,
    role: 'MEMBRO' // default até backend implementar
  };
}
```

### 3. `ForgotPasswordPage.jsx` — resposta sempre 204

`POST /auth/recuperacao-senha` retorna 204 independente do e-mail existir. Exibir sempre mensagem de sucesso. Não há como distinguir e-mail válido de inválido (comportamento correto por segurança).

### 4. `ResetPasswordPage.jsx` — payload correto

Campo esperado é `novaSenha`, não `newPassword` ou `password`:

```js
await api.post('/auth/redefinicao-senha', {
  token,          // string do ?token= na URL
  novaSenha       // campo com a nova senha
});
```

### 5. Tratamento de erros de projeto

Erros de permissão retornam 500 (não 403/404). Tratar `status >= 500` com mensagem genérica: "Operação não permitida" ou "Projeto não encontrado".

---

## O que ainda não existe no backend

| Feature | Status |
|---------|--------|
| Sprint endpoints | ❌ não implementado |
| Tarefa endpoints | ❌ não implementado |
| Membros do projeto (convidar/listar) | ❌ não implementado |
| Campo cargo na response de projetos | ❌ ausente |
| Envio de e-mail (recuperação senha) | ❌ só console.log |
| Claim `role` no JWT | ❌ ausente |
| Handler de erros estruturado (403/404) | ❌ ausente |

Manter mocks para Sprint, Tarefa e Membros até estes endpoints existirem.

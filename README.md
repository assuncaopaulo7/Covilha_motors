# Exemplo de Página Simples

Isto é um exemplo de uma página simples, do início ao fim. Comecei por registar cliente, depois registar vendedor, pôr carros à venda usando o vendedor, e comprar carros usando o cliente. E também a funcionalidade de Adicionar, Atualizar e Eliminar carros pelo vendedor, e Comprar carros pelo cliente. Gerar faturas. Erros e mensagens de aviso.

Vou meter o código no github para darem uma vista de olhos. As tabelas estao muito simples ainda, e não sei como fazer com q um de nós corra o projeto num computador e outro colega aceda ao website (usando o mesmo wifi) e possamos simular:

- a) Pessoa 1 corre website
- b) Pessoa 2 acede ao website e acede como vendedor para gerir carros. E depois aceder como segundo vendedor (Podem haver vários cliente e vendedores)
- c) Pessoa 3 acede ao website e acede como cliente para comprar carros
- d) Pessoa 4 acede ao website e acede como segundo cliente para comprar carros

---

## !!! PARA TESTAREM NOS VOSSOS PC'S !!!

- clicar no canto direito do intelliJ no "m" de "Maven" e clicar no reload Icon e escolher "Reload all Projects"
- Dar run no play button
- ir ao website `localhost:8080`  
  *(O localhost:3306 é para a conexao no mysql. Corresponde ao sítio onde temos a base de dados)*

---

## COMO ACEDER PELO SMARTPHONE

**Privacy & Security > Location** no windows faz com q possamos connectar do smartphone para o servidor do vosso pc q esta a correr o codigo.

No vosso pc ir ao **CMD** e escrever:

```
ipconfig
```

e vão ver uma coisa deste género:

```
IPv4 Address. . . . . . . . . . . : 192.168.1.2
```

Ir ao browser do vosso smartphone e escrever:

```
http://192.168.1.2:8080/
```

---

## SEGURANÇA

Passar os "vendedores" para "gestores".

Cada vez que os gestores meterem um carro para dentro da tabela de carros, deve haver uma outra tabela que dica: ("Atualizar" button, "Eliminar" button e "Adicionar novo" sao ambos INSERIU/RETIROU)

### Tabela Exemplo

```
gestor_id | car_id | inserir/retirar | number_of_cars
------------------------------------------------------
1         | 4      | INSERIU         | 1
2         | 4      | RETIROU         | 2
```

---

## ESTATÍSTICA

**Marcos: TO_DO_LIST:** Temos de ter um botão no website chamado "estatísticas" onde podemos ver os users todos, mas podemos filtrar por:

- "maior gastador"
- "menor gastador"
- ou outros fatores importantes

Ou podemos ver todos os carros e filtrar pelo:

- modelo e/ou categoria "mais vendido"
- ou pelo modelo e/ou categoria que gerou mais dinheiro
- ou o "último modelo que deu entrada no website pelo vendedor"

E outras estatísticas como:

- `SUM`s
- `AVG`s
- `COUNT`s  
  *(soma, média de profits e counts de vendas, etc)*

---

## CENA EXTRA

**TO_DO_LIST:** Column "Disponibilidade" na tabela Car, que podem dizer:

```
"Disponível" ou "Esgotado"
```

---

## SECURITY CRYPTOGRAPHY

*(Não sei se é bem assim, mas deve ser algo deste género:)*

### Steps to Enable HTTPS (TLS):

**Generate a Keystore (contains SSL certificate + private key):**

```bash
keytool -genkeypair -alias lojaveiculos -keyalg RSA -keysize 2048 \
-storetype PKCS12 -keystore keystore.p12 -validity 3650
```

**Place the `keystore.p12` file in:**

```
src/main/resources
```

**Configure Spring Boot in `application.properties`:**

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=lojaveiculos
```

**Run your application and access:**

```
https://localhost:8443
```

> This enables encrypted HTTPS communication between your PC (server) and other devices (clients). And also you need to check if it's working.

---

## NOTA

Não sei se para a parte da segurança a stora se refere também a criptografia, eu não sei se ela quer que nós usemos chaves entre pc e phone para garantir ligação segura, porque tecnicamente, todos os smartphones se deveriam poder conectar ao website que está a ser corrido no nosso pc, porque o objetivo de um website é ser acedido por todos. Por isso, à partida não é preciso.

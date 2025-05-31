
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


O root usa a password que definiram quando instalaram o mysql, e usam-na para aceder ao mysql e mysqlWorkbench.

Criamos um user app_user (root/user) e associamos esse user à BD loja_veiculos. A pass "pass" do application.properties serve para entrar nesse user:

CREATE USER 'app_user'@'%' IDENTIFIED BY 'pass';

GRANT ALL PRIVILEGES ON loja_veiculos.* TO 'app_user'@'%';

FLUSH PRIVILEGES;

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

## CENAS A IMPLEMENTAR:

- Ou fazemos o codigo com o que demos nas aulas práticas "Spring Boot" (que já está feito) ou facilmente podemos mudar para o que demos nas aulas teóricas "Jakarta EE / EJB". Talvez a primeira opção seja melhor, sendo que já está feita.

- Só deve haver 1 administrador e vários clientes. O adiminstrador apenas Adiciona/Remove/Atualiza carros e vê Estatísticas. Os clientes apenas compram carros. O Registar apenas serve para clientes. O Login serve para clientes e o administrador.

- Eu quero um ficheiro que faça com que ao eu meter no github o projeto e depois alguém fazer download, essa pessoa fique com os dados de clientes, sales, etc 

- Quando elimina-se um carro, manter as vendas desse carro no sales table. Eu elimineium carro, e quando fui ver a tabela sales, havia sales desse carro, que também foram eliminadas, só porque o carro tinha sido eliminado. Isto nao pode acontecer, e temos um "problema de cascata" que temos de resolver.

- Meter página inicial do site com css estilo, tipo imagens para cada carro (ao adicionar um carro, teria de tb ser preciso para introduzir uma imagem do PC do administrador para ser associada ao carro a ser adicionado). Ficaria do estilo do website do continente ou worten, em vez de uma simples tabela.
